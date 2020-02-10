package com.sucheon.ddps.rabbitmqdemo.config;

import com.alibaba.fastjson.JSONObject;
import com.sucheon.ddps.rabbitmqdemo.model.MsgModel;
import com.sucheon.ddps.rabbitmqdemo.service.MsgService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.listener.RabbitListenerContainerFactory;
import org.springframework.amqp.support.converter.SimpleMessageConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.UnsupportedEncodingException;

@Configuration
@Slf4j
public class RabbitConfig {

    public static final String EXCHANGE = "mq.test.exchange";
    public static final String ROUTING_KEY = "mq.routing.key";
    public static final String QUEUE = "mq.test.queue";

    public static final String MAIL_QUEUE_NAME = "mail.queue";
    public static final String MAIL_EXCHANGE_NAME = "mail.exchange";
    public static final String MAIL_ROUTING_KEY_NAME = "mail.routing.key";

    @Autowired
    private CachingConnectionFactory cachingConnectionFactory;

    @Autowired
    private MsgService msgService;

    @Bean
    public Queue testQueue() {
        return new Queue(QUEUE, true);
    }

    @Bean
    public Exchange exchange() {
        return new DirectExchange(EXCHANGE, true, false);
    }

    @Bean
    public Binding binding() {
        return BindingBuilder.bind(testQueue()).to(exchange()).with(ROUTING_KEY).noargs();
    }

    @Bean
    public Queue mailQueue() {
        return new Queue(MAIL_QUEUE_NAME, true);
    }

    @Bean
    public Exchange mailExchange() {
        return new DirectExchange(MAIL_EXCHANGE_NAME, true, false);
    }

    @Bean
    public Binding mailBinding() {
        return BindingBuilder.bind(mailQueue()).to(mailExchange()).with(MAIL_ROUTING_KEY_NAME).noargs();
    }

    @Bean
    public RabbitTemplate rabbitTemplate() {
        RabbitTemplate rabbitTemplate = new RabbitTemplate(cachingConnectionFactory);
        // 消息是否成功发送到exchange
        rabbitTemplate.setConfirmCallback((CorrelationData correlationData, boolean ack, String cause) -> {
            String msgId = correlationData.getId();
            if (ack) {
                log.info("消息发送到exchange成功,msgId=[{}]", msgId);
                msgService.updateMsg(msgId, MsgModel.Status.DELIVERY_EXCHANGE_SUCCESS.getCode());
            } else {
                log.info("消息发送到exchange失败,msgId=[{}],cause=[{}]", msgId, cause);
                msgService.updateMsg(msgId, MsgModel.Status.DELIVERY_EXCHANGE_FAIL.getCode());
            }
        });
        // 触发setReturnCallback回调必须设置mandatory=true, 否则Exchange没有找到Queue就会丢弃掉消息, 而不会触发回调
        rabbitTemplate.setMandatory(true);
        // 消息是否从Exchange路由到Queue, 注意: 这是一个失败回调, 只有消息从Exchange路由到Queue失败才会回调这个方法
        rabbitTemplate.setReturnCallback((Message message, int replyCode, String replyText, String exchange, String routingKey) -> {
            try {
                msgService.updateMsg(JSONObject.parseObject(new String(message.getBody(), "UTF-8")).getString("msgId"), MsgModel.Status.DELIVERY_QUEUE_FAIL.getCode());
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
            log.info("消息从Exchange路由到Queue失败: exchange: {}, route: {}, replyCode: {}, replyText: {}, message: {}", exchange, routingKey, replyCode, replyText, message);
        });
        return rabbitTemplate;
    }

    @Bean
    public RabbitListenerContainerFactory<?> rabbitListenerContainerFactory() {
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        factory.setConnectionFactory(cachingConnectionFactory);
        factory.setMessageConverter(new SimpleMessageConverter());
        factory.setAcknowledgeMode(AcknowledgeMode.MANUAL);
        return factory;
    }
}
