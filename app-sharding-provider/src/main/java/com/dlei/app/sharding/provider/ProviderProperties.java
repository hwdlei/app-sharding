package com.dlei.app.sharding.provider;

import com.google.common.collect.Maps;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * @author donglei
 * @version 2023-01-10
 */
@Setter
@Getter
@ConfigurationProperties(prefix = "app.sharding.provider")
public class ProviderProperties {

    // or rpc
    private String protocol = "http";

    /**
     * 分片相关配置
     */
    private Zone zone = new Zone();

    @Data
    public static class Zone {
        /**
         * 分片数
         */
        private Integer count = 1;

        /**
         * 每个分片副本数
         */
        private Integer maxReplicaCount = 1;
    }
}
