package com.example.shorturl.service.impl;

import com.example.shorturl.model.ShortUrl;
import com.example.shorturl.repository.ShortUrlRepository;
import com.example.shorturl.service.IdGeneratorService;
import com.example.shorturl.service.ShortUrlService;
import com.example.shorturl.util.UrlCodecUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.util.Optional;

@Service
@Transactional
public class ShortUrlServiceImpl implements ShortUrlService {
    
    @Autowired
    private ShortUrlRepository shortUrlRepository;
    
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;
    
    @Autowired
    private IdGeneratorService idGeneratorService;
    
    // 随机起始值，用于确保短码的随机性
    private final long randomStart = UrlCodecUtil.generateRandomStart();
    
    @Value("${app.short-url.prefix:http://short.ly/}")
    private String shortUrlPrefix;
    
    @Override
    public ShortUrl createShortUrl(String originalUrl, String businessId) {
        // 检查是否已存在相同的原始URL和业务ID
        Optional<ShortUrl> existing = shortUrlRepository.findByOriginalUrlAndBusinessId(originalUrl, businessId);
        if (existing.isPresent()) {
            return existing.get();
        }
        
        // 获取下一个ID并转换为短码
        long nextId = getNextId();
        String shortCode = UrlCodecUtil.encode(nextId + randomStart);
        
        // 检查Redis缓存中是否已存在该短码
        String cacheKey = "short_url:" + shortCode;
        Object cached = redisTemplate.opsForValue().get(cacheKey);
        if (cached != null) {
            // 如果缓存中存在，递增ID并重新编码
            nextId = getNextId();
            shortCode = UrlCodecUtil.encode(nextId + randomStart);
        }
        
        // 创建短链接对象
        ShortUrl shortUrl = new ShortUrl(shortCode, originalUrl, businessId);
        
        // 保存到数据库
        shortUrl = shortUrlRepository.save(shortUrl);
        
        // 缓存到Redis
        redisTemplate.opsForValue().set(cacheKey, originalUrl, Duration.ofHours(24));
        
        return shortUrl;
    }
    
    @Override
    @Transactional(readOnly = true)
    public String getOriginalUrl(String shortCode) {
        // 首先从Redis缓存中获取
        String cacheKey = "short_url:" + shortCode;
        Object cached = redisTemplate.opsForValue().get(cacheKey);
        if (cached != null) {
            return (String) cached;
        }
        
        // 如果缓存中没有，则从数据库中查询
        Optional<ShortUrl> optionalShortUrl = shortUrlRepository.findActiveByShortCode(shortCode);
        if (optionalShortUrl.isPresent()) {
            ShortUrl shortUrl = optionalShortUrl.get();
            // 缓存到Redis
            redisTemplate.opsForValue().set(cacheKey, shortUrl.getOriginalUrl(), Duration.ofHours(24));
            return shortUrl.getOriginalUrl();
        }
        
        return null;
    }
    
    @Override
    public void incrementClickCount(String shortCode) {
        // 使用Redis来统计点击次数，然后定期同步到数据库
        String countKey = "click_count:" + shortCode;
        redisTemplate.opsForValue().increment(countKey);
        
        // 同时设置过期时间，避免长期占用内存
        redisTemplate.expire(countKey, Duration.ofDays(7));
    }
    
    @Override
    @Transactional(readOnly = true)
    public boolean existsByShortCode(String shortCode) {
        return shortUrlRepository.existsByShortCode(shortCode);
    }
    
    /**
     * 获取下一个ID，使用Redis ID生成器确保高性能
     * @return 下一个ID
     */
    private long getNextId() {
        return idGeneratorService.getNextId();
    }
}