package com.paul.appgen;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.paul.appgen.mapper")
public class AppGenApplication {

    public static void main(String[] args) {
        SpringApplication.run(AppGenApplication.class, args);
    }

}
