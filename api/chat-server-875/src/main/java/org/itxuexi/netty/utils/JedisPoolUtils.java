package org.itxuexi.netty.utils;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

import java.time.Duration;

public class JedisPoolUtils {
//    redis:
//    host: 127.0.0.1
//    port: 6379
//    database: 0
//    password: lyl51nju
    private static final JedisPool jedisPool;
    static {
        // 配置连接池
        JedisPoolConfig poolConfig = new JedisPoolConfig();
        // 最大连接数
        poolConfig.setMaxTotal(10);
        // 最大空闲连接数
        poolConfig.setMaxIdle(10);
        // 最小空闲连接数
        poolConfig.setMinIdle(5);
        // 最长等待时间 ms
        poolConfig.setMaxWait(Duration.ofMillis(1500));

        jedisPool = new JedisPool(poolConfig,
                "127.0.0.1",
                6379,
                1000,
                "lyl51nju");
    }

    public static Jedis getJedis() {
        return jedisPool.getResource();
    }
}
