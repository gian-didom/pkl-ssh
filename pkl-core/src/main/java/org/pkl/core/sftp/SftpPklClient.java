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
import java.time.Duration;

/**
 * An SFTP client.
 *
 * <p>To create a new SFTP client, use a {@linkplain #builder() builder}. To perform SFTP
 * operations, use {@link #upload} and {@link #download}. To release resources held by the client,
 * use {@link #close}.
 *
 * <p>SFTP clients are thread-safe. Each client maintains its own connection pool. For efficiency
 * reasons, clients should be reused whenever possible.
 */
public interface SftpPklClient extends AutoCloseable {

  /** A builder of {@linkplain SftpPklClient SFTP clients}. */
  interface Builder {

    /** Sets the username for the SFTP connection. */
    Builder setUsername(String username);

    /** Sets the host for the SFTP connection. */
    Builder setHost(String host);

    /** Sets the port for the SFTP connection. */
    Builder setPort(int port);

    /**
     * Sets the timeout for connecting to a server.
     *
     * <p>Defaults to 60 seconds.
     */
    Builder setConnectTimeout(Duration timeout);

    /**
     * Creates a new {@code SftpPklClient} from the current state of this builder.
     *
     * @throws SftpClientInitException if an error occurs while initializing the client
     */
    SftpPklClient build();

    /**
     * Returns an {@code SftpPklClient} wrapper that defers building the actual SFTP client until
     * the wrapper's {@link SftpPklClient#upload} or {@link SftpPklClient#download} method is
     * called.
     *
     * <p>Note: When using this method, any exception thrown when building the actual SFTP client is
     * equally deferred.
     */
    SftpPklClient buildLazily();
  }

  /**
   * Creates a new {@code SftpPklClient} builder with default settings.
   *
   * <p>The default settings are:
   *
   * <ul>
   *   <li>Connect timeout: 60 seconds
   *   <li>Host: "localhost"
   *   <li>Port: 22
   * </ul>
   */
  static Builder builder() {
    return new SftpClientBuilder();
  }

  /**
   * Uploads a file to the SFTP server.
   *
   * @param localPath the path of the local file
   * @param remotePath the path on the remote server
   * @throws IOException if an I/O error occurs during the upload
   */
  void upload(Path localPath, String remotePath) throws IOException;

  /**
   * Sets the username for the next session
   *
   * @param username the username to be set
   * @throws IllegalArgumentException if the username is null
   */
  void setUsername(String username) throws IllegalArgumentException;

  /**
   * Sets the host for the next session
   *
   * @param host the host to be set
   * @throws IllegalArgumentException if the host is null
   */
  void setHost(String host) throws IllegalArgumentException;

  /**
   * Sets the host for the next session
   *
   * @param port the host to be set
   * @throws IllegalArgumentException if the port is null
   */
  void setPort(int port) throws IllegalArgumentException;

  /**
   * Downloads a file from the SFTP server.
   *
   * @param remotePath the path on the remote server
   * @throws IOException if an I/O error occurs during the download
   */
  byte[] download(String remotePath) throws IOException;

  /** Returns a client that throws {@link AssertionError} on every attempt to send a request. */
  static SftpPklClient dummyClient() {
    return new DummySftpClient();
  }

  /**
   * Closes this client.
   *
   * <p>This method makes a best effort to release the resources held by this client in a timely
   * manner. This may involve waiting for pending requests to complete.
   *
   * <p>Subsequent calls to this method have no effect. Subsequent calls to any other method throw
   * {@link IllegalStateException}.
   */
  void close();
}
