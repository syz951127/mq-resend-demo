package com.sucheon.ddps.rabbitmqdemo.service;

import com.alibaba.fastjson.JSONObject;
import com.sucheon.ddps.rabbitmqdemo.mapper.MsgMapper;
import com.sucheon.ddps.rabbitmqdemo.model.MsgModel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@Service
@Slf4j
public class MsgService {

    private static final int MAX_RETRY_COUNT = 3;

    @Autowired
    private MsgMapper msgMapper;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    public String sendMsg(String exchange, String routingKey, String message) {
        String msgId = UUID.randomUUID().toString().replaceAll("-", "");
        JSONObject msgJson = new JSONObject();
        msgJson.put("msg", message);
        msgJson.put("msgId", msgId);
        MsgModel msgModel = MsgModel.builder()
                .msgId(msgId)
                .msg(msgJson.toJSONString())
                .exchange(exchange)
                .routingKey(routingKey)
                .status(MsgModel.Status.DELIVERY.getCode())
                .tryCount(0)
                .nextTryTime(null)
                .createTime(new Date())
                .updateTime(new Date())
                .build();
        msgMapper.insert(msgModel);
        CorrelationData correlationData = new CorrelationData(msgId);
        rabbitTemplate.convertAndSend(exchange, routingKey, msgJson.toJSONString(), correlationData);
        return "send success";
    }

    public void updateMsg(String msgId, int code) {
        MsgModel msgModel = msgMapper.getOne(msgId);
        msgModel.setStatus(code);
        msgMapper.updateStatus(msgModel);
    }

    public MsgModel getMsgById(String msgId) {
        return msgMapper.selectOne(MsgModel.builder().msgId(msgId).build());
    }

    public List<MsgModel> getFailMsg() {
        List<MsgModel> failMsgList = msgMapper.selectFailMsg();
        if (CollectionUtils.isEmpty(failMsgList)) {
            return new ArrayList<>();
        }
        return failMsgList;
    }

    public void reSendMsg(MsgModel msgModel) {
        int tryCount = msgModel.getTryCount();
        if (MAX_RETRY_COUNT == tryCount) {
            log.info("消息Id:[{}],已经重试3次...不再继续发送", msgModel.getMsgId());
            return;
        }
        msgModel.setTryCount(++tryCount);
        msgModel.setStatus(MsgModel.Status.DELIVERY.getCode());
        msgModel.setUpdateTime(new Date());
        msgMapper.updateStatus(msgModel);
        CorrelationData correlationData = new CorrelationData(msgModel.getMsgId());
        log.info("消息Id:[{}],重试发送第{}次...", msgModel.getMsgId(), tryCount);
        rabbitTemplate.convertAndSend(msgModel.getExchange(), msgModel.getRoutingKey(), msgModel.getMsg(), correlationData);
    }
}
