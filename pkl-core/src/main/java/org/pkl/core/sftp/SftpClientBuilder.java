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
import java.time.Duration;
import java.util.function.Supplier;

final class SftpClientBuilder implements SftpPklClient.Builder {

  private String username;
  private String host = "localhost";
  private int port = 22;
  private Duration connectTimeout = Duration.ofSeconds(60);

  @Override
  public SftpPklClient.Builder setUsername(String username) {
    this.username = username;
    return this;
  }

  @Override
  public SftpPklClient.Builder setHost(String host) {
    this.host = host;
    return this;
  }

  @Override
  public SftpPklClient.Builder setPort(int port) {
    this.port = port;
    return this;
  }

  @Override
  public SftpPklClient.Builder setConnectTimeout(Duration timeout) {
    this.connectTimeout = timeout;
    return this;
  }

  @Override
  public SftpPklClient build() {
    return doBuild().get();
  }

  @Override
  public SftpPklClient buildLazily() {
    return new LazySftpClient(doBuild());
  }

  private Supplier<SftpPklClient> doBuild() {
    return () -> {
      try {

        return new ApacheMinaSftpClient(username, host, port);
      } catch (IOException e) {
        throw new RuntimeException("Failed to initialize SFTP client", e);
      }
    };
  }
}
