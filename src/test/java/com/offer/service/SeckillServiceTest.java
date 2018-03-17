package com.offer.service;

import static org.junit.Assert.*;

import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.offer.dto.Exposer;
import com.offer.dto.SeckillExecution;
import com.offer.entity.Seckill;
import com.offer.exception.RepeatKillException;
import com.offer.exception.SeckillCloseException;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({ "classpath:spring/spring-dao.xml", "classpath:spring/spring-service.xml" })
public class SeckillServiceTest {

	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	@Autowired
	private SeckillService seckillService;

	@Test
	public void testGetSeckillList() {
		List<Seckill> list = seckillService.getSeckillList();
		logger.info("list={}", list);
	}

	@Test
	public void testGetById() {
		long id = 1000;
		Seckill seckill = seckillService.getById(id);
		logger.info("seckill={}", seckill);
	}

	@Test
	public void testExportSeckillUrl() {
		/*
		 * exposed=false, 没有到时间段的情况 md5='null', seckillId=1000,
		 * now=1505977199765, start=1505232000000, end=1505318400000
		 */
		/*
		 * exposed=true, md5='ea962d9f468cd29b47cf92de6b59a1af', 秒杀开启的测试结果
		 * seckillId=1000, now=0, start=0, end=0
		 */
		long id = 1000;
		Exposer exposer = seckillService.exportSeckillUrl(id);
		logger.info("exposer={}", exposer);
	}

	@Test
	public void testExecuteSeckill() {
		long id = 1000;
		long phone = 13609262623L;
		String md5 = "ea962d9f468cd29b47cf92de6b59a1af";
		try {
			SeckillExecution excution = seckillService.executeSeckill(id, phone, md5);
			logger.info("excution={}", excution);
		} catch (RepeatKillException e) {
			logger.error(e.getMessage());
		} catch (SeckillCloseException e) {
			logger.error(e.getMessage());
		}

		/*
		 * excution=SeckillExecution{ seckillId=1000, state=1, stateInfo='秒杀成功',
		 * successKilled=SuccessKilled [seckillId=1000, userPhone=13609262623,
		 * state=0, createTime=Thu Sep 21 15:20:29 CST 2017, seckill=Seckill
		 * [seckill_id=0,name='1000元秒杀iphone8', number=99, startTime=Thu Sep 21
		 * 15:20:29 CST 2017, endTime=Fri Sep 22 00:00:00 CST 2017,
		 * createTime=Wed Sep 13 23:47:37 CST 2017]]}
		 */
	}
	//集成测试完整逻辑，整合测试“秒杀接口展示”+“开始秒杀”方法
	@Test
	public void testSeckillLogic() {
		long id = 1001;
		Exposer exposer = seckillService.exportSeckillUrl(id);
		if (exposer.isExposed()) {
			logger.info("exposer={}", exposer);
			long phone = 13609262623L;
			String md5 = exposer.getMd5();
			try {
				SeckillExecution excution = seckillService.executeSeckill(id, phone, md5);
				logger.info("excution={}", excution);
			} catch (RepeatKillException e) {
				logger.error(e.getMessage());
			} catch (SeckillCloseException e) {
				logger.error(e.getMessage());
			}

		} else {
			logger.warn("exposer={}", exposer);
		}

	}

}
