package com.dlei.app.sharding.provider;

import lombok.AllArgsConstructor;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;

import javax.annotation.Nonnull;

/**
 * @author donglei
 * @version 2023-01-11
 */
@AllArgsConstructor
public class ProviderApplicationListener implements ApplicationListener<ApplicationEvent> {
    private ProviderAutoRegistry providerAutoRegistry;

    @Override
    public void onApplicationEvent(@Nonnull ApplicationEvent applicationEvent) {
        if (applicationEvent instanceof ApplicationReadyEvent) {
            try {
                // 服务就绪之后改为发布状态
                providerAutoRegistry.publish();
            } catch (Exception e) {
                throw new RuntimeException( "provider publish failed", e);
            }
        }
    }
}
