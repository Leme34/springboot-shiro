package com.lee;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@MapperScan("com.lee.dao")
@SpringBootApplication
public class ShiroSpringbootDemoApplication {

    public static void main(String[] args) {
        SpringApplication.run(ShiroSpringbootDemoApplication.class, args);
    }

}

