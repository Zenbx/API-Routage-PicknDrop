package com.yowyob.delivery.route.config;

import com.yowyob.delivery.route.domain.enums.ParcelPriority;
import com.yowyob.delivery.route.domain.enums.ParcelState;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.convert.ReadingConverter;
import org.springframework.data.convert.WritingConverter;
import org.springframework.lang.NonNull;

/**
 * Converters pour les types ENUM PostgreSQL
 */
public class EnumConverters {

    // ========== ParcelState Converters ==========
    
    @WritingConverter
    public static class ParcelStateToStringConverter implements Converter<ParcelState, String> {
        @Override
        public String convert(@NonNull ParcelState source) {
            return source.name();
        }
    }

    @ReadingConverter
    public static class StringToParcelStateConverter implements Converter<String, ParcelState> {
        @Override
        public ParcelState convert(@NonNull String source) {
            return ParcelState.valueOf(source.toUpperCase());
        }
    }

    // ========== ParcelPriority Converters ==========
    
    @WritingConverter
    public static class ParcelPriorityToStringConverter implements Converter<ParcelPriority, String> {
        @Override
        public String convert(@NonNull ParcelPriority source) {
            return source.name();
        }
    }

    @ReadingConverter
    public static class StringToParcelPriorityConverter implements Converter<String, ParcelPriority> {
        @Override
        public ParcelPriority convert(@NonNull String source) {
            return ParcelPriority.valueOf(source.toUpperCase());
        }
    }

    // ========== DriverState Converters ==========

    @WritingConverter
    public static class DriverStateToStringConverter implements Converter<com.yowyob.delivery.route.domain.enums.DriverState, String> {
        @Override
        public String convert(@NonNull com.yowyob.delivery.route.domain.enums.DriverState source) {
            return source.name();
        }
    }

    @ReadingConverter
    public static class StringToDriverStateConverter implements Converter<String, com.yowyob.delivery.route.domain.enums.DriverState> {
        @Override
        public com.yowyob.delivery.route.domain.enums.DriverState convert(@NonNull String source) {
            return com.yowyob.delivery.route.domain.enums.DriverState.valueOf(source.toUpperCase());
        }
    }
}