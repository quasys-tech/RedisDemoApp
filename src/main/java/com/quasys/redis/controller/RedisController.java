package com.quasys.redis.controller;

import com.quasys.redis.model.Data;
import com.quasys.redis.service.RedisService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.Cursor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ScanOptions;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

@RestController
@CrossOrigin(origins = "*")
@RequestMapping("/api")
public class RedisController {

    @Autowired
    private RedisService redisService;
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;
    private  int threadCount = Integer.parseInt(System.getenv("REDIS_THREAD_COUNT"));
    private  String usePrefix = System.getenv("REDIS_USE_PREFIX");
    private static String redisPrefix = System.getenv("REDIS_PREFIX");
    private final ScheduledExecutorService executorService = Executors.newScheduledThreadPool(threadCount);
    private final AtomicBoolean isWriting = new AtomicBoolean(false);
    private final List<ScheduledFuture<?>> scheduledTasks = new ArrayList<>();
    private final AtomicInteger counter = new AtomicInteger(1);
//    @Autowired
//    private RedisPublisher redisPublisher;

//    private final ThreadPoolTaskScheduler taskScheduler;
//    private ScheduledFuture<?> scheduledTask;
//    private boolean isWriting = false;

    private  int period = Integer.parseInt(System.getenv("REDIS_WRITE_PERIOD"));


    public RedisController(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
//        this.taskScheduler = new ThreadPoolTaskScheduler();
//        this.taskScheduler.setPoolSize(1);
//        this.taskScheduler.initialize();
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

    @GetMapping("/read-all")
    public ResponseEntity<Map<Object, Object>> readAll() {
        Map<Object, Object> result = new HashMap<>();

        try (Cursor<byte[]> cursor = redisTemplate.getConnectionFactory()
                .getConnection()
                .scan(ScanOptions.scanOptions().match("*").count(1000).build())) {

            while (cursor.hasNext()) {
                Map<String, Object> record = new HashMap<>();
                String key = new String(cursor.next());
                if (usePrefix.trim().toLowerCase().equals("true")) {
                    String prekey = key;
                    if (prekey.startsWith(redisPrefix)) {
                        prekey = prekey.substring(redisPrefix.length());
                        Map<Object, Object> entries = redisTemplate.opsForHash().entries(prekey);
                        record.put(key, entries);
//                result.putAll(redisTemplate.opsForHash().entries(key));
                        result.putAll(record);
                    }
                } else {
                    Map<Object, Object> entries = redisTemplate.opsForHash().entries(key);
                    record.put(key, entries);
//                result.putAll(redisTemplate.opsForHash().entries(key));
                    result.putAll(record);
                }
            }
        }
        return ResponseEntity.ok(result);
    }

    @GetMapping("/getAllData")
    public ResponseEntity<List<String>> getAllData() {
        List<String> dataList = new ArrayList<>();
        int i = 1;
        while (true) {
            String key = "data" + i;
            String value = (String) redisTemplate.opsForValue().get(key);

            if (value == null) {
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
        if (isWriting.get()) {
            return ResponseEntity.ok().build();
        }
        isWriting.set(true);

        for (int i = 0; i < threadCount; i++) { // 5 farklı thread oluştur
            int finalI = i;
            ScheduledFuture<?> task = executorService.scheduleAtFixedRate(() -> {
                if (isWriting.get()) {
                    String currentTime = LocalDateTime.now().toString();
//                    int c = counter.getAndIncrement();
                    redisTemplate.opsForHash().put("current_time", String.valueOf(counter.getAndIncrement()), currentTime);
                }
            }, 0, period, TimeUnit.MILLISECONDS); // 100ms arayla çalışacak
            scheduledTasks.add(task);
        }

        return ResponseEntity.ok().build();
    }

    @GetMapping("/stop")
    public ResponseEntity<Void> stopWritingTime() {
        isWriting.set(false);
        for (ScheduledFuture<?> task : scheduledTasks) {
            task.cancel(true);
        }
        scheduledTasks.clear();
        return ResponseEntity.ok().build();
    }
}
