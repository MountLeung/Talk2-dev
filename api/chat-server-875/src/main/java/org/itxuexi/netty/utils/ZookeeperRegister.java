package org.itxuexi.netty.utils;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.locks.InterProcessReadWriteLock;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.data.Stat;
import org.itxuexi.pojo.netty.NettyServerNode;
import org.itxuexi.utils.JsonUtils;

import java.net.InetAddress;
import java.util.List;

public class ZookeeperRegister {

    public static void registerNettyServer(String nodeName,
                                           String ip,
                                           Integer port) throws Exception {
        CuratorFramework zkClient = CuratorConfig.getClient();
        String path = "/" + nodeName;
        Stat stat = zkClient.checkExists().forPath(path);
        if (stat == null) {
            zkClient.create()
                    .creatingParentContainersIfNeeded()
                    .withMode(CreateMode.PERSISTENT)
                    .forPath(path);
        } else {
            System.out.println(stat);
        }

        // 创建对应的临时结点, 值为在线人数, 初始化为0
        NettyServerNode serverNode = new NettyServerNode();
        serverNode.setIp(ip);
        serverNode.setPort(port);
        String nodeJson = JsonUtils.objectToJson(serverNode);

        zkClient.create()
                .withMode(CreateMode.EPHEMERAL_SEQUENTIAL)
                .forPath(path + "/im-", nodeJson.getBytes());

    }

    public static String getLocalIP() throws Exception{
//        InetAddress address = InetAddress.getLocalHost();
//        String ip = address.getHostAddress();
//        System.out.println("本机IP地址：" + ip);
//        return ip;
        return "192.168.1.3";
    }

    public static void incrementOnlineCounts(NettyServerNode serverNode) throws Exception {
        dealOnlineCounts(serverNode, 1);
    }

    public static void decrementOnlineCounts(NettyServerNode serverNode) throws Exception {
        dealOnlineCounts(serverNode, -1);
    }

    private static void dealOnlineCounts (NettyServerNode serverNode,
                                          Integer delta) throws Exception{
        CuratorFramework zkClient = CuratorConfig.getClient();

        InterProcessReadWriteLock readWriteLock =
                new InterProcessReadWriteLock(zkClient, "/rw-lock");

        readWriteLock.writeLock().acquire();

        String path = "/server-list";
        List<String> list = zkClient.getChildren().forPath(path);

        try {
            for (String node : list) {
                String nodePath = path + "/" + node;
                String nodeValue = new String(zkClient.getData().forPath(nodePath));
                NettyServerNode pendingNode = JsonUtils.jsonToPojo(nodeValue,
                        NettyServerNode.class);

                if (pendingNode.getIp().equals(serverNode.getIp()) &&
                        (pendingNode.getPort().intValue() == serverNode.getPort().intValue()))
                {
                    pendingNode.setOnlineCounts(pendingNode.getOnlineCounts() + delta);
                    String nodeJson = JsonUtils.objectToJson(pendingNode);
                    zkClient.setData().forPath(nodePath, nodeJson.getBytes());

                }
            }
        } finally {
            readWriteLock.writeLock().release();
        }

    }

}
