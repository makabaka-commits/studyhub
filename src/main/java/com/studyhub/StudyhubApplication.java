package com.studyhub;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@MapperScan("com.studyhub.mapper")
@SpringBootApplication
public class StudyhubApplication {

    public static void main(String[] args) {
        SpringApplication.run(StudyhubApplication.class, args);
    }

}
