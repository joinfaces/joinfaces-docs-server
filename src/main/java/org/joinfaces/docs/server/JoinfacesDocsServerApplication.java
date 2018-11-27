package org.joinfaces.docs.server;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties(DocsServerProperties.class)
public class JoinfacesDocsServerApplication {

    public static void main(String[] args) {
        SpringApplication.run(JoinfacesDocsServerApplication.class, args);
    }
}
