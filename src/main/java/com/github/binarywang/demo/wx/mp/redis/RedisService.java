package com.github.binarywang.demo.wx.mp.redis;

import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import sun.security.util.Length;

import javax.annotation.Resource;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * @Author: Godykc
 * @Description:
 * @Date Created at 15:06 2018/1/27
 */
@Service
@Slf4j
public class RedisService {
    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    /**
     * 生成key  {appCode}.{modelKey}[.{key}]
     *
     * @param modelEnum
     * @param key
     * @return
     */
    private String generateCacheKey(CacheModelEnum modelEnum, String key) {
        StringBuilder sb = new StringBuilder(modelEnum.key);
        if (key != null) {
            sb.append(".").append(key);
        }
        return sb.toString();
    }

    /**
     * 缓存业务对象专用
     *
     * @param modelEnum 缓存的对象枚举
     * @param key       缓存对象的key，若为null，则为全局唯一缓存
     * @param value     缓存对象的value
     */
    public boolean putModel(CacheModelEnum modelEnum, String key, Object value) {
        if (value == null) {
            return false;
        }
        if (redisTemplate == null) {
            log.error("redisTemplate is null");
            return false;
        }
        try {
            if (modelEnum.expire > 0) {
                redisTemplate.opsForValue().set(generateCacheKey(modelEnum, key), value, modelEnum.expire, TimeUnit.SECONDS);
            } else {
                redisTemplate.opsForValue().set(generateCacheKey(modelEnum, key), value);
            }
            return true;
        } catch (Exception ex) {
            log.error("业务对象缓存失败", ex);
        }
        return false;
    }

    /**
     * 获取业务对象缓存专用
     *
     * @param modelEnum 缓存的对象枚举
     * @param key       缓存对象的key，若为null，则为全局唯一缓存
     * @param <T>
     * @return
     */
    public <T> T getModel(CacheModelEnum modelEnum, String key) {
        if (redisTemplate == null) {
            log.error("redisTemplate is null");
            return null;
        }
        try {
            return (T) redisTemplate.opsForValue().get(generateCacheKey(modelEnum, key));
        } catch (Exception e) {
            log.error("从缓存中获取业务对象失败", e);
        }
        return null;
    }

    /**
     * 删除业务对象缓存专用
     *
     * @param modelEnum 缓存的业务对象枚举
     * @param key       缓存的业务对象key
     * @return
     */
    public void deleteModel(CacheModelEnum modelEnum, String key) {
        if (key == null) {
            return;
        }
        if (redisTemplate == null) {
            log.error("redisTemplate is null");
            return;
        }
        try {
            redisTemplate.delete(generateCacheKey(modelEnum, key));
        } catch (Exception ex) {
            log.error("从缓存中删除业务数据失败");
        }
    }

    /**
     * 插入(覆盖)某个缓存
     */
    public boolean put(String key, Object value, long timeToLive) {
        if (key == null || value == null)
            return false;
        if (redisTemplate == null) {
            log.error("redisTemplate is null");
            return false;
        }
        try {
            if (timeToLive > 0) {
                redisTemplate.opsForValue().set(key, value, timeToLive, TimeUnit.SECONDS);
            } else {
                redisTemplate.opsForValue().set(key, value);
            }
            return true;
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        return false;
    }

    /**
     * 根据key获取缓存
     */
    public <T> T get(String key) {
        if (key == null)
            return null;
        if (redisTemplate == null) {
            log.debug("redisTemplate is null");
            return null;
        }
        try {
            return (T) redisTemplate.opsForValue().get(key);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return null;
        }
    }

    /**
     * 删除某个缓存
     */
    public void delete(String key) {
        if (key == null)
            return;
        if (redisTemplate == null) {
            log.debug("redisTemplate is null");
            return;
        }
        try {
            redisTemplate.delete(key);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
    }

    /**
     * 插原子缓存,同名key已存在返回false
     */
    public boolean setAtomCache(String key, Object value, long timeToLive) {
        if (key == null || value == null)
            return false;
        if (redisTemplate == null) {
            log.error("redisTemplate is null");
            return false;
        }
        boolean success = false;
        try {
            success = redisTemplate.opsForValue().setIfAbsent(key, value);
            log.info("获取原子锁的结果为{}", success);
        } catch (Exception e) {
            log.error("不存在则添加缓存", e);
        }
        if (success && timeToLive > 0) {
            try {
                redisTemplate.expire(key, timeToLive, TimeUnit.SECONDS);
                log.info("{}的过期时间为{}秒", key, redisTemplate.getExpire(key, TimeUnit.SECONDS).toString());
            } catch (Exception e) {
                log.error("不存在则添加缓存-存活时间", e);
            }
        }
        return success;
    }

    public Set<String> getKeysByPrefix(String prefix){
        Set<String> keys = redisTemplate.keys(prefix + "*");
        return keys;
    }

//    public Set<String> getAllKeys(){
//        Set<String> keys = calculateRedisTemplate.keys( "*");
//        return keys;
//    }

//    /**
//     * 插入(覆盖)某个缓存
//     */
//    public boolean putForCalculate(String key, Object value, long timeToLive) {
//        if (key == null || value == null)
//            return false;
//        if (calculateRedisTemplate == null) {
//            log.error("redisTemplate is null");
//            return false;
//        }
//        try {
//            if (timeToLive > 0) {
//                calculateRedisTemplate.opsForValue().set(key, value, timeToLive, TimeUnit.SECONDS);
//            } else {
//                calculateRedisTemplate.opsForValue().set(key, value);
//            }
//            return true;
//        } catch (Exception e) {
//            log.error(e.getMessage(), e);
//        }
//        return false;
//    }

//    /**
//     * 根据key获取缓存
//     */
//    public <T> T getForCalculate(String key) {
//        if (key == null)
//            return null;
//        if (calculateRedisTemplate == null) {
//            log.debug("redisTemplate is null");
//            return null;
//        }
//        try {
//            return (T) calculateRedisTemplate.opsForValue().get(key);
//        } catch (Exception e) {
//            log.error(e.getMessage(), e);
//            return null;
//        }
//    }
}
