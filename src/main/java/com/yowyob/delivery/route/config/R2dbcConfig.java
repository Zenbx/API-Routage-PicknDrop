package com.yowyob.delivery.route.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.r2dbc.convert.R2dbcCustomConversions;
import org.springframework.data.r2dbc.dialect.PostgresDialect;
import org.springframework.data.r2dbc.repository.config.EnableR2dbcRepositories;

import io.r2dbc.spi.ConnectionFactory;

import java.util.Arrays;
import java.util.List;

/**
 * Configuration R2DBC avec support des types géométriques PostGIS (JTS)
 */
@Configuration
@EnableR2dbcRepositories(basePackages = "com.yowyob.delivery.route.repository")
public class R2dbcConfig {

     private final ConnectionFactory connectionFactory;

    public R2dbcConfig(ConnectionFactory connectionFactory) {
        this.connectionFactory = connectionFactory;
    }

    public ConnectionFactory connectionFactory() {
        return connectionFactory;
    }

    protected List<Object> getCustomConverters() {
        return Arrays.asList(
            // Enum converters
            new EnumConverters.ParcelStateToStringConverter(),
            new EnumConverters.StringToParcelStateConverter(),
            new EnumConverters.ParcelPriorityToStringConverter(),
            new EnumConverters.StringToParcelPriorityConverter()
            
            // Note: On N'enregistre PAS les GeometryConverters
            // car nos champs sont des String dans l'entité
        );
    }

    /**
     * Enregistre les convertisseurs personnalisés pour les types géométriques JTS
     */
    // @Bean
    // public R2dbcCustomConversions r2dbcCustomConversions() {
    //     return R2dbcCustomConversions.of(
    //             PostgresDialect.INSTANCE,
    //             Arrays.asList(
    //                     new GeometryConverters.JtsPointToStringConverter(),
             //           new GeometryConverters.StringToJtsPointConverter()));
 //   }
}