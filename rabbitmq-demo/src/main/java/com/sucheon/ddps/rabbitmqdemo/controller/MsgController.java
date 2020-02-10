package com.sucheon.ddps.rabbitmqdemo.controller;

import com.sucheon.ddps.rabbitmqdemo.config.RabbitConfig;
import com.sucheon.ddps.rabbitmqdemo.service.MsgService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class MsgController {

    @Autowired
    private MsgService msgService;

    @GetMapping("/msg")
    public String sendMsg(String msg){
        return msgService.sendMsg(RabbitConfig.EXCHANGE, RabbitConfig.ROUTING_KEY, msg);
    }
}
