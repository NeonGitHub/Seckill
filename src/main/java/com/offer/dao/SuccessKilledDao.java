package com.offer.dao;

import org.apache.ibatis.annotations.Param;

import com.offer.entity.SuccessKilled;


public interface SuccessKilledDao {

	
	/**
	 * 插入购买明细，可过滤重复
	 * @param seckillId
	 * @param userPhone
	 * @return 插入的行数，返回0表示插入失败
	 */
	int insertSuccessKilled(@Param("seckillId") long seckillId,@Param("userPhone") long userPhone);
	
	
	
	/**
	 * 根据id查询successkilled并携带秒杀产品对象实体
	 * @param SeckillId
	 * @return
	 */
	SuccessKilled queryByIdWithSeckill(@Param("seckillId") long seckillId,@Param("userPhone") long userPhone);
	
	
}