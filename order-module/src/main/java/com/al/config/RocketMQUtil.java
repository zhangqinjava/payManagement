package com.al.config;

import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.client.producer.SendCallback;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.apache.rocketmq.spring.support.RocketMQHeaders;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class RocketMQUtil {

    private final RocketMQTemplate rocketMQTemplate;

    public RocketMQUtil(RocketMQTemplate rocketMQTemplate) {
        this.rocketMQTemplate = rocketMQTemplate;
    }

    /**
     * 发送普通同步消息
     */
    public void send(String topic, String tag, String key, Object body) {

        try {

            Message<?> message = MessageBuilder.withPayload(body)
                    .setHeader(RocketMQHeaders.KEYS, key)
                    .build();

            SendResult result = rocketMQTemplate.syncSend(topic + ":" + tag, message);

            log.info("MQ发送成功 topic={} tag={} key={} result={}",
                    topic, tag, key, result);

        } catch (Exception e) {

            log.error("MQ发送失败 topic={} tag={} key={}",
                    topic, tag, key, e);

            throw new RuntimeException("MQ发送失败");

        }

    }

    /**
     * 顺序消息
     */
    public void sendOrderly(String topic, String tag, String key, Object body, String hashKey) {

        try {

            Message<?> message = MessageBuilder.withPayload(body)
                    .setHeader(RocketMQHeaders.KEYS, key)
                    .build();

            SendResult result = rocketMQTemplate.syncSendOrderly(
                    topic + ":" + tag,
                    message,
                    hashKey
            );

            log.info("顺序MQ发送成功 topic={} tag={} key={} result={}",
                    topic, tag, key, result);

        } catch (Exception e) {

            log.error("顺序MQ发送失败 topic={} key={}", topic, key, e);

            throw new RuntimeException("MQ发送失败");

        }

    }

    /**
     * 延迟消息
     */
    public void sendDelay(String topic, String tag, String key, Object body, int delayLevel) {

        try {

            Message<?> message = MessageBuilder.withPayload(body)
                    .setHeader(RocketMQHeaders.KEYS, key)
                    .build();

            SendResult result = rocketMQTemplate.syncSend(
                    topic + ":" + tag,
                    message,
                    3000,
                    delayLevel
            );

            log.info("延迟MQ发送成功 topic={} tag={} key={} delay={} result={}",
                    topic, tag, key, delayLevel, result);

        } catch (Exception e) {

            log.error("延迟MQ发送失败 topic={} key={}", topic, key, e);

            throw new RuntimeException("MQ发送失败");

        }

    }

    /**
     * 异步消息
     */
    public void sendAsync(String topic, String tag, String key, Object body) {

        Message<?> message = MessageBuilder.withPayload(body)
                .setHeader(RocketMQHeaders.KEYS, key)
                .build();

        rocketMQTemplate.asyncSend(topic + ":" + tag, message, new SendCallback() {

            @Override
            public void onSuccess(SendResult sendResult) {

                log.info("MQ异步发送成功 topic={} tag={} key={} result={}",
                        topic, tag, key, sendResult);

            }

            @Override
            public void onException(Throwable e) {

                log.error("MQ异步发送失败 topic={} tag={} key={}",
                        topic, tag, key, e);

            }
        });

    }
    // 发送事务消息
    public void sendTransaction(String topic, String tag, String key, Object body, Object arg) {
        try {
            String destination = topic;
            if (tag != null && !tag.isEmpty()) {
                destination = topic + ":" + tag;
            }

            // 构建消息
            Message<?> message = MessageBuilder.withPayload(body)
                    .setHeader(RocketMQHeaders.KEYS, key)
                    .build();

            // 发送事务消息
            rocketMQTemplate.sendMessageInTransaction(destination, message, arg);

            log.info("事务消息发送完成, destination={}, key={}, body={}", destination, key, body);
        } catch (Exception e) {
            log.error("发送事务消息失败, topic={}, tag={}, key={}, body={}", topic, tag, key, body, e);
        }
    }

}