package org.itxuexi.rabbitmq;

import org.springframework.amqp.core.*;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQTestConfig {
    // 定义用户聊天业务交换机的名字
    public static final String TEST_EXCHANGE = "test_exchange";

    // 定义用户-LLM问答业务交换机的名字
    public static final String LLM_EXCHANGE = "llm_exchange";

    // 定义用户聊天业务队列的名字
    public static final String TEST_QUEUE = "test_queue";

    // 定义用户-LLM问答业务队列的名字
    public static final String LLM_QUEUE = "llm_queue";

    // 具体的路由地址(测试用途)
    public static final String ROUTING_KEY_TEST_SEND = "proj.wechat.test.send";

    // 发送用户聊天信息到消息队列并保存到数据库的路由地址
    public static final String ROUTING_KEY_WECHAT_MSG_SEND = "proj.wechat.wechat.msg.send";

    // 发送用户-LLM问答信息到消息队列并保存到数据库的路由地址
    public static final String ROUTING_KEY_LLM_MSG_SEND = "proj.wechat.llm.msg.send";

    // 创建交换机
    @Bean(TEST_EXCHANGE)
    public Exchange exchange() {
        return ExchangeBuilder.topicExchange(TEST_EXCHANGE).durable(true).build();
    }
    @Bean(LLM_EXCHANGE)
    public Exchange exchange2() {
        return ExchangeBuilder.topicExchange(LLM_EXCHANGE).durable(true).build();
    }

    // 创建队列
    @Bean(TEST_QUEUE)
    public Queue queue() {
        return QueueBuilder.durable(TEST_QUEUE).build();
    }

    @Bean(LLM_QUEUE)
    public Queue queue2() {
        return QueueBuilder.durable(LLM_QUEUE).build();
    }


    // 定义队列绑定到交换机的关系
    @Bean
    public Binding binding(@Qualifier(TEST_EXCHANGE) Exchange exchange,
                           @Qualifier(TEST_QUEUE) Queue queue) {
        return BindingBuilder
                .bind(queue)
                .to(exchange)
                .with("proj.wechat.#")
                .noargs(); // 执行绑定关系
    }

    @Bean
    public Binding binding2(@Qualifier(LLM_EXCHANGE) Exchange exchange,
                           @Qualifier(LLM_QUEUE) Queue queue) {
        return BindingBuilder
                .bind(queue)
                .to(exchange)
                .with("proj.wechat.#")
                .noargs(); // 执行绑定关系
    }
}
