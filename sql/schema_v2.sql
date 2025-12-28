-- 数据库初始化脚本
-- 版本: 2.0.0
-- 日期: 2025-12-28
-- 作者: 小阳

-- 创建数据库 (如果不存在)
CREATE DATABASE IF NOT EXISTS `deep_talent` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE `deep_talent`;

-- ----------------------------
-- 1. 会话主表 (dt_conversation)
-- ----------------------------
DROP TABLE IF EXISTS `dt_conversation`;
CREATE TABLE `dt_conversation` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `thread_id` varchar(64) NOT NULL COMMENT '会话唯一标识',
  `current_phase` varchar(32) DEFAULT 'childhood' COMMENT '当前阶段',
  `dialogue_count` int(11) DEFAULT 0 COMMENT '对话轮次',
  `need_followup` tinyint(1) DEFAULT 0 COMMENT '是否需要追问',
  `last_eval` text COMMENT '最近一次评估结果(JSON)',
  `final_report` longtext COMMENT '最终报告',
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_thread_id` (`thread_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='深度天赋挖掘-会话表';

-- ----------------------------
-- 2. 消息记录表 (dt_message)
-- ----------------------------
DROP TABLE IF EXISTS `dt_message`;
CREATE TABLE `dt_message` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `thread_id` varchar(64) NOT NULL COMMENT '所属会话ID',
  `role` varchar(16) NOT NULL COMMENT '角色(user/assistant/system)',
  `content` longtext COMMENT '消息内容',
  `sequence` int(11) DEFAULT 0 COMMENT '消息顺序',
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_thread_id` (`thread_id`),
  CONSTRAINT `fk_msg_thread` FOREIGN KEY (`thread_id`) REFERENCES `dt_conversation` (`thread_id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='深度天赋挖掘-消息记录表';

-- ----------------------------
-- 3. 特征提取表 (dt_extraction)
-- ----------------------------
DROP TABLE IF EXISTS `dt_extraction`;
CREATE TABLE `dt_extraction` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `thread_id` varchar(64) NOT NULL COMMENT '所属会话ID',
  `phase` varchar(32) DEFAULT NULL COMMENT '所属阶段',
  `tag` varchar(64) DEFAULT NULL COMMENT '特征标签',
  `evidence` text COMMENT '证据文本',
  `confidence` double DEFAULT NULL COMMENT '置信度',
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_thread_id_phase` (`thread_id`, `phase`),
  CONSTRAINT `fk_ext_thread` FOREIGN KEY (`thread_id`) REFERENCES `dt_conversation` (`thread_id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='深度天赋挖掘-特征提取表';


