package org.itxuexi.netty;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import org.itxuexi.netty.utils.JedisPoolUtils;
import org.itxuexi.netty.websocket.WSServerInitializer;
import redis.clients.jedis.Jedis;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * ChatServer: Netty 服务的启动类(服务器)
 */
public class ChatServer2 {

    public static final Integer nettyDefaultPort = 875;
    public static final String INIT = "0";

    public static Integer selectPort(Integer port) {
        String portKey = "netty_port";
        Jedis jedis = JedisPoolUtils.getJedis();

        Map<String, String> portMap = jedis.hgetAll(portKey);

        // 转换key为整数
        List<Integer> portList = portMap.keySet().stream().map(
                Integer::valueOf).toList();

        System.out.println(portList);
        Integer nettyPort = null;
        if (portList == null || portList.isEmpty()) {
            jedis.hset(portKey, port+"", INIT);
            nettyPort = port;
        } else {
            Optional<Integer> maxInteger = portList.stream().max(Integer::compareTo);
            int maxPort = maxInteger.get().intValue();
            Integer curr = maxPort + 10;
            jedis.hset(portKey, curr+"", INIT);
            nettyPort = curr;
        }
        return nettyPort;
    }

    public static void main(String[] args) throws Exception {

        // 定义主从线程池
        // 定义主线程池, 用于接受客户端的连接
        EventLoopGroup bossGroup = new NioEventLoopGroup();
        // 定义从线程池, 处理主线程池交过来的任务
        EventLoopGroup workerGroup = new NioEventLoopGroup();

        // Netty服务启动时, 从redis中查找有没有端口,
        // 无：使用875； 有：累加10
        Integer nettyPort = selectPort(nettyDefaultPort);

        try {
            // 构建Netty服务器
            ServerBootstrap server = new ServerBootstrap();     // 服务的启动类
            server.group(bossGroup, workerGroup)                // 将主从线程池放入启动类
                    .channel(NioServerSocketChannel.class)      // 设置Nio的双向通道
                    .childHandler(new WSServerInitializer()); // 设置处理器,用于处理workerGroup

            // 启动server, 并绑定端口号875, 同时启动方式为“同步"
            ChannelFuture channelFuture = server.bind(nettyPort).sync();

            //监听关闭的channel
            channelFuture.channel().closeFuture().sync();
        } finally {
            // 优雅地关闭线程池
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
    }
}
