package com.quasys.redis.controller;

import com.quasys.redis.model.Data;
import com.quasys.redis.service.RedisService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.atomic.AtomicInteger;

@RestController
@CrossOrigin(origins = "*")
@RequestMapping("/api")
public class RedisController {

    @Autowired
    private RedisService redisService;
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

//    @Autowired
//    private RedisPublisher redisPublisher;

    private final ThreadPoolTaskScheduler taskScheduler;
    private ScheduledFuture<?> scheduledTask;
    private boolean isWriting = false;

    public RedisController(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
        this.taskScheduler = new ThreadPoolTaskScheduler();
        this.taskScheduler.setPoolSize(1);
        this.taskScheduler.initialize();
    }

    @PostMapping
    public ResponseEntity<Void> writeData(@RequestBody Data data) {
        System.out.println("Write Data");
        System.out.println(data.getValue());
        redisTemplate.opsForHash().put("data", data.getKey(), data.getValue());
//        redisTemplate.opsForValue().set(data.getKey(), data.getValue());
//        redisTemplate.opsForList().leftPush("texts", data.getData());
//        redisPublisher.publish(data.getValue());
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @GetMapping
    public ResponseEntity<Map<Object, Object>> readData(){
        Map<Object, Object> result = new HashMap<>();

        result = redisTemplate.opsForHash().entries("data");

        for (Map.Entry<Object, Object> entry : result.entrySet()) {
            System.out.println("Field: " + entry.getKey() + ", Value: " + entry.getValue());
        }
        return ResponseEntity.ok(result);

    }

    @GetMapping("/getAllData")
    public ResponseEntity<List<String>> getAllData() {
        List<String> dataList = new ArrayList<>();

        // Redis'teki tüm "data" anahtarlarını bulmak için bir döngü kullanıyoruz.
        int i = 1;
        while (true) {
            String key = "data" + i;
            String value = (String) redisTemplate.opsForValue().get(key);

            if (value == null) {
                // Eğer veri bulunamazsa döngüyü sonlandır.
                break;
            }

            dataList.add(value);
            i++;
        }

        return new ResponseEntity<>(dataList, HttpStatus.OK);
    }
    @GetMapping("/readTime")
    public ResponseEntity<Map<Object, Object>> readTime(){
        Map<Object, Object> result = new HashMap<>();

        result = redisTemplate.opsForHash().entries("current_time");
        return ResponseEntity.ok(result);
    }

    @GetMapping("/start")
    public ResponseEntity<Void> writeTime() throws InterruptedException {
        AtomicInteger i = new AtomicInteger(1);
        if (isWriting) {
            return new ResponseEntity<>(HttpStatus.OK);
        }
        isWriting = true;
        scheduledTask = taskScheduler.scheduleAtFixedRate(() -> {
            String currentTime = LocalDateTime.now().toString();
            redisTemplate.opsForHash().put("current_time", String.valueOf(i), currentTime);
//            redisTemplate.opsForHash().put("current_time", String.valueOf(LocalDateTime.now().getSecond()), currentTime);
//            redisTemplate.opsForValue().set("data" + String.valueOf(i.get()), currentTime);
            i.getAndIncrement();
//            System.out.println("Writing time to Redis: " + currentTime);
        }, 100);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @GetMapping("/stop")
    public ResponseEntity<Void> stopWritingTime() {
        if (scheduledTask != null) {
            scheduledTask.cancel(true);
        }
        isWriting = false;
        System.out.println("Stopped Writing Data");
        return new ResponseEntity<>(HttpStatus.OK);
    }
}
