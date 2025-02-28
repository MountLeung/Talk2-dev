package org.itxuexi.netty.mq;

import org.itxuexi.pojo.netty.ChatMsg;
import org.itxuexi.utils.JsonUtils;

public class MessagePublisher {

    // 定义交换机的名字
    public static final String TEST_EXCHANGE = "test_exchange";

    // 发布-订阅模式下的交换机名称
    public static final String FANOUT_EXCHANGE = "fanout_exchange_proj";

    // 定义队列的名字
    public static final String TEST_QUEUE = "test_queue";

    // 发送信息到消息队列并保存到数据库的路由地址
    public static final String ROUTING_KEY_WECHAT_MSG_SEND = "proj.wechat.wechat.msg.send";

//    public static void saveMessageByMQ(String msg) throws Exception {
//
//         // 1. 创建连接工厂
//         ConnectionFactory factory = new ConnectionFactory();
//         // 1.1 设置连接参数
//         factory.setHost("127.0.0.1");
//         factory.setPort(5672);
//         factory.setVirtualHost("itzixi");
//         factory.setUsername("lee");
//         factory.setPassword("lee");
//
//         // 1.2 建立连接
//         Connection connection = factory.newConnection();
//
//         // 2. 创建通道channel
//         Channel channel = connection.createChannel();
//
//         // 3. 定义队列
//         channel.queueDeclare(QUEUE_MSG,
//                             true,
//                             false,
//                             false,
//                             null);
//         // 4. 发送消息
//         channel.basicPublish(EXCHANGE_MSG,
//                             SAVE_MSG_ROUTING_KEY,
//                             null,
//                             msg.getBytes());
//
//         // 5. 关闭通道和连接
//         channel.close();
//         connection.close();
//     }
//
//     public static void main(String[] args) throws Exception {
//         //saveMessageByMQ("Send a chat msg by rabbitmq~~~  another  ");
//
//         RabbitMQConnectUtils connectUtils = new RabbitMQConnectUtils();
//         String msg = "Send a chat msg by rabbitmq~~~  new ";
//
//         ChatMsg chatMsg = new ChatMsg();
//         chatMsg.setMsgId("1001");
//         chatMsg.setMsg("123xyz");
//         // String pendingMsg = GsonUtils.object2String(chatMsg);
//         String pendingMsg = JsonUtils.objectToJson(chatMsg);
//
//         connectUtils.sendMsg(pendingMsg, EXCHANGE_MSG, SAVE_MSG_ROUTING_KEY);
//     }

    public static void sendMsgToSave(ChatMsg msg) throws Exception {
        RabbitMQConnectUtils connectUtils = new RabbitMQConnectUtils();
        try {
            connectUtils.sendMsg(JsonUtils.objectToJson(msg),
                    TEST_EXCHANGE,
                    ROUTING_KEY_WECHAT_MSG_SEND);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void sendMsgToNettyServers(String msg) throws Exception {
        RabbitMQConnectUtils connectUtils = new RabbitMQConnectUtils();

        try {
            connectUtils.sendMsg(msg,FANOUT_EXCHANGE, "");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
