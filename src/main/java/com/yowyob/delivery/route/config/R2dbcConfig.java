package com.yowyob.delivery.route.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.r2dbc.repository.config.EnableR2dbcRepositories;
import io.r2dbc.spi.ConnectionFactory;

@Configuration
@EnableR2dbcRepositories(basePackages = "com.yowyob.delivery.route.repository")
public class R2dbcConfig {

    private final ConnectionFactory connectionFactory;

    public R2dbcConfig(ConnectionFactory connectionFactory) {
        this.connectionFactory = connectionFactory;
    }
}