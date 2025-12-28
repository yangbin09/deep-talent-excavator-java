package com.deeptalent.config;

import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;

/**
 * 数据库初始化配置类
 * 负责检查和更新数据库 schema
 */
@Configuration
public class DatabaseInitializer {

    private static final Logger log = LoggerFactory.getLogger(DatabaseInitializer.class);

    private final JdbcTemplate jdbcTemplate;

    public DatabaseInitializer(DataSource dataSource) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }

    @PostConstruct
    public void init() {
        log.info("Checking database schema...");
        try {
            // 检查 dt_message 表是否存在 thread_name 列
            String checkColumnSql = "SELECT COUNT(*) FROM information_schema.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'dt_message' AND COLUMN_NAME = 'thread_name'";
            Integer count = jdbcTemplate.queryForObject(checkColumnSql, Integer.class);

            if (count != null && count == 0) {
                log.info("Adding 'thread_name' column to 'dt_message' table...");
                jdbcTemplate.execute("ALTER TABLE dt_message ADD COLUMN thread_name VARCHAR(255) COMMENT '会话名称/用户姓名'");
                log.info("Column 'thread_name' added successfully.");
            } else {
                log.info("Column 'thread_name' already exists in 'dt_message' table.");
            }
        } catch (Exception e) {
            log.error("Failed to check or update database schema", e);
            // 不抛出异常，避免阻断应用启动（如果数据库连接失败，Spring 可能会自己报错）
        }
    }
}
