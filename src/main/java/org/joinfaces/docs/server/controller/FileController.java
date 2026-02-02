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

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.joinfaces.docs.server.DocsServerProperties;
import org.joinfaces.docs.server.model.EntryInfo;
import org.joinfaces.docs.server.service.MimeTypeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.*;
import org.springframework.stereotype.Controller;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.RedirectView;

import java.io.File;
import java.io.IOException;
import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Controller
public class FileController {

    @Autowired
    private DocsServerProperties docsServerProperties;

    @Autowired
    private MimeTypeService mimeTypeService;

    private String baseDirPath;

    private CacheControl symlinkCacheControl;

    private CacheControl nonSymlinkCacheControl;

    @PostConstruct
    void init() throws IOException {
        baseDirPath = docsServerProperties.getBaseDir().getCanonicalPath();

        symlinkCacheControl = CacheControl.noCache()
                .mustRevalidate()
                .staleIfError(Duration.ofMinutes(30))
                .staleWhileRevalidate(Duration.ofMinutes(1));

        nonSymlinkCacheControl = CacheControl.maxAge(Duration.ofDays(1))
                .sMaxAge(Duration.ofDays(1))
                .cachePublic();
    }

    @GetMapping("/{*path}")
    public Object process(@PathVariable String path, WebRequest webRequest) throws IOException {
        File file = new File(baseDirPath, path);

        if (!file.getCanonicalPath().startsWith(baseDirPath)) {
            return ResponseEntity.badRequest().build();
        }

        if (!file.exists()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }

        if (file.isFile()) {
            return fileResponse(webRequest, file);
        }

        if (file.isDirectory()) {
            return directoryResponse(webRequest, path, file);
        }

        return null;
    }

    private Object directoryResponse(WebRequest webRequest, String path, File directory) throws IOException {
        Assert.state(directory.isDirectory(), "directory is not a directory");
        if (!path.endsWith("/")) {
            return new RedirectView(path + "/", true);
        }

        File[] children = directory.listFiles();

        Optional<File> indexPage = Arrays.stream(children)
                .filter(File::isFile)
                .filter(file -> file.getName().equals("index.html"))
                .findFirst();

        if (indexPage.isPresent()) {
            return fileResponse(webRequest, indexPage.get());
        }

        ModelAndView modelAndView = new ModelAndView("directory-index");
        modelAndView.addObject("path", path);
        List<EntryInfo> entryInfos = Arrays.stream(children)
                .filter(file -> !file.isHidden())
                .map(file -> new EntryInfo(file, path, mimeTypeService.guessMimeType(file)))
                .sorted()
                .collect(Collectors.toList());
        modelAndView.addObject("entries", entryInfos);
        return modelAndView;
    }

    private ResponseEntity<FileSystemResource> fileResponse(WebRequest webRequest, File file) throws IOException {
        Assert.state(file.isFile(), "file is not a file");
        if (webRequest.checkNotModified(file.lastModified())) {
            return null;
        }

        MediaType mediaType = mimeTypeService.guessMimeType(file);

        if (mediaType == null) {
            mediaType = MediaType.TEXT_PLAIN;
        }

        CacheControl cacheControl;
        if (file.getAbsolutePath().equals(file.getCanonicalPath())) {
            cacheControl = nonSymlinkCacheControl;
        } else {
            cacheControl = symlinkCacheControl;
        }

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, ContentDisposition.inline().filename(file.getName()).build().toString())
                .lastModified(file.lastModified())
                .contentType(mediaType)
                .cacheControl(cacheControl)
                .body(new FileSystemResource(file));
    }

}
