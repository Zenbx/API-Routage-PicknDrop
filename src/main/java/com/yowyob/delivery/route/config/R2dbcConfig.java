package com.yowyob.delivery.route.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.r2dbc.convert.R2dbcCustomConversions;
import org.springframework.data.r2dbc.dialect.PostgresDialect;
import org.springframework.data.r2dbc.repository.config.EnableR2dbcRepositories;
import io.r2dbc.spi.ConnectionFactory;
import java.util.Arrays;

@Configuration
@EnableR2dbcRepositories(basePackages = "com.yowyob.delivery.route.repository")
public class R2dbcConfig {

    private final ConnectionFactory connectionFactory;

    public R2dbcConfig(ConnectionFactory connectionFactory) {
        this.connectionFactory = connectionFactory;
    }

    @Bean
    public R2dbcCustomConversions r2dbcCustomConversions() {
        return R2dbcCustomConversions.of(
                PostgresDialect.INSTANCE,
                Arrays.asList(
                    new EnumConverters.ParcelStateToStringConverter(),
                    new EnumConverters.StringToParcelStateConverter(),
                    new EnumConverters.ParcelPriorityToStringConverter(),
                    new EnumConverters.StringToParcelPriorityConverter(),
                    new GeometryConverters.JtsPointToStringConverter(),
                    new GeometryConverters.StringToJtsPointConverter()
                )
        );
    }
}