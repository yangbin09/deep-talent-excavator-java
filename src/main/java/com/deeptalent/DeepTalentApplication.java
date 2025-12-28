package com.deeptalent;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.deeptalent.mapper")
public class DeepTalentApplication {

    public static void main(String[] args) {
        SpringApplication.run(DeepTalentApplication.class, args);
    }

}
