package org.itxuexi;

import org.itxuexi.service.ChatRobotService;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.scheduling.annotation.EnableAsync;
import reactor.core.publisher.Mono;

@SpringBootApplication
@EnableDiscoveryClient  //开启服务的注册与发现功能
@MapperScan(basePackages = "org.itxuexi.mapper")
@EnableFeignClients(value = "org.itxuexi.api.feign")
public class Application {
    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}
