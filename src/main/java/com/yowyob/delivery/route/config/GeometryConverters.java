package com.yowyob.delivery.route.config;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.PrecisionModel;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.convert.ReadingConverter;
import org.springframework.data.convert.WritingConverter;
import org.springframework.lang.NonNull;

/**
 * Convertisseurs pour les types géométriques PostGIS (JTS)
 * Nécessaires pour Spring Data R2DBC avec PostGIS
 */
public class GeometryConverters {

    private static final GeometryFactory geometryFactory = 
        new GeometryFactory(new PrecisionModel(), 4326);

    /**
     * Convertit un Point JTS (org.locationtech.jts.geom.Point) en String WKT
     * Utilisé quand Spring Data R2DBC lit les données depuis PostgreSQL/PostGIS
     */
    @ReadingConverter
    public static class JtsPointToStringConverter implements Converter<Point, String> {
        @Override
        public String convert(@NonNull Point source) {
            // Format WKT: POINT(longitude latitude)
            return String.format("POINT(%s %s)", source.getX(), source.getY());
        }
    }

    /**
     * Convertit un String WKT en Point JTS
     * Utilisé quand Spring Data R2DBC écrit les données vers PostgreSQL/PostGIS
     */
    @WritingConverter
    public static class StringToJtsPointConverter implements Converter<String, Point> {
        @Override
        public Point convert(@NonNull String source) {
            if (source == null || source.trim().isEmpty()) {
                return null;
            }
            
            try {
                // Extraire les coordonnées de "POINT(longitude latitude)"
                String coords = source.replace("POINT(", "")
                                     .replace(")", "")
                                     .trim();
                String[] parts = coords.split("\\s+");
                
                if (parts.length != 2) {
                    throw new IllegalArgumentException("Invalid WKT Point format: " + source);
                }
                
                double x = Double.parseDouble(parts[0]); // longitude
                double y = Double.parseDouble(parts[1]); // latitude
                
                // Créer un Point JTS avec SRID 4326 (WGS 84)
                Coordinate coordinate = new Coordinate(x, y);
                Point point = geometryFactory.createPoint(coordinate);
                point.setSRID(4326);
                
                return point;
            } catch (Exception e) {
                throw new IllegalArgumentException("Failed to parse WKT Point: " + source, e);
            }
        }
    }
}