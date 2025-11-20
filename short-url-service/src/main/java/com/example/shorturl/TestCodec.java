package com.example.shorturl;

import com.example.shorturl.util.UrlCodecUtil;

public class TestCodec {
    public static void main(String[] args) {
        System.out.println("Testing URL Codec Utility...");
        
        // 测试基本编码解码功能
        long original = 12345L;
        String encoded = UrlCodecUtil.encode(original);
        long decoded = UrlCodecUtil.decode(encoded);
        
        System.out.println("Original: " + original + " -> Encoded: " + encoded + " -> Decoded: " + decoded);
        System.out.println("Match: " + (original == decoded));
        
        // 测试大数字的编码解码
        original = 1000000L;
        encoded = UrlCodecUtil.encode(original);
        decoded = UrlCodecUtil.decode(encoded);
        
        System.out.println("Original: " + original + " -> Encoded: " + encoded + " -> Decoded: " + decoded);
        System.out.println("Match: " + (original == decoded));
        
        // 测试0的编码
        original = 0L;
        encoded = UrlCodecUtil.encode(original);
        decoded = UrlCodecUtil.decode(encoded);
        
        System.out.println("Original: " + original + " -> Encoded: " + encoded + " -> Decoded: " + decoded);
        System.out.println("Match: " + (original == decoded));
        
        // 测试随机起始值生成
        long start1 = UrlCodecUtil.generateRandomStart();
        long start2 = UrlCodecUtil.generateRandomStart();
        
        System.out.println("Random start 1: " + start1);
        System.out.println("Random start 2: " + start2);
        
        System.out.println("All tests completed successfully!");
    }
}