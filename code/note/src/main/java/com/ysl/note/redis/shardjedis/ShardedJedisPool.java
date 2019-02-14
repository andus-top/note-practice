package com.ysl.note.redis.shardjedis;

import org.apache.commons.pool2.PooledObject;
import org.apache.commons.pool2.PooledObjectFactory;
import org.apache.commons.pool2.impl.DefaultPooledObject;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import redis.clients.jedis.*;
import redis.clients.jedis.exceptions.JedisConnectionException;
import redis.clients.jedis.util.Hashing;
import redis.clients.jedis.util.Pool;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Logger;
import java.util.regex.Pattern;

/**
 * redis分布式集群
 * @author YSL
 * 2018-09-08 13:32
 */
@Configuration
@PropertySource(value="classpath:redis.properties")
public class ShardedJedisPool extends Pool<ShardedJedis> {

	private static final org.slf4j.Logger logger = LoggerFactory.getLogger(ShardedJedisPool.class);
	private final Logger log = Logger.getLogger(getClass().getName());
	
	// 最大重连次数
	private static final int MAX_RETRY_TIMES = 10;
	// 当前重连次数
	private int retryTime = 0;

    private Set<MasterListener> masterListeners = new HashSet<MasterListener>();

    // 线程A对一个volatile变量的修改，对于其它线程来说是可见的，即线程每次获取volatile变量的值都是最新的。
    private volatile List<HostAndPort> currentHostMasters;

	private GenericObjectPoolConfig poolConfig;
	private static volatile ShardedJedisPool shardedJedisPool = null;

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
	private int maxWaitMillis;

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

    public ShardedJedisPool(){}

	public ShardedJedisPool(List<String> masters, Set<String> sentinels, GenericObjectPoolConfig poolConfig) {
		this.poolConfig = poolConfig;

		// 得到master 所在的ip、端口集合
		List<HostAndPort> masterList = initSentinels(sentinels, masters);
		// 为master 所在的主机创建连接池
		initPool(masterList);
	}

	@Bean
	public ShardedJedisPool getShardedJedisPool() {
		if (null == shardedJedisPool) {
			synchronized (ShardedJedisPool.class) {
				if (null == shardedJedisPool) {
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

					List<String> masters = new ArrayList<>();
					masters.add(master);

					shardedJedisPool = new ShardedJedisPool(masters, new HashSet<String>(Arrays.asList(sentinelNodes.split(","))), poolConfig);
				}
			}
		}

		return shardedJedisPool;
	}

	public ShardedJedis getRedisClient() {
		try {
			return super.getResource();
		} catch (Exception e) {
			logger.error("getRedisClent error", e);
		}
		return null;
	}

	public void destroy() {
		for (MasterListener m : masterListeners) {
			m.shutdown();
		}
		super.destroy();
	}

	public void returnResource(ShardedJedis shardedJedis) {
		super.returnResource(shardedJedis);
	}

	public void returnResource(ShardedJedis shardedJedis, boolean broken) {
		if (broken) {
			super.returnBrokenResource(shardedJedis);
		} else {
			super.returnResource(shardedJedis);
		}
	}

