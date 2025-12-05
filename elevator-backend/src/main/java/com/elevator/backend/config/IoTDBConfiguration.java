package com.elevator.backend.config;

import org.apache.iotdb.session.pool.SessionPool;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class IoTDBConfiguration {

    @Value("${iotdb.host}")
    private String host;

    @Value("${iotdb.port}")
    private int port;

    @Value("${iotdb.user}")
    private String user;

    @Value("${iotdb.password}")
    private String password;

    @Bean
    public SessionPool sessionPool() {
        // 创建一个最大支持 10 个并发连接的池
        return new SessionPool(host, port, user, password, 10);
    }
}