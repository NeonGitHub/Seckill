package com.offer.dao.cache;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.dyuproject.protostuff.LinkedBuffer;
import com.dyuproject.protostuff.ProtobufIOUtil;
import com.dyuproject.protostuff.ProtostuffIOUtil;
import com.dyuproject.protostuff.runtime.RuntimeSchema;
import com.offer.entity.Seckill;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

public class RedisDao {

	private final Logger logger = LoggerFactory.getLogger(this.getClass());
	private final JedisPool jedisPool;

	private RuntimeSchema<Seckill> schema = RuntimeSchema.createFrom(Seckill.class);

	public RedisDao(String ip, int port) {
		jedisPool = new JedisPool(ip, port);
	}

	public Seckill getSeckill(long seckillId) {
		try {
			Jedis jedis = jedisPool.getResource();
			try {
				/*
				 * redis内部没有实现序列化操作
				 * 流程:redis.get->bytes[]->反序列化得到对像->Object(Seckill) 采用自定义序列化
				 * 要使用protostuff，那么对象一定要是标准pojo（set,get）
				 * 通过schema得到这个对象的标准，然后进行反序列化注入对象
				 */
				String key = "seckill:" + seckillId;
				// 从缓存中获取到序列化的二进制数组
				byte[] bytes = jedis.get(key.getBytes());
				// 如果存在
				if (bytes != null) {
					// 创建一个空对象，做容器
					Seckill seckill = schema.newMessage();
					// 二进制字节数组反序列化，根据schema得到的标准注入到seckill空对象中
					ProtobufIOUtil.mergeFrom(bytes, seckill, schema);
					return seckill;
				}
			} finally {
				jedis.close();
			}
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
		return null;
	}

	public String putSeckill(Seckill seckill) {
		// set Object(Seckill) ->序列化->byte[]
		try {
			Jedis jedis = jedisPool.getResource();
			try {
				String key = "seckill:" + seckill.getSeckillId();
				// 进行序列化，LinkedBuffer.allocation缓存器
				byte[] bytes = ProtostuffIOUtil.toByteArray(seckill, schema,
						LinkedBuffer.allocate(LinkedBuffer.DEFAULT_BUFFER_SIZE));
				int timeout = 60 * 60;
				String result = jedis.setex(key.getBytes(), timeout, bytes);
				return result;
			} finally {
				jedis.close();
			}
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
		return null;
	}

}
