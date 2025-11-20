package com.example.shorturl.controller;

import com.example.shorturl.model.ShortUrl;
import com.example.shorturl.service.ShortUrlService;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@RestController
@RequestMapping("/api/short-url")
public class ShortUrlController {
    
    @Autowired
    private ShortUrlService shortUrlService;
    
    @Value("${app.short-url.prefix:http://short.ly/}")
    private String shortUrlPrefix;
    
    /**
     * 创建短链接
     * @param originalUrl 原始链接
     * @param businessId 业务ID
     * @return 短链接对象
     */
    @PostMapping("/create")
    public ResponseEntity<?> createShortUrl(
            @RequestParam String originalUrl,
            @RequestParam(defaultValue = "default") String businessId) {
        try {
            // 验证URL格式
            if (originalUrl == null || originalUrl.trim().isEmpty()) {
                return ResponseEntity.badRequest().body("Original URL cannot be empty");
            }
            
            // 确保URL有协议前缀
            if (!originalUrl.startsWith("http://") && !originalUrl.startsWith("https://")) {
                originalUrl = "http://" + originalUrl;
            }
            
            ShortUrl shortUrl = shortUrlService.createShortUrl(originalUrl, businessId);
            // 构建完整的短链接
            String fullShortUrl = shortUrlPrefix + shortUrl.getShortCode();
            
            return ResponseEntity.ok(new CreateResponse(fullShortUrl, shortUrl.getShortCode()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error creating short URL: " + e.getMessage());
        }
    }
    
    /**
     * 根据短码获取原始链接（API接口）
     * @param shortCode 短码
     * @return 原始链接
     */
    @GetMapping("/get/{shortCode}")
    public ResponseEntity<String> getOriginalUrl(@PathVariable String shortCode) {
        String originalUrl = shortUrlService.getOriginalUrl(shortCode);
        if (originalUrl != null) {
            return ResponseEntity.ok(originalUrl);
        } else {
            return ResponseEntity.notFound().build();
        }
    }
    
    /**
     * 重定向到原始链接
     * @param shortCode 短码
     * @param response HTTP响应对象
     * @throws IOException
     */
    @GetMapping("/{shortCode}")
    public void redirectToOriginalUrl(@PathVariable String shortCode, HttpServletResponse response) throws IOException {
        String originalUrl = shortUrlService.getOriginalUrl(shortCode);
        if (originalUrl != null) {
            // 增加点击次数
            shortUrlService.incrementClickCount(shortCode);
            // 重定向到原始URL
            response.sendRedirect(originalUrl);
        } else {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
        }
    }
    
    // 响应类
    public static class CreateResponse {
        private String shortUrl;
        private String shortCode;
        
        public CreateResponse(String shortUrl, String shortCode) {
            this.shortUrl = shortUrl;
            this.shortCode = shortCode;
        }
        
        public String getShortUrl() { return shortUrl; }
        public void setShortUrl(String shortUrl) { this.shortUrl = shortUrl; }
        
        public String getShortCode() { return shortCode; }
        public void setShortCode(String shortCode) { this.shortCode = shortCode; }
    }
}