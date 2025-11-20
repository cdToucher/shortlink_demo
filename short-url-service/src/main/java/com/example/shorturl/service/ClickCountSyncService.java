package com.example.shorturl.service;

import com.example.shorturl.repository.ShortUrlRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

@Service
public class ClickCountSyncService {
    
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;
    
    @Autowired
    private ShortUrlRepository shortUrlRepository;
    
    /**
     * 每分钟同步一次Redis中的点击计数到数据库
     */
    @Scheduled(fixedRate = 60000) // 每分钟执行一次
    @Transactional
    public void syncClickCounts() {
        // 获取所有以"click_count:"开头的键
        Set<String> keys = redisTemplate.keys("click_count:*");
        
        if (keys != null && !keys.isEmpty()) {
            for (String key : keys) {
                try {
                    // 从Redis获取点击计数
                    Object countObj = redisTemplate.opsForValue().get(key);
                    if (countObj != null) {
                        String shortCode = key.replace("click_count:", "");
                        Long count = Long.valueOf(countObj.toString());
                        
                        // 更新数据库中的点击计数
                        shortUrlRepository.incrementClickCount(shortCode);
                        
                        // 从Redis中删除已同步的计数
                        redisTemplate.delete(key);
                    }
                } catch (Exception e) {
                    // 记录错误但继续处理其他键
                    System.err.println("Error syncing click count for key: " + key + ", error: " + e.getMessage());
                }
            }
        }
    }
}