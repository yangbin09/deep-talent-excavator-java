-- -----------------------------------------------------
-- Schema deep_talent
-- -----------------------------------------------------

-- 1. 创建会话主表 (dt_conversation)
-- 对应原 conversations 表，拆分了 JSON 字段
CREATE TABLE IF NOT EXISTS `dt_conversation` (
  `thread_id` VARCHAR(64) NOT NULL COMMENT '会话唯一标识符',
  `current_phase` VARCHAR(32) DEFAULT 'childhood' COMMENT '当前对话阶段',
  `dialogue_count` INT DEFAULT 0 COMMENT '对话轮数',
  `need_followup` TINYINT(1) DEFAULT 0 COMMENT '是否需要追问',
  `final_report` TEXT COMMENT '最终分析报告',
  `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`thread_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='会话基础信息表';

-- 2. 创建消息记录表 (dt_message)
-- 存储具体的对话内容，实现一对多关系
CREATE TABLE IF NOT EXISTS `dt_message` (
  `id` BIGINT AUTO_INCREMENT NOT NULL COMMENT '自增主键',
  `thread_id` VARCHAR(64) NOT NULL COMMENT '关联会话ID',
  `role` VARCHAR(16) NOT NULL COMMENT '消息角色(user/assistant/system)',
  `content` TEXT COMMENT '消息内容',
  `sequence` INT NOT NULL DEFAULT 0 COMMENT '消息顺序',
  `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  INDEX `idx_message_thread_id` (`thread_id`) COMMENT '会话ID索引'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='会话消息记录表';

-- 3. 创建特征提取表 (dt_extraction)
-- 存储分析出的用户特征，实现一对多关系
CREATE TABLE IF NOT EXISTS `dt_extraction` (
  `id` BIGINT AUTO_INCREMENT NOT NULL COMMENT '自增主键',
  `thread_id` VARCHAR(64) NOT NULL COMMENT '关联会话ID',
  `phase` VARCHAR(32) NOT NULL COMMENT '所属阶段',
  `tag` VARCHAR(128) COMMENT '特征标签',
  `evidence` TEXT COMMENT '证据文本',
  `confidence` DOUBLE DEFAULT 0 COMMENT '置信度',
  `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  INDEX `idx_extraction_thread_id` (`thread_id`) COMMENT '会话ID索引'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='特征提取记录表';


