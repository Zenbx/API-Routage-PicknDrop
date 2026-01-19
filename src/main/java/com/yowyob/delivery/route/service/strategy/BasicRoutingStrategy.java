package com.yowyob.delivery.route.service.strategy;

import com.yowyob.delivery.route.controller.dto.IncidentDTO;
import com.yowyob.delivery.route.controller.dto.RoutingConstraintsDTO;
import com.yowyob.delivery.route.domain.entity.Hub;
import com.yowyob.delivery.route.domain.entity.Route;
import com.yowyob.delivery.route.mapper.HubMapper;
import com.yowyob.delivery.route.repository.HubRepository;
import lombok.RequiredArgsConstructor;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.Point;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Basic routing strategy implementing a direct point-to-point calculation.
 * Uses Euclidean distance (as the crow flies) and a simple duration multiplier.
 */
@Component
@RequiredArgsConstructor
public class BasicRoutingStrategy implements RoutingStrategy {

    private final GeometryFactory geometryFactory = new GeometryFactory();
    private final HubMapper hubMapper;

    /**
     * {@inheritDoc}
     * Computes a direct LineString between start and end points and calculates
     * distance.
     */
    @Override
    public Mono<Route> calculateOptimalRoute(Hub start, Hub end, RoutingConstraintsDTO constraints) {
        Point startPt = hubMapper.wktToPoint(start.getLocation());
        Point endPt = hubMapper.wktToPoint(end.getLocation());

        Coordinate[] coordinates = new Coordinate[] {
                startPt.getCoordinate(),
                endPt.getCoordinate()
        };
        LineString path = geometryFactory.createLineString(coordinates);
        double distance = startPt.distance(endPt);

        return Mono.just(Route.builder()
                .routeGeometry(path.toText())
                .totalDistanceKm(distance)
                .estimatedDurationMinutes((int) (distance * 10))
                .routingService("BASIC")
                .isActive(true)
                .build());
    }

