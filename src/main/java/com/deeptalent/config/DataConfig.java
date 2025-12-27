package com.deeptalent.config;

import jakarta.annotation.PostConstruct;
import org.springframework.context.annotation.Configuration;

import java.io.File;

/**
 * 数据配置类，负责数据存储相关的初始化工作
 * 使用@Configuration注解标识为Spring配置类，在应用启动时自动加载
 */
@Configuration
public class DataConfig {

    /**
     * 应用初始化方法，在依赖注入完成后自动执行
     * 使用@PostConstruct注解确保该方法在Spring容器启动时自动调用
     * 主要功能：检查并创建数据存储目录，避免后续操作因目录不存在而失败
     */
    @PostConstruct
    public void init() {
        // 创建指向"data"目录的File对象
        File dataDir = new File("data");
        // 检查目录是否存在，如果不存在则创建
        if (!dataDir.exists()) {
            // 使用mkdirs()而非mkdir()，确保能创建多级目录
            dataDir.mkdirs();
        }
    }
}