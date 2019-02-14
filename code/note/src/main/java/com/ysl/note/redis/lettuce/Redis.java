package com.ysl.note.redis.lettuce;

import io.lettuce.core.ClientOptions;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.connection.*;
import org.springframework.data.redis.connection.lettuce.LettuceClientConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettucePool;
import org.springframework.data.redis.connection.lettuce.LettucePoolingClientConfiguration;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import redis.clients.jedis.*;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 *
 * @author YSL
 * 2019-02-12 10:02
 */
public class Redis {

    private String nodes = "172.16.209.1:6379,172.16.209.131:6380,172.16.209.132:6381";

    public static void main(String[] args) {

        Redis redis = new Redis();
        // FIXME:一直报空指针异常，未测试成功
        redis.stadalone();
        //redis.cluster();
        //redis.sentinel();
    }

    public void stadalone(){
        // 连接池配置
        GenericObjectPoolConfig poolConfig = new GenericObjectPoolConfig();
        LettucePoolingClientConfiguration pool = LettucePoolingClientConfiguration.builder().poolConfig(poolConfig).build();

        // 单机redis
        RedisStandaloneConfiguration standalone = new RedisStandaloneConfiguration();
        standalone.setDatabase(0);
        standalone.setHostName("172.16.209.1");
        standalone.setPort(6379);
        standalone.setPassword(RedisPassword.of(""));
        LettuceConnectionFactory factory = new LettuceConnectionFactory(standalone, pool);

        // 配置redisTemplate
        RedisTemplate<String, Object> redisTemplate = new RedisTemplate<>();
        redisTemplate.setConnectionFactory(factory);
        redisTemplate.setKeySerializer(new StringRedisSerializer());//key序列化
        redisTemplate.setValueSerializer(new GenericJackson2JsonRedisSerializer());//value序列化
        redisTemplate.afterPropertiesSet();

        ValueOperations<String, Object> opsForValue = redisTemplate.opsForValue();
        opsForValue.set("test-standalone","standalone test");
        System.out.println(opsForValue.get("test-standalone").toString());
    }

    public void cluster(){
        // 连接池配置
        GenericObjectPoolConfig poolConfig = new GenericObjectPoolConfig();
        LettucePoolingClientConfiguration pool = LettucePoolingClientConfiguration.builder().poolConfig(poolConfig).build();

        // 集群redis
        RedisClusterConfiguration cluster = new RedisClusterConfiguration();
        cluster.setMaxRedirects(3);
        cluster.setPassword(RedisPassword.of(""));
        Set<RedisNode> clusterNodeses = new HashSet<>();
        String[] node = nodes.split(",");
        for (String n : node) {
            String[] arr = n.split(":");
            clusterNodeses.add(new RedisNode(arr[0],Integer.valueOf(arr[1])));
        }
        cluster.setClusterNodes(clusterNodeses);
        LettuceConnectionFactory factory = new LettuceConnectionFactory(cluster, pool);

        // 配置redisTemplate
        RedisTemplate<String, Object> redisTemplate = new RedisTemplate<>();
        redisTemplate.setConnectionFactory(factory);
        redisTemplate.setKeySerializer(new StringRedisSerializer());//key序列化
        redisTemplate.setValueSerializer(new GenericJackson2JsonRedisSerializer());//value序列化
        redisTemplate.afterPropertiesSet();

        ValueOperations<String, Object> opsForValue = redisTemplate.opsForValue();
        opsForValue.set("test-cluster","cluster test");
        System.out.println(opsForValue.get("test-cluster").toString());
    }

    public void sentinel(){
        // 连接池配置
        GenericObjectPoolConfig poolConfig = new GenericObjectPoolConfig();
        LettucePoolingClientConfiguration pool = LettucePoolingClientConfiguration.builder().poolConfig(poolConfig).build();

        // 哨兵redis
        RedisSentinelConfiguration sentinel = new RedisSentinelConfiguration();
        sentinel.setMaster("redis-master");
        sentinel.setPassword(RedisPassword.of(""));
        sentinel.setDatabase(0);
        Set<RedisNode> sentinels = new HashSet<>();
        String[] node2 = nodes.split(",");
        for (String n : node2) {
            String[] arr = n.split(":");
            sentinels.add(new RedisNode(arr[0],Integer.valueOf(arr[1])));
        }
        sentinel.setSentinels(sentinels);
        LettuceConnectionFactory factory = new LettuceConnectionFactory(sentinel, pool);

        // 配置redisTemplate
        RedisTemplate<String, Object> redisTemplate = new RedisTemplate<>();
        redisTemplate.setConnectionFactory(factory);
        redisTemplate.setKeySerializer(new StringRedisSerializer());//key序列化
        redisTemplate.setValueSerializer(new GenericJackson2JsonRedisSerializer());//value序列化
        redisTemplate.afterPropertiesSet();

        ValueOperations<String, Object> opsForValue = redisTemplate.opsForValue();
        opsForValue.set("test-sentinel","sentinel test");
        System.out.println(opsForValue.get("test-sentinel").toString());
    }
}
