package com.quasys.redis.service;

import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.ChannelTopic;


@Service
public class RedisPublisher {

//    @Autowired
//    private RedisTemplate<String, Object> redisTemplate;
//
//    @Autowired
//    private ChannelTopic topic;
//
//    public void publish(String message) {
//        redisTemplate.convertAndSend(topic.getTopic(), message);
//    }
}
