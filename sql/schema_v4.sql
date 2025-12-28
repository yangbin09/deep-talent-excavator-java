-- 数据库初始化脚本 v4
-- 版本: 4.0.0
-- 日期: 2025-12-28
-- 作者: Trae
-- 描述: 在 dt_message 表中添加 thread_name 字段

USE `deep_talent`;

-- ----------------------------
-- 修改 dt_message 表，添加 thread_name 字段
-- ----------------------------
ALTER TABLE `dt_message` ADD COLUMN `thread_name` VARCHAR(255) COMMENT '会话名称/用户姓名';
