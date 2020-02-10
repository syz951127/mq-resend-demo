package com.sucheon.ddps.rabbitmqdemo.model;

import lombok.*;

import javax.persistence.Id;
import javax.persistence.Table;
import java.util.Date;

@Builder
@Table(name = "msg_log")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class MsgModel {

    @Id
    private String msgId;

    private String msg;

    private String exchange;

    private String routingKey;

    private int status;

    private int tryCount;

    private Date nextTryTime;

    private Date createTime;

    private Date updateTime;

    @AllArgsConstructor
    @Getter
    public enum Status {
        DELIVERY("投递中", 0),
        DELIVERY_EXCHANGE_SUCCESS("投递交换机成功", 1),
        DELIVERY_QUEUE_SUCCESS("投递队列成功", 2),
        DELIVERY_CONSUMER_SUCCESS("消费成功", 3),
        DELIVERY_EXCHANGE_FAIL("投递交换机失败", 4),
        DELIVERY_QUEUE_FAIL("投递队列失败", 5),
        DELIVERY_CONSUMER_FAIL("消费失败", 6),
        ;
        private String msg;
        private int code;
    }
}
