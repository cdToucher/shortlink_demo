package com.example.shorturl.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
public class IdGeneratorService {
    
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;
    
    private static final String ID_KEY = "short_url_id_counter";
    private static final long BATCH_SIZE = 100; // 每次预分配的ID数量
    
    /**
     * 获取下一个ID
     * @return 下一个可用的ID
     */
    public synchronized long getNextId() {
        String key = ID_KEY;
        
        // 尝试从Redis获取当前ID
        Object currentIdObj = redisTemplate.opsForValue().get(key);
        Long currentId;
        
        if (currentIdObj == null) {
            // 如果Redis中没有，则从数据库获取最大ID并增加批量大小
            // 这里我们简单地从1开始，实际应用中可以从数据库获取最大ID
            currentId = redisTemplate.opsForValue().increment(key, BATCH_SIZE);
            // 设置初始值为BATCH_SIZE，这样第一个获取的ID是1
            if (currentId == BATCH_SIZE) {
                redisTemplate.opsForValue().set(key, 1L, 1, TimeUnit.DAYS); // 设置过期时间1天
                return 1L; // 返回第一个ID
            }
        }
        
        // 增加计数器并返回新ID
        Long newId = redisTemplate.opsForValue().increment(key);
        
        // 检查是否需要预分配更多ID
        if (newId % BATCH_SIZE == 0) {
            // 预分配下一批ID
            redisTemplate.opsForValue().increment(key, BATCH_SIZE);
        }
        
        return newId;
    }
}