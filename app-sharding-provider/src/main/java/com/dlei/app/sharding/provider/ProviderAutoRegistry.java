package com.dlei.app.sharding.provider;

import com.alibaba.fastjson2.JSON;
import com.dlei.app.sharding.provider.utils.NetworkUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.RetryNTimes;
import org.apache.zookeeper.CreateMode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.nio.charset.Charset;

/**
 * @author donglei
 * @version 2023-01-10
 */
@Slf4j
@Component
public class ProviderAutoRegistry {

    @Autowired
    private RegistryProperties registryProperties;

    @Autowired
    private ProviderProperties providerProperties;

    @Autowired
    private ProviderMetadataHolder providerMetadataHolder;

    @Value("${server.port}")
    private String serverPort;

    private CuratorFramework curatorFramework;

    @PostConstruct
    public void init() {
        String host = NetworkUtils.getLocalHost();
        Integer port = Integer.parseInt(serverPort);

        RegistryProperties.Zookeeper zookeeper = registryProperties.getZookeeper();
        ProviderProperties.Zone zone = providerProperties.getZone();

        curatorFramework = CuratorFrameworkFactory.builder().connectString(zookeeper.getAddress())
                .namespace(zookeeper.getNamespace() + "/" + zookeeper.getGroup())
                .sessionTimeoutMs(zookeeper.getSessionTimeoutMs())
                .connectionTimeoutMs(zookeeper.getConnectionTimeoutMs())
                .retryPolicy(new RetryNTimes(zookeeper.getRetryCount(), zookeeper.getElapsedTimeMs()))
                .build();
        curatorFramework.getConnectionStateListenable().addListener((client, newState) -> {
            log.info("zookeeper connection state is {}", newState);
            switch (newState) {
                case CONNECTED:
                case RECONNECTED: {
                    ProviderMetadata providerMetadata;
                    if (providerMetadataHolder.getMetadata() == null) {
                        providerMetadata = contendZone(zone,
                                ProviderMetadata.builder().host(host).port(port)
                                        .protocol(providerProperties.getProtocol())
                                        .status(ProviderMetadata.Status.INIT.getCode())
                                        .build());
                    } else {
                        // providerMetadataHolder.getMetadata()?????????????????????????????????????????????????????????????????????
                        providerMetadata = reconnect(providerMetadataHolder.getMetadata())
                                ? providerMetadataHolder.getMetadata()
                                : contendZone(zone, providerMetadataHolder.getMetadata());
                    }
                    if (providerMetadata != null) {
                        // ?????????????????????????????????,???????????????????????????????????????????????????????????????????????????
                        providerMetadataHolder.setMetadata(providerMetadata);
                    } else {
                        throw new RuntimeException("contend zone failed");
                    }
                    break;
                }
                case LOST:
                case SUSPENDED:
                case READ_ONLY:
                default:
                    break;
            }
        });
        curatorFramework.start();
    }

    /**
     * ???????????????????????????????????????????????????????????????????????????????????????????????????
     */
    public void publish() {
        ProviderMetadata metadata = providerMetadataHolder.getMetadata();
        metadata.setStatus(ProviderMetadata.Status.PUBLISHED.getCode());
        try {
            log.info("publish provider {}", metadata);
            String zonePath = "/zones/" + metadata.getShard() + "/" + metadata.getReplica();
            curatorFramework.setData().forPath(zonePath, JSON.toJSONBytes(metadata));
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
    }

    /**
     * ???????????? <br/>
     *
     * ??????????????? ?????????A,B,C???????????????????????????????????????????????????????????? A1 -> B1 -> C1 -> A2 -> B2 -> C2
     * @param zone
     * @param providerMetadata
     * @return
     */
    private ProviderMetadata contendZone(ProviderProperties.Zone zone, ProviderMetadata providerMetadata) {
        int maxCount =  zone.getMaxReplicaCount();
        Integer currentShard = providerMetadata.getShard();
        for (int replica = 0; replica < maxCount; replica++) {
            for (int shard = 0; shard < zone.getCount(); shard++) {
                if (currentShard != null && currentShard != shard) {
                    // ??????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????
                    log.warn("current hold shard[{}], skip to contend shard[{}]!", currentShard, shard);
                    continue;
                }
                String zonePath = "/zones/" + shard + "/" + replica;
                providerMetadata.setShard(shard);
                providerMetadata.setShardCount(zone.getCount());
                providerMetadata.setReplica(replica);
                try {
                    String result = curatorFramework.create().creatingParentsIfNeeded().withMode(CreateMode.EPHEMERAL)
                            .forPath(zonePath, JSON.toJSONBytes(providerMetadata));
                    log.info("contend success, zone path is {}, metadata is {}", result, providerMetadata);
                    return providerMetadata;
                } catch (Exception e) {
                    log.error(e.getMessage(), e);
                }
            }
        }
        return null;
    }

    private boolean reconnect(ProviderMetadata providerMetadata) {
        Integer currentShard = providerMetadata.getShard();
        Integer currentReplica = providerMetadata.getReplica();
        if (currentShard == null || currentReplica == null) {
            // ???????????????????????????????????????
            log.warn("currentShard or currentReplica is null, providerMetadata is {}", providerMetadata);
            return false;
        }
        String zonePath = "/zones/" + currentShard + "/" + currentReplica;
        log.info("reconnect to {}", zonePath);
        try {
            if (curatorFramework.checkExists().creatingParentContainersIfNeeded().forPath(zonePath) == null) {
                log.info("node[{}] is empty, create new EPHEMERAL node, data is {}", zonePath, providerMetadata);
                curatorFramework.create().creatingParentsIfNeeded().withMode(CreateMode.EPHEMERAL).forPath(zonePath,
                        JSON.toJSONBytes(providerMetadata));
                return true;
            } else {
                // ??????????????????????????????
                ProviderMetadata currentProviderMetadataInRegistry = JSON.parseObject(
                        new String(curatorFramework.getData().forPath(zonePath), Charset.defaultCharset()),
                        ProviderMetadata.class);
                boolean currentEqualsDataInRegistry = currentProviderMetadataInRegistry.getProtocol()
                        .equals(providerMetadata.getProtocol())
                        && currentProviderMetadataInRegistry.getHost().equals(providerMetadata.getHost())
                        && currentProviderMetadataInRegistry.getPort().equals(providerMetadata.getPort());
                if (currentEqualsDataInRegistry) {
                    log.info("current metadata equals metadata in registry, cover node[{}], data is {}", zonePath,
                            providerMetadata);
                    curatorFramework.delete().forPath(zonePath);
                    curatorFramework.create().creatingParentsIfNeeded().withMode(CreateMode.EPHEMERAL).forPath(zonePath,
                            JSON.toJSONBytes(providerMetadata));
                    return true;
                }
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        return false;
    }

}