	/**
	 * 启动所有哨兵
	 * @param sentinels node:port格式的集合
	 * @param masters masterName的集合
	 * @author YSL
	 * 2019-02-12 16:04
	 */
	private List<HostAndPort> initSentinels(Set<String> sentinels, List<String> masters) {

		/**
		 * key: masterName
		 * value: 该master所在ip、port
		 */
    	Map<String, HostAndPort> masterMap = new HashMap<String, HostAndPort>();

    	// mater所在的ip、port
    	List<HostAndPort> shardMasters = new ArrayList<HostAndPort>();
	    
	    for (String masterName : masters) {
	    	HostAndPort master = null;
	    	boolean find = false;
			/**
			 * 查找masterName所在主机，总共最多重试${MAX_RETRY_TIMES}次
			 * 找到放入masterMap、shardMasters中
			 */
	    	while (!find && retryTime < MAX_RETRY_TIMES) {
	    		for (String sentinel : sentinels) {
					final HostAndPort hap = toHostAndPort(Arrays.asList(sentinel.split(":")));
			
					try {
					    Jedis jedis = new Jedis(hap.getHost(), hap.getPort());
					    master = masterMap.get(masterName);
					    if (master == null) {
					    	// 通过masterName找到master所在主机的ip、port
					    	List<String> hostAndPort = jedis.sentinelGetMasterAddrByName(masterName);
					    	if (hostAndPort != null && hostAndPort.size() > 0) {
					    		master = toHostAndPort(hostAndPort);
								shardMasters.add(master);
								masterMap.put(masterName, master);
								find = true;
								jedis.disconnect();
								break;
					    	}
					    }
					} catch (JedisConnectionException e) {
					    e.printStackTrace();
					}
		    	}

	    		// 未找到masterName所在主机，
		    	if (null == master) {
		    		try {
						log.severe("wait "+maxWaitMillis+"ms, Will try again.");
						Thread.sleep(1000);
				    } catch (InterruptedException e) {
				    	e.printStackTrace();
				    }
		    		find = false;
		    		retryTime++;
		    	}
	    	}
	    	
	    	// 有master没有找到，且已经尝试10次
	    	if (!find && retryTime >= MAX_RETRY_TIMES) {
	    		throw new JedisConnectionException("cannot connect all mater. abort");
	    	}
	    }
	    
	    // 所有的master都找到
	    if (masters.size() != 0 && masters.size() == shardMasters.size()) {
	    	
	    	// 哨兵监听
			for (String sentinel : sentinels) {
			    HostAndPort hap = toHostAndPort(Arrays.asList(sentinel.split(":")));
			    // 创建连接池
			    MasterListener masterListener = new MasterListener(masters, hap.getHost(), hap.getPort());
			    masterListeners.add(masterListener);
			    masterListener.start();
			}
	    }
	    
		return shardMasters;
    }

	private void initPool(List<HostAndPort> masters) {
		// currentHostMasters与masters中有一个不相等时
		if (!isSame(currentHostMasters, masters)) {
			StringBuffer sb = new StringBuffer();
			for (HostAndPort master : masters) {
				sb.append(master.toString());
				sb.append(" ");
			}
			List<JedisShardInfo> shardMasters = makeShardInfoList(masters);
			// 为${masters}中的master创建连接池
			initPool(poolConfig, new ShardedJedisFactory(shardMasters, Hashing.MURMUR_HASH, null));
			currentHostMasters = masters;
		}
	}

	/**
	 * 全部相等返回true
	 * 有一个不相等返回false
	 */
	private boolean isSame(List<HostAndPort> currentShardMasters, List<HostAndPort> shardMasters) {
		if (currentShardMasters != null && shardMasters != null) {
			if (currentShardMasters.size() == shardMasters.size()) {
				for (int i = 0; i < currentShardMasters.size(); i++) {
					if (!currentShardMasters.get(i).equals(shardMasters.get(i))) return false;
				}
				return true;
			}
		}
		return false;
	}

	private List<JedisShardInfo> makeShardInfoList(List<HostAndPort> masters) {
		List<JedisShardInfo> shardMasters = new ArrayList<JedisShardInfo>();
		for (HostAndPort master : masters) {
			JedisShardInfo jedisShardInfo = new JedisShardInfo(master.getHost(), master.getPort(), maxWaitMillis);
			jedisShardInfo.setPassword(password);

			shardMasters.add(jedisShardInfo);
		}
		return shardMasters;
	}

    private HostAndPort toHostAndPort(List<String> nodeAndPort) {
    	String host = nodeAndPort.get(0);
    	int port = Integer.parseInt(nodeAndPort.get(1));
    	return new HostAndPort(host, port);
    }

	class MasterListener extends Thread {

		// jedis订阅消息出错，sleep时间
		private long subscribeRetryWaitTime = 5000;
		private List<String> masters;
		private String host;
		private int port;
		private Jedis jedis;
		private AtomicBoolean running = new AtomicBoolean(false);

		public MasterListener() {
		}

		public MasterListener(List<String> masters, String host, int port) {
			this.masters = masters;
			this.host = host;
			this.port = port;
		}

