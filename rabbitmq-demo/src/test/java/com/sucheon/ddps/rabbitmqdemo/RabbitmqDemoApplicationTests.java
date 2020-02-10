package com.sucheon.ddps.rabbitmqdemo;

import com.sucheon.ddps.rabbitmqdemo.config.RabbitConfig;
import com.sucheon.ddps.rabbitmqdemo.service.MsgService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class RabbitmqDemoApplicationTests {

    @Autowired
    private MsgService msgService;

    @Test
    void contextLoads() {
        String result = msgService.sendMsg(RabbitConfig.EXCHANGE, RabbitConfig.ROUTING_KEY, "测试消息");
    }

}
