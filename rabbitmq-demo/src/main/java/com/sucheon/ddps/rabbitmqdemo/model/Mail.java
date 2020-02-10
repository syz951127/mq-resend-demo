package com.sucheon.ddps.rabbitmqdemo.model;

import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class Mail {

    private String sendTo;

    private String title;

    private String content;

    private String msgId;
}
