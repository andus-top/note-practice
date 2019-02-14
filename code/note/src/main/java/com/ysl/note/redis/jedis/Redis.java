package com.ysl.note.redis.jedis;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.connection.RedisNode;
import redis.clients.jedis.*;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * @author YSL
 * 2019-02-12 10:02
 */
public class Redis {

    private String nodes = "172.16.209.1:6379,172.16.209.131:6380,172.16.209.132:6381";

    public static void main(String[] args) {
        Redis redis = new Redis();
        redis.stadalone(); // 成功
        //redis.cluster(); // 未测试
        redis.sentinel(); // 成功
    }

    public void stadalone(){
        // 连接池
        JedisPoolConfig poolConfig = new JedisPoolConfig();

        // 单点redis
        JedisPool standalone = new JedisPool(poolConfig, "172.16.209.1", 6379);

        Jedis jedis = standalone.getResource();
        jedis.set("test-stadalone","stadalone test");
        System.out.println(jedis.get("test-stadalone"));
        jedis.close();
    }

    public void cluster(){
        // 连接池
        JedisPoolConfig poolConfig = new JedisPoolConfig();

        Set<HostAndPort> hosts = new HashSet<>();
        String[] node = nodes.split(",");
        for (String n : node) {
            String[] arr = n.split(":");
            hosts.add(new HostAndPort(arr[0],Integer.valueOf(arr[1])));
        }

        // 集群redis
        JedisCluster cluster = new JedisCluster(hosts,1000,3,poolConfig);

        cluster.set("test-cluster","cluster test");
        System.out.println(cluster.get("test-cluster"));
        cluster.close();
    }

    public void sentinel(){
        // 连接池
        JedisPoolConfig poolConfig = new JedisPoolConfig();

        // 哨兵redis
        JedisSentinelPool sentinelPool = new JedisSentinelPool("redis-master", new HashSet<String>(Arrays.asList(nodes.split(","))),poolConfig);

        Jedis jedis = sentinelPool.getResource();
        jedis.set("test-sentinel","sentinel test");
        System.out.println(jedis.get("test-sentinel"));
        jedis.close();
    }

}
