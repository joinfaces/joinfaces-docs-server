/*
 * Copyright 2023 https://github.com/joinfaces
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.joinfaces.docs.server.model;

import lombok.Data;
import org.springframework.http.MediaType;
import org.springframework.util.MimeType;
import org.springframework.util.unit.DataSize;

import java.io.File;
import java.time.Instant;
import java.util.Comparator;

@Data
public class EntryInfo implements Comparable<EntryInfo> {

    private static final Comparator<EntryInfo> comparator = Comparator
            .comparing((EntryInfo ei) -> ei.getFile().isDirectory()).reversed()
            .thenComparing(ei -> ei.getFile().getName());


    private final File file;
    private final String basePath;
    private final MimeType mimeType;

    public String getName() {
        if (file.isDirectory()) {
            return file.getName() + "/";
        }
        return file.getName();
    }

    public Instant getLastModified() {
        return Instant.ofEpochMilli(file.lastModified());
    }

    public DataSize getSize() {
        if (file.isFile()) {
            return DataSize.ofBytes(file.length());
        }
        return null;
    }

    public boolean isFile() {
        return file.isFile();
    }

    public boolean isDirectory() {
        return file.isDirectory();
    }

    public String getFullPath() {
        return basePath + getName();
    }

    @Override
    public int compareTo(EntryInfo o) {
        return comparator.compare(this, o);
    }
}
