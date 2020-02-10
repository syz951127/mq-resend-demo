package com.sucheon.ddps.rabbitmqdemo.config;

import com.sucheon.ddps.rabbitmqdemo.model.MsgModel;
import com.sucheon.ddps.rabbitmqdemo.service.MsgService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

import java.util.List;

@Configuration
@Slf4j
@EnableScheduling
public class ScheduleTask {

    @Autowired
    private MsgService msgService;

    /**
     * 每30秒消息补发 次数达到3次不再发送
     */
    @Scheduled(cron = "0/30 * * * * ?")
    public void checkMsg() {
        log.info("投递失败消息进行重试开始...");
        List<MsgModel> failMsg = msgService.getFailMsg();
        failMsg.forEach(msg -> {
            msgService.reSendMsg(msg);
        });
        log.info("投递失败消息进行重试结束...");
    }
}
