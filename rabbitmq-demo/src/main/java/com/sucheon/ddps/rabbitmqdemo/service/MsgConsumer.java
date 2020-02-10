package com.sucheon.ddps.rabbitmqdemo.service;

import com.alibaba.fastjson.JSONObject;
import com.rabbitmq.client.Channel;
import com.sucheon.ddps.rabbitmqdemo.config.RabbitConfig;
import com.sucheon.ddps.rabbitmqdemo.model.Mail;
import com.sucheon.ddps.rabbitmqdemo.model.MsgModel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@Slf4j
public class MsgConsumer {

    @Autowired
    private MsgService msgService;

    @Autowired
    private MailService mailService;

    @RabbitListener(queues = RabbitConfig.QUEUE)
    public void listen(Message message, Channel channel) throws IOException {
        JSONObject receiveMsg = JSONObject.parseObject(new String(message.getBody(), "UTF-8"));
        String msgId = receiveMsg.getString("msgId");
        log.info("消费消息:id=[{}],内容:[{}]", msgId, receiveMsg.getString("msg"));
        MessageProperties messageProperties = message.getMessageProperties();
        MsgModel msgModel = msgService.getMsgById(msgId);
        if (msgModel.getStatus() == MsgModel.Status.DELIVERY_CONSUMER_SUCCESS.getCode()) {
            log.info("重复消费:id=[{}]", msgId);
            return;
        }
        long tag = messageProperties.getDeliveryTag();
        boolean sendResult = mailService.send(Mail.builder().content(receiveMsg.getString("msg")).msgId(msgId).title("测试邮发送").sendTo("763938645@qq.com").build());
        // 模拟数据发送异常情况
        if (sendResult) {
            // 成功消费
            channel.basicAck(tag, false);
            log.info("消费:id=[{}]成功", msgId);
            msgService.updateMsg(msgId, MsgModel.Status.DELIVERY_CONSUMER_SUCCESS.getCode());
        } else {
            channel.basicNack(tag, false, true);
            log.info("消费:id=[{}]失败", msgId);
            msgService.updateMsg(msgId, MsgModel.Status.DELIVERY_CONSUMER_FAIL.getCode());
        }
    }
}
