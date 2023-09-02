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

package org.joinfaces.docs.server.service;

import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class IconService {

    Map<MediaType, String> icons = Map.of(
            MediaType.APPLICATION_XML, "fa-file-code",
            MediaType.TEXT_HTML, "fa-file-code",
            MediaType.parseMediaType("application/*+xml"), "fa-file-code",
            MediaType.parseMediaType("application/java-archive"), "fa-file-zipper",
            MediaType.parseMediaType("audio/*"), "fa-file-audio",
            MediaType.parseMediaType("video/*"), "fa-file-video",
            MediaType.parseMediaType("image/*"), "fa-file-image",
            MediaType.parseMediaType("text/*"), "fa-file-lines"
    );

    public String getIconClass(MediaType mediaType) {

        for (Map.Entry<MediaType, String> mediaTypeStringEntry : icons.entrySet()) {
            if (mediaTypeStringEntry.getKey().includes(mediaType)) {
                return mediaTypeStringEntry.getValue();
            }
        }

        return "fa-file";
    }
}
