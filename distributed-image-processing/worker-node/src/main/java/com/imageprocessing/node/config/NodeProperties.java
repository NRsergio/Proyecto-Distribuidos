package com.imageprocessing.node.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "node")
public class NodeProperties {
    private String id;
    private int grpcPort;
    private String appServerUrl;
    private String storagePath;
    private int threadPoolSize;
}
