package com.ysl.note.redis.signal.lettuce;

import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.CachingConfigurerSupport;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.data.redis.connection.*;
import org.springframework.data.redis.connection.lettuce.*;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;


/**
 * 单点redis配置 lettuce
 * @author YSL
 * 2019-02-12 10:01
 */
@Configuration(value = "lettuceRedis")
@PropertySource(value="classpath:redis.properties")
public class RedisConfig extends CachingConfigurerSupport {

    @Value("${spring.redis.database}")
    private int database;

    @Value("${spring.redis.host}")
    private String host;

    @Value("${spring.redis.port}")
    private int port;

    @Value("${spring.redis.password}")
    private String password;

    @Value("${spring.redis.pool.max-total}")
    private int maxTotal;

    @Value("${spring.redis.pool.max-idle}")
    private int maxIdle;

    @Value("${spring.redis.pool.min-idle}")
    private int minIdle;

    @Value("${spring.redis.pool.maxWaitMillis}")
    private long maxWaitMillis;

    @Value("${spring.redis.pool.timeBetweenEvictionRunsMillis}")
    private long timeBetweenEvictionRunsMillis;

    @Value("${spring.redis.pool.minEvictableIdleTimeMillis}")
    private long minEvictableIdleTimeMillis;

    @Value("${spring.redis.pool.testWhileIdle}")
    private boolean testWhileIdle;

    @Value("${spring.redis.pool.numTestsPerEvictionRun}")
    private int numTestsPerEvictionRun;

    @Value("${spring.redis.pool.testOnBorrow}")
    private boolean testOnBorrow;

    @Value("${spring.redis.pool.testOnReturn}")
    private boolean testOnReturn;

    // sentinel
    @Value("${spring.redis.sentinel.master}")
    private String master;
    @Value("${spring.redis.sentinel.nodes}")
    private String sentinelNodes;

    // cluster
    @Value("${spring.redis.cluster.max-redirects}")
    private int maxRedirects;
    @Value("${spring.redis.cluster.nodes}")
    private String clusterNodes;


    /**
     * RedisTemplate配置
     * @return
     */
    @Bean("lettuceRedisTemplate")
    public RedisTemplate<String, Object> redisTemplate() {

        // 连接池配置
        GenericObjectPoolConfig poolConfig = new GenericObjectPoolConfig();
        poolConfig.setMaxTotal(maxTotal);
        poolConfig.setMaxIdle(maxIdle);
        poolConfig.setMinIdle(minIdle);
        poolConfig.setMaxWaitMillis(maxWaitMillis);
        poolConfig.setTimeBetweenEvictionRunsMillis(timeBetweenEvictionRunsMillis);
        poolConfig.setMinEvictableIdleTimeMillis(minEvictableIdleTimeMillis);
        poolConfig.setTestWhileIdle(testOnBorrow);
        poolConfig.setNumTestsPerEvictionRun(numTestsPerEvictionRun);
        poolConfig.setTestOnBorrow(testOnBorrow);
        poolConfig.setTestOnReturn(testOnReturn);
        LettucePoolingClientConfiguration pool = LettucePoolingClientConfiguration.builder().poolConfig(poolConfig).build();

        // 单机redis
        RedisStandaloneConfiguration standalone = new RedisStandaloneConfiguration();
        standalone.setDatabase(database);
        standalone.setHostName(host);
        standalone.setPort(port);
        standalone.setPassword(RedisPassword.of(password));

        /*// 集群redis
        RedisClusterConfiguration cluster = new RedisClusterConfiguration();
        cluster.setMaxRedirects(maxRedirects);
        cluster.setPassword(RedisPassword.of(password));
        Set<RedisNode> clusterNodeses = new HashSet<>();
        String[] node = clusterNodes.split(",");
        for (String n : node) {
            String[] arr = n.split(":");
            clusterNodeses.add(new RedisNode(arr[0],Integer.valueOf(arr[1])));
        }
        cluster.setClusterNodes(clusterNodeses);

        // 哨兵redis
        RedisSentinelConfiguration sentinel = new RedisSentinelConfiguration();
        sentinel.setMaster(master);
        sentinel.setPassword(RedisPassword.of(password));
        sentinel.setDatabase(database);
        Set<RedisNode> sentinels = new HashSet<>();
        String[] node2 = sentinelNodes.split(",");
        for (String n : node2) {
            String[] arr = n.split(":");
            sentinels.add(new RedisNode(arr[0],Integer.valueOf(arr[1])));
        }
        sentinel.setSentinels(sentinels);*/

        LettuceConnectionFactory factory = new LettuceConnectionFactory(standalone, pool);
        //LettuceClientConfiguration.LettuceClientConfigurationBuilder builder = LettuceClientConfiguration.builder();
        //LettuceConnectionFactory factory = new LettuceConnectionFactory(standalone,builder.build());

        // 配置redisTemplate
        RedisTemplate<String, Object> redisTemplate = new RedisTemplate<>();
        redisTemplate.setConnectionFactory(factory);
        redisTemplate.setKeySerializer(new StringRedisSerializer());//key序列化
        redisTemplate.setValueSerializer(new GenericJackson2JsonRedisSerializer());//value序列化
        redisTemplate.afterPropertiesSet();

        return redisTemplate;
    }

}
