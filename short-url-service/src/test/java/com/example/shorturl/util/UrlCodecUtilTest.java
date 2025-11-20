package com.example.shorturl.util;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class UrlCodecUtilTest {

    @Test
    public void testEncodeDecode() {
        // 测试基本编码解码功能
        long original = 12345L;
        String encoded = UrlCodecUtil.encode(original);
        long decoded = UrlCodecUtil.decode(encoded);
        
        assertEquals(original, decoded, "编码解码应该保持数值不变");
        System.out.println("Original: " + original + " -> Encoded: " + encoded + " -> Decoded: " + decoded);
    }
    
    @Test
    public void testEncodeDecodeLargeNumber() {
        // 测试大数字的编码解码
        long original = 1000000L;
        String encoded = UrlCodecUtil.encode(original);
        long decoded = UrlCodecUtil.decode(encoded);
        
        assertEquals(original, decoded, "大数字编码解码应该保持数值不变");
        System.out.println("Original: " + original + " -> Encoded: " + encoded + " -> Decoded: " + decoded);
    }
    
    @Test
    public void testEncodeZero() {
        // 测试0的编码
        long original = 0L;
        String encoded = UrlCodecUtil.encode(original);
        long decoded = UrlCodecUtil.decode(encoded);
        
        assertEquals(original, decoded, "0的编码解码应该保持数值不变");
        assertEquals("0", encoded, "0应该编码为'0'");
        System.out.println("Original: " + original + " -> Encoded: " + encoded + " -> Decoded: " + decoded);
    }
    
    @Test
    public void testRandomStartGeneration() {
        // 测试随机起始值生成
        long start1 = UrlCodecUtil.generateRandomStart();
        long start2 = UrlCodecUtil.generateRandomStart();
        
        assertTrue(start1 >= 100000, "随机起始值应该大于等于100000");
        assertTrue(start1 < 1100000, "随机起始值应该小于1100000");
        System.out.println("Random start 1: " + start1);
        System.out.println("Random start 2: " + start2);
    }
}