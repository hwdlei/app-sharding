package com.dlei.app.sharding.provider;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;

/**
 * @author donglei
 * @version 2023-01-11
 */
public class ProviderConfiguration {
    @Bean
    public ProviderAutoRegistry providerAutoRegistry() {
        return new ProviderAutoRegistry();
    }

    @Bean
    public ProviderMetadataHolder providerMetadataHolder() {
        return new ProviderMetadataHolder();
    }

    @Bean
    public ProviderApplicationListener providerApplicationListener(
            @Autowired ProviderAutoRegistry providerAutoRegistry) {
        return new ProviderApplicationListener(providerAutoRegistry);
    }
}
