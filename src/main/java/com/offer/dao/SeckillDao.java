package com.offer.dao;

import java.util.Date;
import java.util.List;

import org.apache.ibatis.annotations.Param;

import com.offer.entity.Seckill;

public interface SeckillDao {

	/**
	 * 减库存
	 * @param seckillId 减库存的商品
	 * @param killTime 执行减库存的时间
	 * @return 如果影响行数>1，表示更新的记录行数.=0表示失败
	 */
	int reduceNumber(@Param("seckillId") long seckillId,@Param("killTime") Date killTime);
	
	/**
	 * 根据ID查询秒杀库存对象
	 * @param seckillId
	 * @return
	 */
	Seckill queryById(long seckillId);
	
	/**
	 * 根据偏移量查询秒杀商品列表
	 * @param offet 偏移量
	 * @param limit 
	 * @return
	 */
	List<Seckill> queryAll(@Param("offet") int offet,@Param("limit") int limit);
	
}
