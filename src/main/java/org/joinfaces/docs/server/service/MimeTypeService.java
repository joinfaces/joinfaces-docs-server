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

import lombok.SneakyThrows;
import org.apache.tika.Tika;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;

import java.io.File;

@Service
public class MimeTypeService {

    @Autowired
    private Tika tika;

    @Nullable
    @SneakyThrows
    public MediaType guessMimeType(File file) {

        if (!file.isFile()) {
            return null;
        }

        String mimeType = tika.detect(file);

        if (mimeType != null) {
            return MediaType.parseMediaType(mimeType);
        }

        return null;
    }
}
