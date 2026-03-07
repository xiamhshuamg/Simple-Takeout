package com.ynu.edu.elm_intermediate;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.ynu.edu.elm_intermediate.mapper")
public class ElmIntermediateApplication {

    public static void main(String[] args) {

        SpringApplication.run(ElmIntermediateApplication.class, args);
    }

}
