package org.springblade.common.utils;

import org.springblade.core.redis.cache.BladeRedis;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * Redis工具类 (适配 BladeRedis)
 *
 * @author BladeX
 * @since 2026-01-27
 */
@Component
public class RedisUtils {

    @Autowired
    private BladeRedis bladeRedis;

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    public StringRedisTemplate getRedisTemplate() {
        return this.stringRedisTemplate;
    }

    /**
     * 根据前缀获取所有的key
     */
    public Set<String> getListKey(String prefix) {
        return stringRedisTemplate.keys(prefix.concat("*"));
    }

    /**
     * 删除key
     */
    public void delete(String key) {
        bladeRedis.del(key);
    }

    /**
     * 批量删除key
     */
    public void delete(Collection<String> keys) {
        bladeRedis.del(keys);
    }

    /**
     * 是否存在key
     */
    public Boolean hasKey(String key) {
        return bladeRedis.exists(key);
    }

    /**
     * 设置过期时间
     */
    public Boolean expire(String key, long timeout, TimeUnit unit) {
        return stringRedisTemplate.expire(key, timeout, unit);
    }

    /**
     * 设置值
     */
    public void set(String key, Object value) {
        bladeRedis.set(key, value);
    }

    /**
     * 设置值带过期时间
     */
    public void set(String key, Object value, long time) {
        bladeRedis.setEx(key, value, time);
    }

    /**
     * 获取值
     */
    public <T> T get(String key) {
        return (T) bladeRedis.get(key);
    }

    /**
     * Hash Set
     */
    public void hPut(String key, String hashKey, Object value) {
        bladeRedis.hSet(key, hashKey, value);
    }

    /**
     * Hash Delete
     */
    public void hDelete(String key, Object... hashKeys) {
        bladeRedis.hDel(key, hashKeys);
    }

    /**
     * Hash Get All
     */
    public Map<Object, Object> hGetAll(String key) {
        return stringRedisTemplate.opsForHash().entries(key);
    }

    /**
     * List Range
     */
    public List<String> lRange(String key, long start, long end) {
        return stringRedisTemplate.opsForList().range(key, start, end);
    }

    /**
     * List Left Push
     */
    public void lLeftPush(String key, String value) {
        stringRedisTemplate.opsForList().leftPush(key, value);
    }

    /**
     * List Remove
     */
    public void lRemove(String key, long count, Object value) {
        stringRedisTemplate.opsForList().remove(key, count, value);
    }



/**
 * Set Add
 */
public Long sAdd(String key, String... values) {
return stringRedisTemplate.opsForSet().add(key, values);
}

/**
 * Set Remove
 */
public Long sRemove(String key, Object... values) {
return stringRedisTemplate.opsForSet().remove(key, values);
}

/**
 * Set IsMember
 */
public Boolean sIsMember(String key, Object value) {
return stringRedisTemplate.opsForSet().isMember(key, value);
}

/**
 * Set Members
 */
public Set<String> sMembers(String key) {
return stringRedisTemplate.opsForSet().members(key);
}
}