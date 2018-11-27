package org.joinfaces.docs.server;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.io.File;

@Data
@ConfigurationProperties("joinfaces-docs")
public class DocsServerProperties {

    private File webRoot;
}
