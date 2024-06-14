/**
 * Copyright © 2024 Apple Inc. and the Pkl project authors. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.pkl.core.sftp;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Optional;
import java.util.function.Supplier;
import javax.annotation.concurrent.GuardedBy;
import javax.annotation.concurrent.ThreadSafe;

/**
 * An {@code SftpPklClient} decorator that defers creating the underlying SFTP client until the
 * first upload or download.
 */
@ThreadSafe
final class LazySftpClient implements SftpPklClient {
  private final Supplier<SftpPklClient> supplier;
  private final Object lock = new Object();

  @GuardedBy("lock")
  private SftpPklClient client;

  @GuardedBy("lock")
  private RuntimeException exception;

  LazySftpClient(Supplier<SftpPklClient> supplier) {
    this.supplier = supplier;
  }

  @Override
  public void setUsername(String username) {
    getOrCreateClient().setUsername(username);
  }

  @Override
  public void setHost(String host) {
    getOrCreateClient().setHost(host);
  }

  @Override
  public void setPort(int port) {
    getOrCreateClient().setPort(port);
  }

  @Override
  public void upload(Path localPath, String remotePath) throws IOException {
    getOrCreateClient().upload(localPath, remotePath);
  }

  @Override
  public byte[] download(String remotePath) throws IOException {
    return getOrCreateClient().download(remotePath);
  }

  @Override
  public void close() {
    getClient().ifPresent(SftpPklClient::close);
  }

  private SftpPklClient getOrCreateClient() {
    synchronized (lock) {
      if (exception != null) {
        throw exception;
      }

      if (client == null) {
        try {
          client = supplier.get();
        } catch (RuntimeException t) {
          exception = t;
          throw t;
        }
      }
      return client;
    }
  }

  private Optional<SftpPklClient> getClient() {
    synchronized (lock) {
      return Optional.ofNullable(client);
    }
  }
}
