package org.itxuexi.netty.websocket;

import com.a3test.component.idworker.IdWorkerConfigBean;
import com.a3test.component.idworker.Snowflake;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.util.concurrent.GlobalEventExecutor;
import org.itxuexi.enums.MsgTypeEnum;
import org.itxuexi.grace.result.GraceJSONResult;
import org.itxuexi.netty.mq.MessagePublisher;
import org.itxuexi.netty.utils.JedisPoolUtils;
import org.itxuexi.netty.utils.ZookeeperRegister;
import org.itxuexi.pojo.netty.ChatMsg;
import org.itxuexi.pojo.netty.DataContent;
import org.itxuexi.pojo.netty.NettyServerNode;
import org.itxuexi.utils.JsonUtils;
import org.itxuexi.utils.LocalDateUtils;
import org.itxuexi.utils.OkHttpUtil;
import redis.clients.jedis.Jedis;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 创建的自定义助手类
 */
// SimpleChannelInboundHandler: 请求入站(入境)
// TextWebSocketFrame : 为websocket处理而封装的文本数据对象，Frame是数据(消息)的载体
public class ChatHandler_Single extends SimpleChannelInboundHandler<TextWebSocketFrame> {

    public static ChannelGroup clients =
            new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);

    @Override
    protected void channelRead0(ChannelHandlerContext ctx,
                                TextWebSocketFrame msg) throws Exception {

        // 1. 获得客户端传输过来的消息并且解析
        String content = msg.text();
        System.out.println("接收到的数据" + content);

        DataContent dataContent = JsonUtils.jsonToPojo(content, DataContent.class);
        ChatMsg chatMsg = dataContent.getChatMsg();

        String msgText = chatMsg.getMsg();
        String receiverId = chatMsg.getReceiverId();
        String senderId = chatMsg.getSenderId();

        if (receiverId != null) {
            // 只要有一方拉黑，则终止发送
            GraceJSONResult result = OkHttpUtil.get("http://192.168.1.3:1000/friendship/isBlack?friendId1st=" + receiverId
                    + "&friendId2nd=" + senderId);
            Boolean isBlack = (Boolean) result.getData();
            if (isBlack) {
                return;
            }
        }

        // 时间校准, 以服务器的时间为准
        chatMsg.setChatTime(LocalDateTime.now());

        Integer msgType = chatMsg.getMsgType();

        // 获取channel
        Channel currentChannel = ctx.channel();
        String currentChannelId = currentChannel.id().asLongText();
        String currentChannelIdShort = currentChannel.id().asShortText();

        // 2. 判断消息类型, 根据不同的类型来处理不同的业务
        if (msgType == MsgTypeEnum.CONNECT_INIT.type) {
            // websocket初次open, 初始化channel, 关联channel和用户id
            UserChannelSession.putMultiChannels(senderId, currentChannel);
            UserChannelSession.putUserChannelIdRelation(currentChannelId, senderId);



            // 初次连接后, 该节点下的在线人数累加
            NettyServerNode minNode = dataContent.getServerNode();
            ZookeeperRegister.incrementOnlineCounts(minNode);

            // 获得IP和端口, 在redis中记录关联, 以便在前端设备断线后减少在线人数
            Jedis jedis = JedisPoolUtils.getJedis();
            jedis.set(senderId, JsonUtils.objectToJson(minNode));

        } else if (msgType == MsgTypeEnum.WORDS.type
                || msgType == MsgTypeEnum.IMAGE.type
                || msgType == MsgTypeEnum.VIDEO.type
                || msgType == MsgTypeEnum.VOICE.type) {

            // mq异步解耦，保存手动生成了id的消息到数据库
            // 此处通过snowflake直接生成唯一的Id, 而不是通过数据库自增的方式自动生成
            // 控制消息id在chatHandler中生成, 使得收发双方都能在在聊天服务器中获得消息id
            Snowflake snowflake = new Snowflake(new IdWorkerConfigBean());
            String sid = snowflake.nextId();

            chatMsg.setMsgId(sid);

            // 发送消息
            List<Channel> receiverChannels = UserChannelSession.getMultiChannels(receiverId);
            if (receiverChannels == null || receiverChannels.isEmpty()) {
                // receiverChannels 为空, 表示用户离线/断线, 消息不需要发送, 后续可以存储到数据库
                chatMsg.setIsReceiverOnLine(false);
            } else {
                chatMsg.setIsReceiverOnLine(true);
                if (msgType == MsgTypeEnum.VOICE.type) {
                    chatMsg.setIsRead(false);
                }
                // receiverChannels 不为空, receiver同账户多端接收消息
                send(receiverChannels, dataContent, chatMsg);
            }

            // 3. 把聊天信息作为mq的消息发送给消费者进行消费处理(保存到数据库)
            MessagePublisher.sendMsgToSave(chatMsg);
            System.out.println("Saving done!");
        }

        // 4. sender多端同步消息
        List<Channel> myOtherChannels = UserChannelSession
                .getMyOtherChannels(senderId, currentChannelId);
        if (myOtherChannels != null) {
            send(myOtherChannels, dataContent, chatMsg);
        }

        UserChannelSession.outputMulti();

//        currentChannel.writeAndFlush(new TextWebSocketFrame(currentChannelId));
    }

    /**
     * 客户端连接到服务器之后(打开链接)
     *
     * @param ctx
     * @throws Exception
     */
    @Override
    public void handlerAdded(ChannelHandlerContext ctx) throws Exception {
        Channel currentChannel = ctx.channel();
        String currentChannelId = currentChannel.id().asLongText();
        System.out.println("客户端建立连接, channel对应的长ID为：" + currentChannelId);

        // 获得客户端的channel, 并且存入到ChannelGroup中进行管理(作为一个客户端群组)
        clients.add(currentChannel);
    }

    /**
     * 关闭连接, 移除channel
     *
     * @param ctx
     * @throws Exception
     */
    @Override
    public void handlerRemoved(ChannelHandlerContext ctx) throws Exception {
        Channel currentChannel = ctx.channel();
        String currentChannelId = currentChannel.id().asLongText();
        System.out.println("客户端关闭连接, channel对应的长ID为：" + currentChannelId);

        // 移除多余的会话
        String userId = UserChannelSession.getUserIdByChannelId(currentChannelId);
        UserChannelSession.removeUselessChannels(userId, currentChannelId);

        clients.remove(currentChannel);

        // 在zk中累减在线人数
        Jedis jedis = JedisPoolUtils.getJedis();
        NettyServerNode minNode = JsonUtils.jsonToPojo(jedis.get(userId),
                NettyServerNode.class);
        ZookeeperRegister.decrementOnlineCounts(minNode);

    }

    /**
     * 发生异常并且捕获, 移除channel
     *
     * @param ctx
     * @param cause
     * @throws Exception
     */
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        Channel currentChannel = ctx.channel();
        String currentChannelId = currentChannel.id().asLongText();
        System.out.println("异常捕获, channel对应的长ID为：" + currentChannelId);

        // 发生异常之后关闭连接(关闭channel)
        ctx.channel().close();
        // 从ChannelGroup中移除对应的channel
        clients.remove(currentChannel);
        // 移除多余的会话
        String userId = UserChannelSession.getUserIdByChannelId(currentChannelId);
        UserChannelSession.removeUselessChannels(userId, currentChannelId);

        // 在zk中累减在线人数
        Jedis jedis = JedisPoolUtils.getJedis();
        NettyServerNode minNode = JsonUtils.jsonToPojo(jedis.get(userId),
                NettyServerNode.class);
        ZookeeperRegister.decrementOnlineCounts(minNode);

    }

    private static void send(List<Channel> channels, DataContent dataContent, ChatMsg chatMsg) {
        for (Channel c : channels) {
            Channel foundChannel = clients.find(c.id());
            if (foundChannel != null) {
                // 更新数据
                dataContent.setChatMsg(chatMsg);
                String chatTimeFormat = LocalDateUtils.format(chatMsg.getChatTime(),
                        LocalDateUtils.DATETIME_PATTERN_2);
                dataContent.setChatTime(chatTimeFormat);
                // 发送消息给clients中已经记录的设备通道
                foundChannel.writeAndFlush(
                        new TextWebSocketFrame(
                                JsonUtils.objectToJson(dataContent)));
            }
        }
    }
}