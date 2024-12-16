package org.itxuexi.netty.websocket;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler;
import io.netty.handler.stream.ChunkedWriteHandler;
import io.netty.handler.timeout.IdleStateHandler;

/**
 * 初始化器, channel注册后, 会执行里面的相应的初始化方法
 */
public class WSServerInitializer extends ChannelInitializer<SocketChannel> {
    @Override
    protected void initChannel(SocketChannel channel) throws Exception {

        // 通过SocketChannel获得对应的管道
        ChannelPipeline pipeline = channel.pipeline();

        /**
         * 通过管道,添加handler处理器
         */

        // HttpServerCodec 是由netty子集提供的助手类, 此处可以理解为管道中的拦截器
        // 当请求到服务端, 需要解码；响应客户端前, 需要编码
        // WebSocket 基于HTTP协议 , 故需要http的编解码器
        pipeline.addLast(new HttpServerCodec());

        // 添加对大数据流的支持
        pipeline.addLast(new ChunkedWriteHandler());

        // 【常用】对httpMessage进行聚合, 聚合成为FullHttpRequest或FullHttpResponse
        pipeline.addLast(new HttpObjectAggregator(1024 * 64));

        // ---------------以上是用于支持http协议相关的handler------------

        // =====================增加心跳支持 start ========================
        // 针对客户端， 如果在30分钟内没有向服务器发送读写心跳(ALL), 则主动断开连接
        // 如果是读空闲或者写空闲, 不做任何处理
        pipeline.addLast(new IdleStateHandler(
                8,
                10,
                30 * 60));

        pipeline.addLast(new HeartBeatHandler());

        // =====================增加心跳支持 end ========================

        // ---------------以下是用于支持websocket协议相关的handler------------
        /**
         * WebSocket 服务器处理的协议，用于指定给客户端连接的时候访问路由： /ws
         * 此Handler会处理一些比较复杂的繁重的操作，如：
         * handShaking（close,ping,pong）ping + pong = 心跳
         * 对于WebSocket来说，数据都是以frames进行传输的，不同的数据类型所对应的frames也都不同
         */
        pipeline.addLast(new WebSocketServerProtocolHandler("/ws"));

        // 添加自定义的助手类
        pipeline.addLast(new ChatHandler());
    }
}
