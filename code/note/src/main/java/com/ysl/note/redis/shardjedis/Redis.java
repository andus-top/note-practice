package com.ysl.note.redis.shardjedis;

import redis.clients.jedis.ShardedJedis;

/**
 *
 * @author YSL
 * 2019-02-12 10:02
 */
public class Redis {

    private String nodes = "172.16.209.1:6379,172.16.209.131:6380,172.16.209.132:6381";

    public static void main(String[] args) {
        Redis redis = new Redis();
        // 未测试
    }

    public void stadalone(){
        ShardedJedisPool shardedJedisPool = new ShardedJedisPool().getShardedJedisPool();
        ShardedJedis shardedJedis = shardedJedisPool.getRedisClient();
        shardedJedis.setex("shared", 10, "shardedJedis");
        shardedJedis.get("shared");
        shardedJedisPool.destroy();
    }


}
