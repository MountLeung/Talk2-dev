package org.itxuexi.zk;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@Data
@ConfigurationProperties(prefix = "zookeeper.curator")
public class CuratorConfig {

    private String host;                        // 单机/集群的ip:port地址
    private Integer connectionTimeoutMs;        // 连接超时时间
    private Integer sessionTimeoutMs;           // 会话超时时间
    private Integer sleepMsBetweenRetry;        // 每次重试的间隔时间
    private Integer maxRetries;                 // 最大重试次数
    private String namespace;                   // 命名空间（root根节点名称）

    public static final String PATH = "/server-list";

    @Bean("curatorClient")
    public CuratorFramework curatorClient(){
        RetryPolicy backoffRetry = new ExponentialBackoffRetry(sleepMsBetweenRetry, maxRetries);
        // 声明初始化客户端
        CuratorFramework client = CuratorFrameworkFactory.builder()
                .connectString(host)
                .connectionTimeoutMs(connectionTimeoutMs)
                .sessionTimeoutMs(sessionTimeoutMs)
                .retryPolicy(backoffRetry)
                .namespace(namespace)
                .build();

        // 启动curator客户端
        client.start();
//        // 注册监听事件
//        addWatcher(PATH, client);

        return client;
    }
}
