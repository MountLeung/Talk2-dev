package org.itxuexi.rabbitmq;

import com.rabbitmq.client.Channel;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.itxuexi.pojo.PromptMessage;
import org.itxuexi.pojo.netty.ChatMsg;
import org.itxuexi.service.ChatMessageService;
import org.itxuexi.service.ChatRobotService;
import org.itxuexi.utils.JsonUtils;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class RabbitMQConsumer {

    @Resource
    private ChatMessageService chatMessageService;

    @Resource
    private ChatRobotService chatRobotService;

    /**
     * 对Netty聊天消息的消费
     * @param payload
     * @param message
     */
    @RabbitListener(queues = {RabbitMQTestConfig.TEST_QUEUE})
    public void watchQueue(String payload, Message message) {

        String routingKey = message.getMessageProperties().getReceivedRoutingKey();
        log.info("routingKey = " + routingKey);

        if (routingKey.equals(RabbitMQTestConfig.ROUTING_KEY_WECHAT_MSG_SEND)) {
            String msg = payload;
            ChatMsg chatMsg = JsonUtils.jsonToPojo(msg, ChatMsg.class);

            chatMessageService.saveMsg(chatMsg);
        }
    }

    /**
     * 对LLM问答消息的消费
     * @param payload
     * @param message
     */
    @RabbitListener(queues = {RabbitMQTestConfig.LLM_QUEUE})
    public void watchQueue2(String payload, Message message) {

        String routingKey = message.getMessageProperties().getReceivedRoutingKey();
        log.info("routingKey = " + routingKey);

        if (routingKey.equals(RabbitMQTestConfig.ROUTING_KEY_LLM_MSG_SEND)) {
            String msg = payload;
            PromptMessage promptMsg = JsonUtils.jsonToPojo(msg, PromptMessage.class);

            chatRobotService.saveMsg(promptMsg);
        }
    }
}
