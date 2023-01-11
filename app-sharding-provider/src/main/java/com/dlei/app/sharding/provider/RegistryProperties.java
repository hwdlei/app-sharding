package com.dlei.app.sharding.provider;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * @author donglei
 * @version 2023-01-10
 */
@Getter
@Setter
@ConfigurationProperties(prefix = "app.sharding.registry")
public class RegistryProperties {

    private Zookeeper zookeeper = new Zookeeper();

    @Getter
    @Setter
    public static class Zookeeper {
        private String namespace = "app-sharding";
        private String group = "default";
        private String address = "127.0.0.1:2181";
        private int sessionTimeoutMs = 3000;
        private int connectionTimeoutMs = 3000;
        private int retryCount = 3;
        private int elapsedTimeMs = 3000;
    }
}
