package com.yowyob.delivery.route.config;

import org.locationtech.jts.geom.LineString;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.convert.ReadingConverter;
import org.springframework.data.r2dbc.convert.R2dbcCustomConversions;
import org.springframework.data.r2dbc.dialect.PostgresDialect;

import java.util.Arrays;

@Configuration
public class R2dbcConfiguration {

    @Bean
    public R2dbcCustomConversions r2dbcCustomConversions() {
        return R2dbcCustomConversions.of(
                PostgresDialect.INSTANCE,
                Arrays.asList(
                    // Converters pour les enums
                    new EnumConverters.ParcelStateToStringConverter(),
                    new EnumConverters.StringToParcelStateConverter(),
                    new EnumConverters.ParcelPriorityToStringConverter(),
                    new EnumConverters.StringToParcelPriorityConverter(),
                    new EnumConverters.DriverStateToStringConverter(),
                    new EnumConverters.StringToDriverStateConverter(),
                    // Converters pour les géométries
                    new GeometryConverters.JtsPointToStringConverter(),
                    new GeometryConverters.StringToJtsPointConverter(),
                    new LineStringToStringConverter()
                )
        );
    }

    @ReadingConverter
    static class LineStringToStringConverter implements Converter<LineString, String> {
        @Override
        public String convert(LineString source) {
            return source.toText();
        }
    }
}
