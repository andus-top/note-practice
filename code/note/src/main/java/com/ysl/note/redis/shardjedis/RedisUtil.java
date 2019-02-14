
package com.ysl.note.redis.shardjedis;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.ShardedJedis;
import redis.clients.jedis.params.SetParams;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * Redis工具类，用作参考
 */
public class RedisUtil{

    private static final Logger logger = LoggerFactory.getLogger(RedisUtil.class);

	private ShardedJedisPool shardedJedisPool;

	//新建key并设一个过期时间
	public boolean setNxAndExpire(String key, String val, int expire){
		boolean broken = false;
		boolean success = false;
		ShardedJedis shardedJedis = shardedJedisPool.getRedisClient();
		try {
			long n = shardedJedis.setnx(key, val);
			if(n==1){
				shardedJedis.expire(key, expire);
				success = true;
			}
		} catch (Exception e) {
			broken = true;
			logger.error(e.getMessage(), e);
		} finally {
			shardedJedisPool.returnResource(shardedJedis, broken);
		}
		return success;
	}
	
	/**
	 * 同时设置一个key的值和时间
	 * @param key
	 * @param val
	 * @param expire
	 */
	public boolean setEx(String key, String val, int expire){
        ShardedJedis shardedJedis = shardedJedisPool.getRedisClient();
        boolean broken = false;
        boolean success = false;
        try {
			shardedJedis.setex(key, expire, val);
			success = true;
        } catch (Exception e) {
            broken = true;
            logger.error(e.getMessage(), e);
        } finally {
			shardedJedisPool.returnResource(shardedJedis, broken);
        }
        return success;
    }

	/**
	 * 尝试获取分布式锁
	 * @param requestId 请求标识
	 * @param expireTime 超期时间
	 * @return 是否获取成功
	 */
	public boolean tryGetDistributedLock(String lockType, String requestId, int expireTime) {
		ShardedJedis shardedJedis = shardedJedisPool.getRedisClient();
		String redisLock = String.format("lock_%s", lockType);
		SetParams params = new SetParams();
		params.nx();
		params.xx();
		params.ex(expireTime);
		boolean success = false;
		try {
			while (!success) {
				String result = shardedJedis.set(redisLock, requestId, params);
				if ("OK".equals(result)) {
					success = true;
				}
			}
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
		return success;
	}

	/**
	 * 释放分布式锁
	 * @param requestId 请求标识
	 * @return 是否释放成功
	 */
	public boolean releaseDistributedLock(String lockType, String requestId) {
		ShardedJedis shardedJedis = shardedJedisPool.getRedisClient();
		String redisLock = String.format("lock_%s", lockType);
		boolean success = false;
		try {
			String script = "if redis.call('get', KEYS[1]) == ARGV[1] then return redis.call('del', KEYS[1]) else return 0 end";
			Object result = shardedJedis.getShard(redisLock).eval(script, Collections.singletonList(redisLock), Collections.singletonList(requestId));
			if ("OK".equals(result)) {
				success = true;
			}
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
		return success;
	}

	/**
	 * 同时设置一个key的值和时间
	 * @param key
	 * @param val
	 * @param expire
	 * @return
	 */
	public boolean set(String key, String val, int expire){
		ShardedJedis shardedJedis = shardedJedisPool.getRedisClient();
		boolean broken = false;
		boolean success = false;
		SetParams params = new SetParams();
		params.nx();
		params.xx();
		params.ex(expire);
		try {
			shardedJedis.set(key, val, params);
			success = true;
		} catch (Exception e) {
			broken = true;
			logger.error(e.getMessage(), e);
		} finally {
			shardedJedisPool.returnResource(shardedJedis, broken);
		}
		return success;
	}
	
	public String get(String key){
		ShardedJedis shardedJedis = shardedJedisPool.getRedisClient();
		boolean broken = false;
		String cache = null;
		try {
			cache = shardedJedis.get(key);
			if (StringUtils.isEmpty(cache)) {
				cache = "0";
			}
		} catch (Exception e) {
			broken = true;
			logger.error(e.getMessage(), e);
		} finally {
			shardedJedisPool.returnResource(shardedJedis, broken);
		}
		return cache;
	}

	//修改一个key值并设置过期时间
	public boolean setAndExpire(String key, String val, int expire){
		ShardedJedis shardedJedis = shardedJedisPool.getRedisClient();
		boolean broken = false;
		boolean success = false;
		try {
			shardedJedis.set(key, val);
			shardedJedis.expire(key, expire);
			success = true;
		} catch (Exception e) {
			broken = true;
			logger.error(e.getMessage(), e);
		} finally {
			shardedJedisPool.returnResource(shardedJedis, broken);
		}
		return success;
	}

	//检查key是否存在
	public boolean exists(String key){
		ShardedJedis shardedJedis = shardedJedisPool.getRedisClient();
		boolean broken = false;
		boolean exists = false;
		try {
			exists = shardedJedis.exists(key);
		} catch (Exception e) {
			broken = true;
			logger.error(e.getMessage(), e);
		} finally {
			shardedJedisPool.returnResource(shardedJedis, broken);
		}
		return exists;
	}
	
	public long delete(String key){
		ShardedJedis shardedJedis = shardedJedisPool.getRedisClient();
		boolean broken = false;
		long n = 0;
		try {
			n = shardedJedis.del(key);
		} catch (Exception e) {
			broken = true;
			logger.error(e.getMessage(), e);
		} finally {
			shardedJedisPool.returnResource(shardedJedis, broken);
		}
		return n;
	}
	
	public List<String> mget(String[] keys){
		ShardedJedis shardedJedis = shardedJedisPool.getRedisClient();
		boolean broken = false;
		List<String> result = null;
		try {	
			Collection<Jedis> shards = shardedJedis.getAllShards();
			for(Jedis jedis : shards){
				List<String> list = jedis.mget(keys);
				if(result==null){
					result = list;
				}else{
					int idx = -1;
					for(String val : list){
						idx++;
						if(val==null){
							continue;
						}
						result.set(idx, val);
					}
				}
			}
		} catch (Exception e) {
			broken = true;
			logger.error(e.getMessage(), e);
		} finally {
			shardedJedisPool.returnResource(shardedJedis, broken);
		}
		return result;
	}

}
