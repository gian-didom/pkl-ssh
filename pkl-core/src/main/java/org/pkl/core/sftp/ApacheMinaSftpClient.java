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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.apache.sshd.client.SshClient;
import org.apache.sshd.client.session.ClientSession;
import org.apache.sshd.common.keyprovider.FileKeyPairProvider;
import org.apache.sshd.common.keyprovider.KeyPairProvider;
import org.apache.sshd.sftp.client.SftpClientFactory;

final class ApacheMinaSftpClient implements SftpPklClient {

  private SshClient sshClient;
  private ClientSession session;
  private org.apache.sshd.sftp.client.SftpClient sftpClient;
  private String username;
  private String host;
  private int port = 22;
  KeyPairProvider keyPairProvider;
  private final int connectTimeout = 15000;

  ApacheMinaSftpClient(String username, String host, int port) throws IOException {
    this.username = username;
    this.host = host;
    this.port = port;
    sshClient = SshClient.setUpDefaultClient();
    sshClient.start();
    // Load keys
    // Load KeyPair
    Path keyPath = Paths.get(System.getProperty("user.home"), ".ssh", "id_rsa");
    // Open the file and print the key pair
    this.keyPairProvider = new FileKeyPairProvider(keyPath);
    sshClient.setKeyIdentityProvider(keyPairProvider);
  }

  @Override
  public void setUsername(String username) {
    if (username != null) {
      this.username = username;
      return;
    }
    throw new UnsupportedOperationException("Username cannot be null");
  }

  @Override
  public void setHost(String host) {
    if (host != null) {
      this.host = host;
      return;
    }
    throw new UnsupportedOperationException("Username cannot be null");
  }

  @Override
  public void setPort(int port) {
    this.port = port;
  }

  public void auth() throws IOException {
    session = sshClient.connect(username, host, port).verify(connectTimeout).getSession();
    session.auth().verify(connectTimeout);
  }

  @Override
  public void upload(Path localPath, String remotePath) throws IOException {
    throw new IOException("Not implemented");
  }

  @Override
  public byte[] download(String remotePath) throws IOException {

    this.auth();
    this.sftpClient = SftpClientFactory.instance().createSftpClient(session);
    try (var remoteInputStream = sftpClient.read(remotePath)) {
      var buffer = new ByteArrayOutputStream();
      remoteInputStream.transferTo(buffer);
      // session.close();
      return buffer.toByteArray();
      // Disconnect
    }
  }

  @Override
  public void close() {
    try {

      sftpClient.close();
      session.close();
      sshClient.stop();
    } catch (IOException e) {
      throw new RuntimeException("Failed to close SFTP client", e);
    }
  }
}
