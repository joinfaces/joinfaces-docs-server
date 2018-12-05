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

package org.joinfaces.docs.server;

import org.joinfaces.docs.server.service.FilesService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;

@RestController
@SpringBootApplication
@EnableConfigurationProperties(DocsServerProperties.class)
public class JoinfacesDocsServer {

    public static void main(String[] args) {
        SpringApplication.run(JoinfacesDocsServer.class, args);
    }

    @Autowired
    private DocsServerProperties docsServerProperties;

    @Autowired
    private FilesService filesService;

    @PutMapping("/{version}")
    public ResponseEntity<Void> uploadDocs(@PathVariable String version, InputStream inputStream) throws IOException {

        HttpStatus status = HttpStatus.CREATED;
        File baseDir = new File(docsServerProperties.getWebRoot(), version);

        if (baseDir.isDirectory()) {
            filesService.deleteDirectory(baseDir);
            status = HttpStatus.NO_CONTENT;
        }

        if (!baseDir.isDirectory() && !baseDir.mkdirs()) {
            throw new IOException("Failed to create directory " + baseDir);
        }

        filesService.extractZipStream(inputStream, baseDir);

        filesService.updateSymlinks(baseDir);

        return ResponseEntity.status(status)
                .location(URI.create("https://docs.joinfaces.org/" + version))
                .build();
    }

}
