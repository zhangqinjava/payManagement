package com.al.common.business;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum TopicEnum {
    ACCOUNT_UP("ACCOUNT_TOPIC_UP","收单账户异步上账"),
    ORDER_SPLIT("ORDER_TOPIC_SPLIT","订单异步分账");
    private String topic;
    private String desc;
    public static TopicEnum getByTopic(String topic) {
        for (TopicEnum topicEnum : TopicEnum.values()) {
            if (topicEnum.getTopic().equals(topic)) {
                return topicEnum;
            }
        }
        return null;
    }

}
