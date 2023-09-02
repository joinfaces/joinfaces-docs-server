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

package org.joinfaces.docs.server;

import org.springframework.format.Printer;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.util.unit.DataSize;

import java.util.Locale;

@Component
public class DataSizeFormatter implements Printer<DataSize> {

    @Override
    @NonNull
    public String print(DataSize object, Locale locale) {

        long bytes = object.toBytes();

        if (bytes < 1024) {
            return String.format(locale, "%d B", bytes);
        }

        double kib = bytes / 1024d;
        if (kib < 1024) {
            return String.format(locale, "%.2f KiB", kib);
        }

        double mib = kib / 1024d;
        if (mib < 1024) {
            return String.format(locale, "%.2f MiB", mib);
        }

        double gib = mib / 1024d;
        if (gib < 1024) {
            return String.format(locale, "%.2f GiB", gib);
        }

        double tib = gib / 1024d;
        if (tib < 1024) {
            return String.format(locale, "%.2f TiB", tib);
        }

        double pib = tib / 1024d;
        return String.format(locale, "%.2f PiB", pib);

    }
}
