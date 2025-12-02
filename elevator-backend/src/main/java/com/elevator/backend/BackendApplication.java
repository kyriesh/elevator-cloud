package com.elevator.backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Spring Boot 启动类
 * * @SpringBootApplication 注解等同于 @Configuration + @EnableAutoConfiguration + @ComponentScan
 * 关键点：它会自动扫描当前包（com.elevator.backend）及其所有子包下的组件
 */
@SpringBootApplication
public class BackendApplication {

    public static void main(String[] args) {
        // 启动 Spring Boot 应用
        SpringApplication.run(BackendApplication.class, args);
        System.out.println("(♥◠‿◠)ﾉﾞ  电梯运维云平台后端启动成功   ლ(´ڡ`ლ)ﾞ");
    }

}