package com.github.binarywang.demo.wx.mp.redis;

/**
 * @Author: Godykc
 * @Description:
 * @Date Created at 15:07 2018/1/27
 */
public enum CacheModelEnum {
    一分钟缓存("1MinCache", 60),
    五分钟缓存("5MinCache", 60 * 5),
    十分钟缓存("10MinCache", 60 * 10),
    一小时缓存("1HourCache", 60 * 60),
    两小时缓存("2HourCache", 60 * 60 * 2),
    六小时("6HourCache", 60 * 60 * 6),
    一天缓存("1DayCache", 60 * 60 * 24),
    一月缓存("1MonthCache", 60 * 60 * 24 * 30),
    ;

    public String key; //业务key
    public long expire; //过期时间（秒），小于等于0为永久

    CacheModelEnum(String key, long expire) {
        this.key = key;
        this.expire = expire;
    }
}
