package com.dlei.sharding.test;

import com.dlei.app.sharding.provider.EnableShardingProvider;
import com.dlei.app.sharding.provider.ProviderProperties;
import com.dlei.app.sharding.provider.RegistryProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

/**
 * @author donglei
 * @version 2023-01-11
 */
@SpringBootApplication
@EnableShardingProvider
@EnableConfigurationProperties({RegistryProperties.class, ProviderProperties.class})
public class ProviderApplication {
    public static void main(String[] args) {
        SpringApplication.run(ProviderApplication.class, args);
    }
}
