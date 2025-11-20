package com.example.shorturl.util;

public class UrlCodecUtil {
    
    // 64进制字符集
    private static final String CHARS = "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ-_";
    private static final int BASE = 64;
    
    /**
     * 将数字转换为64进制字符串
     * @param num 要转换的数字
     * @return 64进制字符串
     */
    public static String encode(long num) {
        if (num == 0) {
            return String.valueOf(CHARS.charAt(0));
        }
        
        StringBuilder sb = new StringBuilder();
        while (num > 0) {
            sb.append(CHARS.charAt((int) (num % BASE)));
            num /= BASE;
        }
        return sb.reverse().toString();
    }
    
    /**
     * 将64进制字符串转换为数字
     * @param str 64进制字符串
     * @return 数字
     */
    public static long decode(String str) {
        long num = 0;
        for (char c : str.toCharArray()) {
            num = num * BASE + CHARS.indexOf(c);
        }
        return num;
    }
    
    /**
     * 生成随机起始值，用于确保短码的随机性
     * @return 随机起始值
     */
    public static long generateRandomStart() {
        // 使用当前时间戳的一部分作为随机起始值
        return System.currentTimeMillis() % 1000000 + 100000; // 100000-1099999范围
    }
}