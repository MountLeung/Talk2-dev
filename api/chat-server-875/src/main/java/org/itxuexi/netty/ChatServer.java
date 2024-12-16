package org.itxuexi.netty;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import org.itxuexi.netty.websocket.WSServerInitializer;

/**
 * ChatServer: Netty 服务的启动类(服务器)
 */
public class ChatServer {

    public static final Integer nettyDefaultPort = 875;

    public static void main(String[] args) throws Exception {

        // 定义主从线程池
        // 定义主线程池, 用于接受客户端的连接
        EventLoopGroup bossGroup = new NioEventLoopGroup();
        // 定义从线程池, 处理主线程池交过来的任务
        EventLoopGroup workerGroup = new NioEventLoopGroup();

        try {
            // 构建Netty服务器
            ServerBootstrap server = new ServerBootstrap();     // 服务的启动类
            server.group(bossGroup, workerGroup)                // 将主从线程池放入启动类
                    .channel(NioServerSocketChannel.class)      // 设置Nio的双向通道
                    .childHandler(new WSServerInitializer()); // 设置处理器,用于处理workerGroup

            // 启动server, 并绑定端口号875, 同时启动方式为“同步"
            ChannelFuture channelFuture = server.bind(nettyDefaultPort).sync();

            //监听关闭的channel
            channelFuture.channel().closeFuture().sync();
        } finally {
            // 优雅地关闭线程池
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
    }
}
