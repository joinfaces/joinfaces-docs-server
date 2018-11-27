package org.joinfaces.docs.server;

import org.joinfaces.docs.server.service.FilesService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

@RestController
@RequestMapping("/api")
public class ApiController {

    @Autowired
    private DocsServerProperties docsServerProperties;

    @Autowired
    private FilesService filesService;

    @PutMapping("/{version}")
    public ResponseEntity<Void> uploadDocs(InputStream inputStream, @PathVariable String version) throws IOException {

        HttpStatus status = HttpStatus.CREATED;
        File baseDir = new File(docsServerProperties.getWebRoot(), version);

        if (baseDir.isDirectory()) {
            filesService.deleteDirectory(baseDir);
            status = HttpStatus.NO_CONTENT;
        }

        if (!baseDir.isDirectory() && !baseDir.mkdirs()) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }

        try (ZipInputStream zipInputStream = new ZipInputStream(inputStream)) {

            ZipEntry entry;

            while ((entry = zipInputStream.getNextEntry()) != null) {

                File file = new File(baseDir, entry.getName());

                if (entry.isDirectory()) {
                    if (!file.isDirectory() && !file.mkdirs()) {
                        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
                    }
                } else {
                    File dir = file.getParentFile();
                    if (dir.isDirectory() || dir.mkdirs()) {
                        Files.copy(zipInputStream, file.toPath());
                    } else {
                        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
                    }
                }

                file.setLastModified(entry.getTime());

                zipInputStream.closeEntry();
            }
        }

        return ResponseEntity.status(status)
                .lastModified(baseDir.lastModified())
                .build();
    }
}