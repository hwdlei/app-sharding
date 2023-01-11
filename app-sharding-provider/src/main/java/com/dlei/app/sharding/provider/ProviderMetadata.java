package com.dlei.app.sharding.provider;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

/**
 * @author donglei
 * @version 2023-01-10
 */
@Getter
@Setter
@Builder
public class ProviderMetadata {
    private String protocol;
    private String host;
    private Integer port;
    private Integer shardCount;
    private Integer shard;
    private Integer replica;
    private Integer status;

    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    public enum Status {
        /**
         * 初始化
         */
        INIT(1),
        /**
         * 已发布
         */
        PUBLISHED(2);

        @Getter
        private int code;
    }
}
