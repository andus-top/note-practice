package com.ysl.note.redis.jedis;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import redis.clients.jedis.*;


/**
 * redis 配置
 * @author YSL
 * 2018-09-12 11:03
 */
@Configuration(value = "jedisRedis")
@PropertySource("classpath:redis.properties")
public class RedisConfig {

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
     * 非切片连接池
     * @author YSL
     * 2018-09-12 14:00
     */
    @Bean
    public JedisPool getJedisPool(){

        // 连接池
        JedisPoolConfig poolConfig = new JedisPoolConfig();
        // 最大连接数
        poolConfig.setMaxTotal(maxTotal);
        // 最大空闲数
        poolConfig.setMaxIdle(maxIdle);
        // 最大允许等待时间，如果超过这个时间还未获取到连接，则会报JedisException异常：
        // Could not get a resource from the pool
        poolConfig.setMaxWaitMillis(maxWaitMillis);
        // 获得一个jedis实例的时候是否检查连接可用性（ping()）；如果为true，则得到的jedis实例均是可用的；
        poolConfig.setTestOnBorrow(testOnBorrow);
        // 表示idle object evitor两次扫描之间要sleep的毫秒数；
        poolConfig.setTimeBetweenEvictionRunsMillis(timeBetweenEvictionRunsMillis);
        // 如果为true，表示有一个idle object evitor线程对idle object进行扫描，如果validate失败，此object会被从pool中drop掉；
        // 这一项只有在timeBetweenEvictionRunsMillis大于0时才有意义；
        poolConfig.setTestWhileIdle(testWhileIdle);
        // 表示一个对象至少停留在idle状态的最短时间，然后才能被idle object evitor扫描并驱逐；
        // 这一项只有在timeBetweenEvictionRunsMillis大于0时才有意义；
        poolConfig.setMinEvictableIdleTimeMillis(minEvictableIdleTimeMillis);
        // 表示idle object evitor每次扫描的最多的对象数；
        poolConfig.setNumTestsPerEvictionRun(numTestsPerEvictionRun);
        // 在进行returnObject对返回的connection进行validateObject校验
        poolConfig.setTestOnReturn(testOnReturn);

        // 单点redis
        JedisPool standalone = new JedisPool(poolConfig, host, port);

        // cluster redis
        //JedisCluster cluster = new JedisCluster(new HashSet<HostAndPort>(),maxWaitMillis,maxRedirects,poolConfig);

        // sentinel redis
        //JedisSentinelPool sentinelPool = new JedisSentinelPool(master, new HashSet<String>(Arrays.asList(sentinelNodes.split(","))),poolConfig);

        return standalone;
    }
}