		public MasterListener(List<String> masters, String host, int port,long subscribeRetryWaitTime) {
			this(masters, host, port);
			this.subscribeRetryWaitTime = subscribeRetryWaitTime;
		}

		public void run() {
			running.set(true);
			while (running.get()) {
				jedis = new Jedis(host, port);
				try {
					// jedis的一般模式设置频道。订阅得到信息在将会lister的onMessage(…)方法或者onPMessage(…)中进行进行处理
					jedis.subscribe(new JedisPubSub() {
						/**
						 * channel：频道
						 * message：收到的消息
						 */
						@Override
						public void onMessage(String channel, String message) {
							log.fine("recoive from " + host + ":" + port + " message is: " + message + ".");

							String[] switchMasterMsg = message.split(" ");

							if (switchMasterMsg.length > 3) {

								int index = masters.indexOf(switchMasterMsg[0]);
								if (index >= 0) {
									HostAndPort newHostMaster = toHostAndPort(Arrays.asList(switchMasterMsg[3], switchMasterMsg[4]));
									List<HostAndPort> newHostMasters = new ArrayList<HostAndPort>();
									for (int i = 0; i < masters.size(); i++) {
										newHostMasters.add(null);
									}
									Collections.copy(newHostMasters, currentHostMasters);
									newHostMasters.set(index, newHostMaster);

									// 收到订阅消息后，为其创建连接池
									initPool(newHostMasters);
								} else {
									// 忽略不在masters中的masterName
								}

							} else {
								log.severe("invalid message received from"
										+ host + ":" + port + " on channel: "+ channel + ",and message is: "+message);
							}
						}
					},
				"channel"/*可以有多个频道*/);

				} catch (JedisConnectionException e) {

					if (running.get()) {
						log.severe("Lost connection at " + host+ ":" + port
								+ ". Sleeping " + subscribeRetryWaitTime+"ms and retry...");
						try {
							Thread.sleep(subscribeRetryWaitTime);
						} catch (InterruptedException e1) {
							e1.printStackTrace();
						}
					} else {
						log.fine("unsubscribing from " + host + ":"+ port);
					}
				}
			}
		}

		public void shutdown() {
			try {
				log.fine("shutting down on " + host + ":" + port);
				running.set(false);
				jedis.close();
			} catch (Exception e) {
				log.severe("shutdown fail: " + e.getMessage());
			}
		}
	}

	class ShardedJedisFactory implements PooledObjectFactory<ShardedJedis> {
		private List<JedisShardInfo> shards;
		private Hashing algo;
		private Pattern keyTagPattern;

		public ShardedJedisFactory(List<JedisShardInfo> shards, Hashing algo, Pattern keyTagPattern) {
			this.shards = shards;
			this.algo = algo;
			this.keyTagPattern = keyTagPattern;
		}

		// 创建一个连接池
		public PooledObject<ShardedJedis> makeObject() throws Exception {
			ShardedJedis jedis = new ShardedJedis(shards, algo, keyTagPattern);
			return new DefaultPooledObject<ShardedJedis>(jedis);
		}

		// 销毁实例
		public void destroyObject(PooledObject<ShardedJedis> pooledShardedJedis) throws Exception {
			final ShardedJedis shardedJedis = pooledShardedJedis.getObject();
			for (Jedis jedis : shardedJedis.getAllShards()) {
				try {
					try {
						jedis.quit();
					} catch (Exception e) {
						e.printStackTrace();
					}
					jedis.disconnect();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}

		// 确保线程池返回的是安全可用的实例
		public boolean validateObject(PooledObject<ShardedJedis> pooledShardedJedis) {
			try {
				ShardedJedis jedis = pooledShardedJedis.getObject();
				for (Jedis shard : jedis.getAllShards()) {
					if (!"PONG".equals(shard.ping())) {
						return false;
					}
				}
				return true;
			} catch (Exception ex) {
				ex.printStackTrace();
				return false;
			}
		}

		public void activateObject(PooledObject<ShardedJedis> p) throws Exception {

		}

		public void passivateObject(PooledObject<ShardedJedis> p) throws Exception {

		}
	}

	public List<HostAndPort> getCurrentHostMaster() {
		return currentHostMasters;
	}

}
