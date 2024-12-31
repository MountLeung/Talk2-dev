package org.itxuexi.rabbitmq;

import org.itxuexi.pojo.PromptMessage;
import org.itxuexi.utils.JsonUtils;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class MessageLLMPublisher {
    // 定义交换机的名字
    public static final String LLM_EXCHANGE = "llm_exchange";

    // 发送信息到消息队列并保存到数据库的路由地址
    public static final String ROUTING_KEY_LLM_MSG_SEND = "proj.wechat.llm.msg.send";

    @Autowired
    private RabbitTemplate rabbitTemplate;

    public void sendMsgToSave(PromptMessage msg) {
        String s = JsonUtils.objectToJson(msg);
        rabbitTemplate.convertAndSend(LLM_EXCHANGE, ROUTING_KEY_LLM_MSG_SEND,
                                s);
    }
}