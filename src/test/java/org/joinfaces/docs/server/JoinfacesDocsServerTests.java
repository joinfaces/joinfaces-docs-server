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

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.context.ApplicationContext;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.nio.charset.Charset;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class JoinfacesDocsServerTests {

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private ApplicationContext applicationContext;

    @Autowired
    private DocsServerProperties docsServerProperties;

    @Test
    public void contextLoads() {
        assertThat(applicationContext).isNotNull();
    }

    @Test
    public void testUpload_simple() throws IOException {

        restTemplate = restTemplate.withBasicAuth("user", "password");

        File destDir = new File(docsServerProperties.getBaseDir(), "1.2.3");

        assertThat(destDir).doesNotExist();

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ZipOutputStream outputStream = new ZipOutputStream(out);

        outputStream.putNextEntry(new ZipEntry("bar.txt"));
        outputStream.write("Hello World".getBytes(Charset.defaultCharset()));
        outputStream.closeEntry();
        outputStream.close();

        new HttpEntity<>(out.toByteArray());

        ResponseEntity<Void> response = restTemplate.exchange("/1.2.3", HttpMethod.PUT, new HttpEntity<>(out.toByteArray()), void.class);

        URI location = response.getHeaders().getLocation();
        assertThat(location).hasPath("/1.2.3");

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);

        assertThat(destDir).isDirectory();
        assertThat(new File(destDir, "bar.txt")).isFile();

        assertThat(new File(docsServerProperties.getBaseDir(), "current")).isDirectory();
    }

    @Test
    public void testUpload_withPath() throws IOException {

        restTemplate = restTemplate.withBasicAuth("user", "password");

        File destDir = new File(docsServerProperties.getBaseDir(), "foo/1.2.3");

        assertThat(destDir).doesNotExist();

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ZipOutputStream outputStream = new ZipOutputStream(out);

        outputStream.putNextEntry(new ZipEntry("bar.txt"));
        outputStream.write("Hello World".getBytes(Charset.defaultCharset()));
        outputStream.closeEntry();
        outputStream.close();

        new HttpEntity<>(out.toByteArray());

        ResponseEntity<Void> response = restTemplate.exchange("/1.2.3?path=foo", HttpMethod.PUT, new HttpEntity<>(out.toByteArray()), void.class);

        URI location = response.getHeaders().getLocation();
        assertThat(location).hasPath("/foo/1.2.3");

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);

        assertThat(destDir).isDirectory();
        assertThat(new File(destDir, "bar.txt")).isFile();

        assertThat(new File(docsServerProperties.getBaseDir(), "foo/current")).isDirectory();
    }

}
