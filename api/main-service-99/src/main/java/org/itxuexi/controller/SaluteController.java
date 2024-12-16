package org.itxuexi.controller;

import jakarta.annotation.Resource;
import org.itxuexi.pojo.netty.ChatMsg;
import org.itxuexi.rabbitmq.RabbitMQTestConfig;
import org.itxuexi.utils.JsonUtils;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("m")
public class SaluteController {
    @GetMapping("salute")
    public Object salute(){
        return "Morning";
    }

    @Resource
    private RabbitTemplate rabbitTemplate;

    @GetMapping("mq")
    public Object mq() {

        ChatMsg chatMsg = new ChatMsg();
        chatMsg.setMsg("Good Night");
        String msg = JsonUtils.objectToJson(chatMsg);

        rabbitTemplate.convertAndSend(RabbitMQTestConfig.TEST_EXCHANGE,
                RabbitMQTestConfig.ROUTING_KEY_TEST_SEND,
                msg);

        return "ok";
    }
}
