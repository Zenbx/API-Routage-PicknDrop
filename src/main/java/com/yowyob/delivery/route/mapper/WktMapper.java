package com.yowyob.delivery.route.mapper;

import org.locationtech.jts.geom.Point;
import org.locationtech.jts.io.WKTReader;
import org.mapstruct.Named;
import org.springframework.stereotype.Component;

@Component
public class WktMapper {

    @Named("wktToPoint")
    public Point wktToPoint(String wkt) {
        if (wkt == null || wkt.isEmpty()) {
            return null;
        }
        try {
            return (Point) new WKTReader().read(wkt);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
