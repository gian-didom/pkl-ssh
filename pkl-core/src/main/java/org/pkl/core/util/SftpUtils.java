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
package org.pkl.core.util;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;
import org.pkl.core.PklBugException;

public final class SftpUtils {
  private SftpUtils() {}

  public static boolean isSshUrl(URL url) {
    var protocol = url.getProtocol();
    return "sftp".equalsIgnoreCase(protocol);
  }

  public static boolean isSshUrl(URI uri) {
    var protocol = uri.getScheme();
    return "sftp".equalsIgnoreCase(protocol);
  }

  public static boolean validateUri(URI uri) {
    if (!isSshUrl(uri)) {
      return false;
    }
    String userInfo = uri.getUserInfo();
    if (userInfo == null) {
      return false;
    }
    String[] userParts = userInfo.split("@");
    if (userParts.length != 2) {
      return false;
    }
    String username = userParts[0];
    String host = uri.getHost();
    if (host == null) {
      return false;
    }
    int port = uri.getPort();
    if (port < 0 || port > 65535) {
      return false;
    }
    return true;
  }

  public static URI setPort(URI uri, int port) {
    if (port < 0 || port > 65535) {
      throw new IllegalArgumentException(String.valueOf(port));
    }
    try {
      return new URI(
          uri.getScheme(),
          uri.getUserInfo(),
          uri.getHost(),
          port,
          uri.getPath(),
          uri.getQuery(),
          uri.getFragment());
    } catch (URISyntaxException e) {
      throw PklBugException.unreachableCode(); // only port changed
    }
  }

  public static Path getSshHomeDir() {
    return Path.of(System.getProperty("user.home"), ".ssh");
  }

  private static final String DEFAULT_KEY_PATH = System.getProperty("user.home") + "/.ssh/id_rsa";

  public static String getPrivateKeyPath() {
    return DEFAULT_KEY_PATH;
  }

  public static String readPrivateKey(String privateKeyPath) throws IOException {
    StringBuilder privateKeyContent = new StringBuilder();
    try (BufferedReader reader = new BufferedReader(new FileReader(privateKeyPath))) {
      String line;
      while ((line = reader.readLine()) != null) {
        privateKeyContent.append(line).append("\n");
      }
    }
    return privateKeyContent.toString();
  }
}
