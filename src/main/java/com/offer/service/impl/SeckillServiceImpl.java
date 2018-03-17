package com.offer.service.impl;

import java.util.Date;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.DigestUtils;

import com.offer.dao.SeckillDao;
import com.offer.dao.SuccessKilledDao;
import com.offer.dao.cache.RedisDao;
import com.offer.dto.Exposer;
import com.offer.dto.SeckillExecution;
import com.offer.entity.Seckill;
import com.offer.entity.SuccessKilled;
import com.offer.enums.SeckillStatEnum;
import com.offer.exception.RepeatKillException;
import com.offer.exception.SeckillCloseException;
import com.offer.exception.SeckillException;
import com.offer.service.SeckillService;

@Service
public class SeckillServiceImpl implements SeckillService {

	private Logger logger = LoggerFactory.getLogger(this.getClass());
	// 注入service依赖
	@Autowired
	private SeckillDao seckillDao;
	@Autowired
	private RedisDao redisDao;
	@Autowired
	private SuccessKilledDao successKilledDao;
	// md5盐值字符串，用于混淆md5
	private final String slat = "I@wanna*good|offer";

	/*
	 * 查询全部的秒杀记录
	 */
	@Override
	public List<Seckill> getSeckillList() {
		return seckillDao.queryAll(0, 4);
	}

	/*
	 * 查询单个秒杀记录
	 * 
	 * @param seckillId
	 * 
	 * @return
	 */
	@Override
	public Seckill getById(long seckillId) {
		return seckillDao.queryById(seckillId);
	}

	/*
	 * 展示秒杀接口地址
	 */
	@Override
	public Exposer exportSeckillUrl(long seckillId) {
		//优化点：缓存优化：超时的基础上维护一致性
		//1.访问Redis
		Seckill seckill = redisDao.getSeckill(seckillId);
		if (seckill == null) {
			//2.访问数据库
			seckill = seckillDao.queryById(seckillId);
			if (seckill == null) {
				return new Exposer(false, seckillId);
			} else {
				//3.放入redis
				redisDao.putSeckill(seckill);
			}
		}

		Date startTime = seckill.getStartTime();
		Date endTime = seckill.getEndTime();
		Date nowTime = new Date();
		if (nowTime.getTime() < startTime.getTime() || nowTime.getTime() > endTime.getTime()) {
			return new Exposer(false, seckillId, nowTime.getTime(), startTime.getTime(), endTime.getTime());
		}
		// 转化特定字符串的过程，不可逆
		String md5 = getMD5(seckillId);
		return new Exposer(true, md5, seckillId);
	}

	private String getMD5(long seckillId) {
		String base = seckillId + "/" + slat;
		String md5 = DigestUtils.md5DigestAsHex(base.getBytes());
		return md5;
	}

	/**
	 * 使用注解控制事务方法的优点： 1. 开发团队达成一致约定，明确标注事务的方法的编程风格
	 * 2.保证事务方法的执行时间尽可能短，不要穿插其他网络操作，RPC/HTTP请求或者剥离到事务方法外部（非数据库操作，例如操作缓存）
	 * 3.不是所有的方法都需要事务，如只有1条修改操作，只读操作不需要事务控制
	 * （当我们有两条以上的修改操作需要同时去完成，或者是当我们的只读操作需要select for update这样语句的时候，需要事务）
	 */
	@Override
	@Transactional
	public SeckillExecution executeSeckill(long seckillId, long userPhone, String md5)
			throws SeckillException, RepeatKillException, SeckillCloseException {
		if (md5 == null || !md5.equals(getMD5(seckillId))) {
			throw new SeckillException("seckill data rewrite");
		}
		// 执行秒杀逻辑：减库存+记录购买行为
		Date nowTime = new Date();
		
/*		try {
			// 减库存
			int updateCount = seckillDao.reduceNumber(seckillId, nowTime);
			if (updateCount <= 0) {
				// 没有更新记录，秒杀结束
				throw new SeckillCloseException("seckill is closed.");
			} else {
				// 记录购买行为
				int insertCount = successKilledDao.insertSuccessKilled(seckillId, userPhone);
				// 唯一：seckillId,userPhone
				if (insertCount <= 0) {
					// 重复秒杀
					throw new RepeatKillException("seckill repeated");
				} else {
					// 秒杀成功
					SuccessKilled successKilled = successKilledDao.queryByIdWithSeckill(seckillId, userPhone);
					return new SeckillExecution(seckillId, SeckillStatEnum.SUCCESS, successKilled);
				}
			}

		} 
		*/
		try {
			// 记录购买行为
			int insertCount = successKilledDao.insertSuccessKilled(seckillId, userPhone);
			// 唯一：seckillId,userPhone
			if (insertCount <= 0) {
				// 重复秒杀
				throw new RepeatKillException("seckill repeated");
			} else {
				// 减库存
				int updateCount = seckillDao.reduceNumber(seckillId, nowTime);
				if (updateCount <= 0) {
					// 没有更新记录，秒杀结束
					throw new SeckillCloseException("seckill is closed.");
				} else {
					// 秒杀成功
					SuccessKilled successKilled = successKilledDao.queryByIdWithSeckill(seckillId, userPhone);
					return new SeckillExecution(seckillId, SeckillStatEnum.SUCCESS, successKilled);

				}
			}

		}catch (SeckillCloseException e1) {
			throw e1;
		} catch (RepeatKillException e2) {
			throw e2;
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			// 所有编译期异常，转化为运行期异常
			// Srping声明式事务会对运行期异常进行回滚
			throw new SeckillException("seckill inner error" + e.getMessage());
		}
	}

}
