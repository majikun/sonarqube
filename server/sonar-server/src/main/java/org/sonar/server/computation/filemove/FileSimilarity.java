/*
 * SonarQube
 * Copyright (C) 2009-2016 SonarSource SA
 * mailto:contact AT sonarsource DOT com
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package org.sonar.server.computation.filemove;

import java.util.List;
import javax.annotation.CheckForNull;
import javax.annotation.Nullable;

public interface FileSimilarity {

  final class File {
    private final String key;
    private final List<String> lineHashes;

    public File(String key, @Nullable List<String> lineHashes) {
      this.key = key;
      this.lineHashes = lineHashes;
    }

    public String getKey() {
      return key;
    }

    @CheckForNull
    public List<String> getLineHashes() {
      return lineHashes;
    }
  }

  int score(File file1, File file2);
}
