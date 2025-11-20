package com.example.shorturl.service;

import com.example.shorturl.model.ShortUrl;

public interface ShortUrlService {
    
    /**
     * 创建短链接
     * @param originalUrl 原始长链接
     * @param businessId 业务ID
     * @return 短链接对象
     */
    ShortUrl createShortUrl(String originalUrl, String businessId);
    
    /**
     * 根据短码获取原始链接
     * @param shortCode 短码
     * @return 原始链接
     */
    String getOriginalUrl(String shortCode);
    
    /**
     * 增加点击次数
     * @param shortCode 短码
     */
    void incrementClickCount(String shortCode);
    
    /**
     * 检查短码是否存在
     * @param shortCode 短码
     * @return 是否存在
     */
    boolean existsByShortCode(String shortCode);
}