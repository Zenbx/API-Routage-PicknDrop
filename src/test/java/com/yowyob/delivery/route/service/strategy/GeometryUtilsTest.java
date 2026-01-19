package com.yowyob.delivery.route.service.strategy;

import com.yowyob.delivery.route.controller.dto.IncidentDTO;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class GeometryUtilsTest {

    @Test
    void testDoesRouteIntersectIncident_QuarterCrossing() {
        // Route: Horizontal line in Douala
        // Start: 4.0500, 9.7000
        // End: 4.0500, 9.7200 (Length ~2.2km)
        double routeStartLat = 4.0500;
        double routeStartLng = 9.7000;
        double routeEndLat = 4.0500;
        double routeEndLng = 9.7200;

        // Incident: Vertical line crossing at 9.7050 (1/4 of the way)
        // Start: 4.0400, 9.7050
        // End: 4.0600, 9.7050
        // Buffer: 50 meters
        IncidentDTO incident = new IncidentDTO();
        incident.setType("ACCIDENT");
        incident.setBufferDistance(50.0);

        IncidentDTO.GeoLocation start = new IncidentDTO.GeoLocation();
        start.setLatitude(4.0400);
        start.setLongitude(9.7050);
        incident.setLineStart(start);

        IncidentDTO.GeoLocation end = new IncidentDTO.GeoLocation();
        end.setLatitude(4.0600);
        end.setLongitude(9.7050);
        incident.setLineEnd(end);

        // They definitely cross physically.
        boolean intersects = GeometryUtils.doesRouteIntersectIncident(
                routeStartLat, routeStartLng,
                routeEndLat, routeEndLng,
                incident);

        assertTrue(intersects, "Route should intersect incident line at the quarter point");
    }

    @Test
    void testDoesRouteIntersectIncident_MidpointCrossing() {
        // Route: Horizontal
        // Start: 4.0500, 9.7000
        // End: 4.0500, 9.7200
        // Midpoint: 4.0500, 9.7100
        double routeStartLat = 4.0500;
        double routeStartLng = 9.7000;
        double routeEndLat = 4.0500;
        double routeEndLng = 9.7200;

        // Incident: Vertical crossing exactly at 9.7100
        IncidentDTO incident = new IncidentDTO();
        incident.setBufferDistance(50.0);

        IncidentDTO.GeoLocation start = new IncidentDTO.GeoLocation();
        start.setLatitude(4.0400);
        start.setLongitude(9.7100);
        incident.setLineStart(start);

        IncidentDTO.GeoLocation end = new IncidentDTO.GeoLocation();
        end.setLatitude(4.0600);
        end.setLongitude(9.7100);
        incident.setLineEnd(end);

        boolean intersects = GeometryUtils.doesRouteIntersectIncident(
                routeStartLat, routeStartLng,
                routeEndLat, routeEndLng,
                incident);

        assertTrue(intersects, "Route should intersect incident line at the midpoint");
    }
}
