/*
 * Copyright 2018 https://github.com/joinfaces
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

package org.joinfaces.docs.server.service;

import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

@Service
public class FilesService {

    public void deleteDirectory(File dir) throws IOException {
        Files.walkFileTree(dir.toPath(), new SimpleFileVisitor<>() {
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                Files.delete(file);
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                Files.delete(dir);
                return FileVisitResult.CONTINUE;
            }
        });
    }

    public void extractZipStream(InputStream inputStream, File destinationDir) throws IOException {
        try (ZipInputStream zipInputStream = new ZipInputStream(inputStream)) {

            ZipEntry entry;

            while ((entry = zipInputStream.getNextEntry()) != null) {

                File file = new File(destinationDir, entry.getName());

                if (entry.isDirectory()) {
                    if (!file.isDirectory() && !file.mkdirs()) {
                        throw new IOException("Unable to create directory " + file);
                    }
                } else {
                    File dir = file.getParentFile();
                    if (dir.isDirectory() || dir.mkdirs()) {
                        Files.copy(zipInputStream, file.toPath());
                    } else {
                        throw new IOException("Unable to create file " + file);
                    }
                }

                file.setLastModified(entry.getTime());

                zipInputStream.closeEntry();
            }
        }
    }

}
