package org.itxuexi.zk;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.recipes.cache.CuratorCache;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.itxuexi.pojo.netty.NettyServerNode;
import org.itxuexi.utils.JsonUtils;
import org.itxuexi.utils.RedisOperator;
import org.springframework.beans.factory.annotation.Autowired;
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
        // 注册监听事件
        addWatcher(PATH, client);

        return client;
    }

    @Autowired
    private RedisOperator redis;

    /**
     * 注册节点的事件监听
     * @param path
     * @param client
     */
    public void addWatcher(String path, CuratorFramework client) {

        CuratorCache curatorCache = CuratorCache.build(client, path);
        curatorCache.listenable().addListener((type, oldData, data) -> {
            // type: 当前监听到的事件类型
            // oldData: 节点更新前的数据、状态
            // data: 节点更新后的数据、状态

            System.out.println(type.name());

            // System.out.println("new path:" + data.getPath() + ",
            // new value:" + data.getData());

            //NODE_CREATED
            //NODE_CHANGED
            //NODE_DELETED

            switch (type.name()) {
                case "NODE_CREATED":
                    log.info("(子)节点创建");
                    break;
                case "NODE_CHANGED":
                    log.info("(子)节点数据变更");
                    break;
                case "NODE_DELETED":
                    log.info("(子)节点删除");

                    NettyServerNode oldNode = JsonUtils.jsonToPojo(new String(oldData.getData()),
                            NettyServerNode.class);

                    System.out.println("old path:" + oldData.getPath() + ", old value:" + oldNode);

                    String oldPort = oldNode.getPort() + "";
                    String portKey = "netty_port";
                    redis.hdel(portKey, oldPort);
                    break;
                default:
                    log.info("default");
                    break;
            }

        });

        curatorCache.start();
    }

}
