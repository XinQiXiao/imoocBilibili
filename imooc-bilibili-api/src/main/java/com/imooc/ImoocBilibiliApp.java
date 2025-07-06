package com.imooc;

import org.springframework.boot.SpringApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class ImoocBilibiliApp {

    public static void main(String[] args){
        ApplicationContext app = SpringApplication.run(ImoocBilibiliApp.class, args);
    }
}
