--数据库初始化脚本

--连接数据库控制台
mysql -uroot -p
--创建数据库
CREATE DATABASE seckill;
--使用数据库
use seckill;
CREATE TABLE seckill(
`seckill_id` bigint NOT NULL AUTO_INCREMENT COMMENT '商品库存ID',
`name` varchar(120) NOT NULL COMMENT '商品名称',
`number`  int NOT NULL COMMENT '库存数量',
`start_time` timestamp NOT NULL  COMMENT  '开始时间',
`end_time` timestamp NOT NULL  COMMENT '结束时间',
`create_time` timestamp NOT NULL DEFAULT  CURRENT_TIMESTAMP COMMENT '创建时间',
PRIMARY KEY (seckill_id),
key idx_start_time(start_time),
key idx_end_time(end_time),
key idx_create_time(create_time)
)ENGINE=InnoDB AUTO_INCREMENT=1000 DEFAULT  CHARSET=utf8 COMMENT='秒杀库存表';
--引擎启用支持事务的InnoDB   初始化自增ID 默认编码utf8 注释标注：秒杀库存表

--需要一些初始化数据
insert into seckill(name,number,start_time,end_time)
values ('1000元秒杀iphone8',100,'2017-09-13 00:00:00','2017-09-14 00:00:00'),
              ('1200元秒杀iphoneX',200,'2017-09-13 00:00:00','2017-09-14 00:00:00'),
              ('100元秒杀sp-offer',300,'2017-09-13 00:00:00','2017-09-14 00:00:00'),
              ('10元秒杀offer',400,'2017-09-13 00:00:00','2017-09-14 00:00:00');

--秒杀成功明细表
--用户登陆认证相关信息
CREATE TABLE success_killed(
`seckill_id` bigint NOT NULL  COMMENT '秒杀商品ID',
`user_phone` bigint NOT NULL COMMENT '用户手机号',
`state` tinyint NOT NULL DEFAULT -1  COMMENT '状态显示：-1：无效，0：成功，1：已付款，2：发货',
`create_time` timestamp NOT NULL COMMENT '创建时间',
PRIMARY KEY(seckill_id,user_phone),/*联合主键*/
key idx_create_time(create_time)
)ENGINE=InnoDB  DEFAULT  CHARSET=utf8 COMMENT='秒杀成功明细表';
              

