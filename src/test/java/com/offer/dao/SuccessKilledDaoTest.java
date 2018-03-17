package com.offer.dao;

import static org.junit.Assert.*;

import javax.annotation.Resource;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.offer.entity.SuccessKilled;


@RunWith(SpringJUnit4ClassRunner.class)
//告诉junit spring的配置文件
@ContextConfiguration({"classpath:spring/spring-dao.xml"})
public class SuccessKilledDaoTest {

    @Resource
    private SuccessKilledDao successKilledDao;
	@Test
	public void testInsertSuccessKilled() throws Exception{
	
		long seckillId=1000L;
		long userPhone=13609262626L;
		int insertCount=successKilledDao.insertSuccessKilled(seckillId, userPhone);
		System.out.println("insertCount="+insertCount);
		 /* 
		  * 第一次插入Updates: 1
		  * 
		  * 第二次插入 Updates: 0
		  * 成功
		  * */
		
	}

	@Test
	public void testQueryByIdWithSeckill() {
		long seckillId=1000L;
		long userPhone=13609262626L;
		SuccessKilled successkilled=successKilledDao.queryByIdWithSeckill(seckillId, userPhone);
		System.out.println(successkilled);
		System.out.println(successkilled.getSeckill());
		
		/*
		 * SuccessKilled [seckillId=1000, userPhone=13609262626, state=0,
		 * createTime=Tue Sep 19 16:16:26 CST 2017, 
		 * seckill=Seckill[seckill_id=0,name='1000元秒杀iphone8', number=100, startTime=Wed Sep 13
		 * 00:00:00 CST 2017, endTime=Thu Sep 14 00:00:00 CST 2017,
		 * createTime=Wed Sep 13 23:47:37 CST 2017]] 
		 * 
		 * Seckill [seckill_id=0,name='1000元秒杀iphone8', number=100, startTime=Wed Sep 13
		 * 00:00:00 CST 2017, endTime=Thu Sep 14 00:00:00 CST 2017,
		 * createTime=Wed Sep 13 23:47:37 CST 2017]
		 */
		
	}

}
