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

package org.joinfaces.docs.server.controller;

import org.joinfaces.docs.server.DocsServerProperties;
import org.joinfaces.docs.server.service.FilesService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.util.FileSystemUtils;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

@RestController
public class UploadController {

    @Autowired
    private DocsServerProperties docsServerProperties;

    @Autowired
    private FilesService filesService;

    @PreAuthorize("isAuthenticated()")
    @PutMapping("/api/{version}")
    public ResponseEntity<Void> uploadDocs(
            @PathVariable String version,
            @RequestParam(required = false) String path,
            InputStream inputStream
    ) throws IOException {

        HttpStatus status = HttpStatus.CREATED;

        File baseDir = resolveBaseDir(path);

        File dir = new File(baseDir, version);

        if (dir.isDirectory()) {
            FileSystemUtils.deleteRecursively(dir);
            status = HttpStatus.NO_CONTENT;
        }

        if (!dir.isDirectory() && !dir.mkdirs()) {
            throw new IOException("Failed to create directory " + dir);
        }

        filesService.extractZipStream(inputStream, dir);

        filesService.updateSymlinks(baseDir);

        ServletUriComponentsBuilder servletUriComponentsBuilder = ServletUriComponentsBuilder.fromCurrentContextPath();

        if (path != null) {
            servletUriComponentsBuilder.path(path);
        }
        servletUriComponentsBuilder.pathSegment(version);
        return ResponseEntity.status(status)
                .location(servletUriComponentsBuilder.build().toUri())
                .build();
    }

    private File resolveBaseDir(String path) {
        if (path == null) {
            return docsServerProperties.getBaseDir();
        }
        else {
            return new File(docsServerProperties.getBaseDir(), path);
        }
    }

}