    /**
     * {@inheritDoc}
     * Recalculates route by creating a detour around the incident location.
     * Supports both linear (lineStart/lineEnd) and legacy circular incidents.
     */
    @Override
    public Mono<Route> recalculateRoute(Route currentRoute, IncidentDTO incident) {
        System.out.println("=== BASIC STRATEGY: recalculateRoute called ===");
        System.out.println("Route ID: " + currentRoute.getId());
        System.out.println("startHubId: " + currentRoute.getStartHubId());
        System.out.println("endHubId: " + currentRoute.getEndHubId());
        System.out.println("Current geometry: " + currentRoute.getRouteGeometry());
        System.out.println("Incident: " + incident);

        if (currentRoute.getStartHubId() == null || currentRoute.getEndHubId() == null) {
            System.out.println("EARLY EXIT: No hub IDs");
            return Mono.just(currentRoute);
        }

        // Validate incident data for linear segment
        if (incident.getLineStart() == null || incident.getLineEnd() == null) {
            System.out.println("EARLY EXIT: Incomplete incident line data");
            return Mono.just(currentRoute);
        }

        System.out.println("Incident lineStart: " + incident.getLineStart());
        System.out.println("Incident lineEnd: " + incident.getLineEnd());
        System.out.println("Incident bufferDistance: " + incident.getBufferDistance());

        // Calculate midpoint of incident line for detour calculation
        double incidentMidLat = (incident.getLineStart().getLatitude() + incident.getLineEnd().getLatitude()) / 2;
        double incidentMidLng = (incident.getLineStart().getLongitude() + incident.getLineEnd().getLongitude()) / 2;

        System.out.println("Incident midpoint: (" + incidentMidLng + ", " + incidentMidLat + ")");

        // Parse current route geometry to get start and end points
        String wkt = currentRoute.getRouteGeometry();
        if (wkt == null || !wkt.startsWith("LINESTRING")) {
            return Mono.just(currentRoute);
        }

        // Extract coordinates from WKT (handle both "LINESTRING(" and "LINESTRING (")
        String coords = wkt.replaceAll("LINESTRING\\s*\\(", "").replace(")", "").trim();
        String[] points = coords.split(",");

        if (points.length < 2) {
            return Mono.just(currentRoute);
        }

        // Get start and end coordinates
        String[] startCoords = points[0].trim().split("\\s+");
        String[] endCoords = points[points.length - 1].trim().split("\\s+");

        double startLng = Double.parseDouble(startCoords[0]);
        double startLat = Double.parseDouble(startCoords[1]);
        double endLng = Double.parseDouble(endCoords[0]);
        double endLat = Double.parseDouble(endCoords[1]);

        System.out.println("Route: Start(" + startLng + ", " + startLat + ") -> End(" + endLng + ", " + endLat + ")");

        // Check if route intersects with linear incident buffer using GeometryUtils
        boolean intersects = GeometryUtils.doesRouteIntersectIncident(
                startLat, startLng, endLat, endLng, incident);

        if (!intersects) {
            System.out.println("Route does NOT intersect incident buffer - no recalculation needed");
            return Mono.just(currentRoute);
        }

        System.out.println("Route INTERSECTS incident buffer - calculating detour");

        // Get incident buffer distance from the incident data (convert meters to
        // degrees, roughly 1° ≈ 111km)
        double incidentRadiusMeters = incident.getBufferDistance();
        double incidentRadiusDeg = incidentRadiusMeters / 111000.0; // Convert to degrees
        double safetyBuffer = 0.001; // Additional ~111m safety margin

        System.out.println("Incident buffer distance: " + incidentRadiusMeters + "m (" + incidentRadiusDeg + "°)");

        // Calculate direction vector of original route
        double routeDx = endLng - startLng;
        double routeDy = endLat - startLat;
        double routeLength = Math.sqrt(routeDx * routeDx + routeDy * routeDy);

        // Normalize direction vector
        double routeUnitX = routeDx / routeLength;
        double routeUnitY = routeDy / routeLength;

        System.out.println("Route direction: (" + routeUnitX + ", " + routeUnitY + ")");

        // Calculate perpendicular vector (rotate 90° counterclockwise)
        double perpX = -routeUnitY;
        double perpY = routeUnitX;

        System.out.println("Perpendicular direction: (" + perpX + ", " + perpY + ")");

        // Project incident midpoint onto the route line to find closest point
        double startToIncidentX = incidentMidLng - startLng;
        double startToIncidentY = incidentMidLat - startLat;
        double projection = startToIncidentX * routeUnitX + startToIncidentY * routeUnitY;

        // Contact point on the route (where vehicle hits incident zone)
        double contactLng = startLng + projection * routeUnitX;
        double contactLat = startLat + projection * routeUnitY;

        System.out.println("Contact point: (" + contactLng + ", " + contactLat + ")");

        // Calculate distance from incident midpoint to route line
        double distToLine = Math.sqrt(
                Math.pow(incidentMidLng - contactLng, 2) + Math.pow(incidentMidLat - contactLat, 2));

        System.out.println(
                "Distance from incident midpoint to route: " + distToLine + "° (~" + (distToLine * 111) + "km)");

        // Calculate detour point: from incident midpoint, perpendicular direction,
        // outside safe zone
        // We go perpendicular to the route, starting from incident midpoint
        double detourDistanceFromCenter = incidentRadiusDeg + safetyBuffer;
        double detourLng = incidentMidLng + perpX * detourDistanceFromCenter;
        double detourLat = incidentMidLat + perpY * detourDistanceFromCenter;

        System.out.println("Detour point: (" + detourLng + ", " + detourLat + ")");

        // Verify detour is outside incident zone
        double detourToIncidentDist = Math.sqrt(
                Math.pow(detourLng - incidentMidLng, 2) + Math.pow(detourLat - incidentMidLat, 2));
        System.out.println("Detour to incident midpoint distance: " + detourToIncidentDist + "° (should be > "
                + incidentRadiusDeg + "°)");

        // Calculate if we should create a 4th point to rejoin the original route
        double endToIncidentDist = Math.sqrt(
                Math.pow(endLng - incidentMidLng, 2) + Math.pow(endLat - incidentMidLat, 2));

        List<Coordinate> coordinates = new ArrayList<>();

        // Start from CURRENT position (first point of route geometry)
        // This is the actual driver position, NOT the original start hub
        coordinates.add(new Coordinate(startLng, startLat)); // Current driver position

        // Add detour waypoint to go around incident
        coordinates.add(new Coordinate(detourLng, detourLat)); // Detour point

        if (endToIncidentDist > (incidentRadiusDeg + safetyBuffer) && projection < routeLength * 0.7) {
            // Create 4th point: rejoin original route after incident
            double rejoinDistance = incidentRadiusDeg + safetyBuffer;
            double rejoinOffset = Math.sqrt(rejoinDistance * rejoinDistance -
                    Math.pow(Math.abs(startToIncidentX * perpX + startToIncidentY * perpY), 2));

            double rejoinLng = contactLng + routeUnitX * rejoinOffset;
            double rejoinLat = contactLat + routeUnitY * rejoinOffset;

            // Verify rejoin point is outside incident zone
            double rejoinToIncidentDist = Math.sqrt(
                    Math.pow(rejoinLng - incidentMidLng, 2) + Math.pow(rejoinLat - incidentMidLat, 2));

            if (rejoinToIncidentDist > incidentRadiusDeg) {
                coordinates.add(new Coordinate(rejoinLng, rejoinLat)); // Rejoin point
                System.out.println("Rejoin point: (" + rejoinLng + ", " + rejoinLat + ")");
            }
        }

        coordinates.add(new Coordinate(endLng, endLat)); // End point

        LineString newPath = geometryFactory.createLineString(coordinates.toArray(new Coordinate[0]));

        System.out.println("New path created with " + coordinates.size() + " points");
        System.out.println("New geometry WKT: " + newPath.toText());

        // Calculate total distance
        double totalDistance = 0;
        for (int i = 0; i < coordinates.size() - 1; i++) {
            Coordinate c1 = coordinates.get(i);
            Coordinate c2 = coordinates.get(i + 1);
            totalDistance += Math.sqrt(Math.pow(c2.x - c1.x, 2) + Math.pow(c2.y - c1.y, 2));
        }

        // Update route
        currentRoute.setRouteGeometry(newPath.toText());
        currentRoute.setTotalDistanceKm(totalDistance);
        currentRoute.setEstimatedDurationMinutes((int) (totalDistance * 10));
        currentRoute.setRoutingService("BASIC_DETOUR");

        System.out.println("=== BASIC STRATEGY: Route updated ===");
        System.out.println("New routing service: " + currentRoute.getRoutingService());
        System.out.println("New geometry: " + currentRoute.getRouteGeometry());

        return Mono.just(currentRoute);
    }
}
