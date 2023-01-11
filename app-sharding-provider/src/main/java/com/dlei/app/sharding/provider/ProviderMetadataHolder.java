package com.dlei.app.sharding.provider;

import lombok.Getter;
import lombok.Setter;

/**
 * @author donglei
 * @version 2023-01-10
 */
@Getter
@Setter
public class ProviderMetadataHolder {
    private volatile ProviderMetadata metadata;
}
