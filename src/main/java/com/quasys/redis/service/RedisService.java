package com.quasys.redis.service;

import com.quasys.redis.model.Data;
import org.springframework.stereotype.Service;

@Service
public class RedisService {

    public void writeData(Data data) {
        System.out.println("Test");
    }
}
