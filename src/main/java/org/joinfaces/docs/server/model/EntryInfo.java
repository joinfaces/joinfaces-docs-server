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

    public String getSize() {
        if (file.isDirectory()) {
            return "-";
        }

        if (file.isFile()) {

            double bytes = file.length();

            if (bytes < 1024) {
                return String.format("%.2f B", bytes);
            }

            double kib = bytes / 1024d;
            if (kib < 1024) {
                return String.format("%.2f KiB", kib);
            }

            double mib = kib / 1024d;
            if (mib < 1024) {
                return String.format("%.2f MiB", mib);
            }

            double gib = mib / 1024d;
            if (gib < 1024) {
                return String.format("%.2f GiB", gib);
            }

            double tib = gib / 1024d;
            return String.format("%.2f TiB", tib);

        }
        return null;
    }

    public String getFullPath() {
        return basePath + getName();
    }

    public String getIconClass() {
        if (file.isDirectory()) {
            return "fa-regular fa-folder";
        }

        if (file.isFile()) {

            if (mimeType != null) {

                if (mimeType.isCompatibleWith(MediaType.TEXT_HTML)) {
                    return "fa-regular fa-file-code";
                }

                if (mimeType.isCompatibleWith(MediaType.APPLICATION_XML)) {
                    return "fa-regular fa-file-code";
                }

                if (mimeType.toString().equals("application/java-archive")) {
                    return "fa-regular fa-file-zipper";
                }

                if (mimeType.getType().equals("image")) {
                    return "fa-regular fa-file-image";
                }

                if (mimeType.getType().equals("text")) {
                    return "fa-regular fa-file-text";
                }
            }

            if (file.getName().endsWith(".zip")) {
                return "fa-regular fa-file-zipper";
            }

            return "fa-regular fa-file";
        }

        return null;
    }

    @Override
    public int compareTo(EntryInfo o) {
        return comparator.compare(this, o);
    }
}
