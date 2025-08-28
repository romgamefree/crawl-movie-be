package com.devteria.identityservice.configuration;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "m3u8")
@Getter
@Setter
public class M3u8Properties {
    private String playlistBaseUrl;
}
