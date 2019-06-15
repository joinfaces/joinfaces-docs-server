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

import lombok.SneakyThrows;
import lombok.Value;
import lombok.extern.slf4j.Slf4j;
import org.joinfaces.docs.server.DocsServerProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Arrays;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

@Slf4j
@Service
public class FilesService {

    @Autowired
    private DocsServerProperties docsServerProperties;

    public static final Comparator<Version> VERSION_COMPARATOR = Comparator.comparing(Version::getMajor)
            .thenComparing(Version::getMinor)
            .thenComparing(Version::getPatch);

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
                }
                else {
                    File dir = file.getParentFile();
                    if (dir.isDirectory() || dir.mkdirs()) {
                        Files.copy(zipInputStream, file.toPath());
                    }
                    else {
                        throw new IOException("Unable to create file " + file);
                    }
                }

                file.setLastModified(entry.getTime());

                zipInputStream.closeEntry();
            }
        }
    }

    public void updateSymlinks() {

        Pattern versionPattern = Pattern.compile("(\\d+)\\.(\\d+)\\.(\\d+)");

        LinkedList<Version> versions = new LinkedList<>();

        File[] files = docsServerProperties.getBaseDir().listFiles();
        log.info("Updating symlinks for {}", Arrays.toString(files));

        for (File file : files) {
            if (file.isDirectory()) {
                Matcher matcher = versionPattern.matcher(file.getName());
                if (matcher.matches()) {
                    int major = Integer.parseInt(matcher.group(1));
                    int minor = Integer.parseInt(matcher.group(2));
                    int patch = Integer.parseInt(matcher.group(3));
                    versions.add(new Version(major, minor, patch, file));
                }
            }
        }

        versions.sort(VERSION_COMPARATOR);
        if (!versions.isEmpty()) {
            setSymlink("current", versions.getLast().getFile());
        }

        versions.stream()
                .collect(Collectors.groupingBy(Version::getMajor))
                .forEach((majorVersion, minorVersions) -> {
                    minorVersions.sort(VERSION_COMPARATOR);

                    Version latestMinorVersion = minorVersions.get(minorVersions.size() - 1);
                    setSymlink(String.format("%d.x", majorVersion), latestMinorVersion.getFile());

                    minorVersions.stream()
                            .collect(Collectors.groupingBy(Version::getMinor))
                            .forEach((minorVersion, patchVersions) -> {
                                patchVersions.sort(VERSION_COMPARATOR);

                                Version latestPatchVersion = patchVersions.get(patchVersions.size() - 1);
                                setSymlink(String.format("%d.%d.x", majorVersion, minorVersion), latestPatchVersion.getFile());
                            });
                });
    }

    @SneakyThrows
    private void setSymlink(String name, File target) {
        log.info("Linking {} -> {}", name, target);
        Path link = new File(docsServerProperties.getBaseDir(), name).toPath();

        Files.deleteIfExists(link);

        Files.createSymbolicLink(link, target.toPath());
    }

    @Value
    private static class Version implements Comparable<Version> {
        private int major;
        private int minor;
        private int patch;
        private File file;

        @Override
        public int compareTo(Version o) {
            return VERSION_COMPARATOR
                    .compare(this, o);
        }
    }

}
