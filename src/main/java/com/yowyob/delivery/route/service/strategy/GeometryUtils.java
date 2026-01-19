package com.yowyob.delivery.route.service.strategy;

import com.yowyob.delivery.route.controller.dto.IncidentDTO;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.LineString;

/**
 * Utility class for geometric calculations related to linear incidents
 */
public class GeometryUtils {

    private static final GeometryFactory geometryFactory = new GeometryFactory();

    /**
     * Check if a point is within the buffer zone of a line segment
     *
     * @param pointLat     Latitude of the point to check
     * @param pointLng     Longitude of the point to check
     * @param lineStart    Start point of the line segment
     * @param lineEnd      End point of the line segment
     * @param bufferMeters Buffer distance in meters
     * @return true if the point is within the buffer zone
     */
    public static boolean isPointInLineBuffer(
            double pointLat, double pointLng,
            IncidentDTO.GeoLocation lineStart,
            IncidentDTO.GeoLocation lineEnd,
            double bufferMeters) {

        if (lineStart == null || lineEnd == null) {
            return false;
        }

        double distance = pointToSegmentDistance(
                pointLat, pointLng,
                lineStart.getLatitude(), lineStart.getLongitude(),
                lineEnd.getLatitude(), lineEnd.getLongitude());

        // Convert buffer from meters to kilometers for comparison
        double bufferKm = bufferMeters / 1000.0;
        return distance <= bufferKm;
    }

    /**
     * Calculate the minimum distance from a point to a line segment using Haversine
     *
     * @return Distance in kilometers
     */
    private static double pointToSegmentDistance(
            double pointLat, double pointLng,
            double segStartLat, double segStartLng,
            double segEndLat, double segEndLng) {

        // Vector from segment start to end
        double segmentLat = segEndLat - segStartLat;
        double segmentLng = segEndLng - segStartLng;

        // Vector from segment start to point
        double pointLatDiff = pointLat - segStartLat;
        double pointLngDiff = pointLng - segStartLng;

        // Calculate segment length squared
        double segmentLengthSq = segmentLat * segmentLat + segmentLng * segmentLng;

        // If segment is actually a point, return distance to that point
        if (segmentLengthSq == 0) {
            return haversineDistance(pointLat, pointLng, segStartLat, segStartLng);
        }

        // Calculate projection parameter t (clamped to [0, 1])
        // t = 0: closest point is segment start
        // t = 1: closest point is segment end
        // 0 < t < 1: closest point is between start and end
        double t = Math.max(0, Math.min(1,
                (pointLatDiff * segmentLat + pointLngDiff * segmentLng) / segmentLengthSq));

        // Calculate the closest point on the segment
        double closestLat = segStartLat + t * segmentLat;
        double closestLng = segStartLng + t * segmentLng;

        // Return distance from point to closest point on segment
        return haversineDistance(pointLat, pointLng, closestLat, closestLng);
    }

    /**
     * Calculate distance between two points using Haversine formula
     *
     * @return Distance in kilometers
     */
    public static double haversineDistance(
            double lat1, double lng1,
            double lat2, double lng2) {

        final double R = 6371.0; // Earth's radius in kilometers

        double dLat = Math.toRadians(lat2 - lat1);
        double dLng = Math.toRadians(lng2 - lng1);

        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                        Math.sin(dLng / 2) * Math.sin(dLng / 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        return R * c;
    }

    /**
     * Check if a route (line segment) intersects with an incident buffer zone
     *
     * @param routeStartLat Route start latitude
     * @param routeStartLng Route start longitude
     * @param routeEndLat   Route end latitude
     * @param routeEndLng   Route end longitude
     * @param incident      Incident with line segment and buffer
     * @return true if the route intersects the incident buffer
     */
    public static boolean doesRouteIntersectIncident(
            double routeStartLat, double routeStartLng,
            double routeEndLat, double routeEndLng,
            IncidentDTO incident) {

        if (incident.getLineStart() == null || incident.getLineEnd() == null) {
            return false;
        }

        // Robust check for crossing/intersection using JTS
        try {
            Coordinate[] routeCoords = new Coordinate[] {
                    new Coordinate(routeStartLng, routeStartLat),
                    new Coordinate(routeEndLng, routeEndLat)
            };
            LineString routeLine = geometryFactory.createLineString(routeCoords);

            Coordinate[] incidentCoords = new Coordinate[] {
                    new Coordinate(incident.getLineStart().getLongitude(), incident.getLineStart().getLatitude()),
                    new Coordinate(incident.getLineEnd().getLongitude(), incident.getLineEnd().getLatitude())
            };
            LineString incidentLine = geometryFactory.createLineString(incidentCoords);

            if (routeLine.intersects(incidentLine)) {
                return true;
            }
        } catch (Exception e) {
            // Log or ignore, fall back to buffer checks
        }

        // Check if route start or end point is within incident buffer
        if (isPointInLineBuffer(routeStartLat, routeStartLng,
                incident.getLineStart(), incident.getLineEnd(), incident.getBufferDistance())) {
            return true;
        }

        if (isPointInLineBuffer(routeEndLat, routeEndLng,
                incident.getLineStart(), incident.getLineEnd(), incident.getBufferDistance())) {
            return true;
        }

        // Check middle point of route
        double midLat = (routeStartLat + routeEndLat) / 2;
        double midLng = (routeStartLng + routeEndLng) / 2;
        if (isPointInLineBuffer(midLat, midLng,
                incident.getLineStart(), incident.getLineEnd(), incident.getBufferDistance())) {
            return true;
        }

        // Check if incident line endpoints are within route buffer
        // (This handles cases where incident line crosses the route)
        double routeBufferKm = incident.getBufferDistance() / 1000.0;
        double distToIncidentStart = pointToSegmentDistance(
                incident.getLineStart().getLatitude(), incident.getLineStart().getLongitude(),
                routeStartLat, routeStartLng, routeEndLat, routeEndLng);

        if (distToIncidentStart <= routeBufferKm) {
            return true;
        }

        double distToIncidentEnd = pointToSegmentDistance(
                incident.getLineEnd().getLatitude(), incident.getLineEnd().getLongitude(),
                routeStartLat, routeStartLng, routeEndLat, routeEndLng);
        return distToIncidentEnd <= routeBufferKm;
    }

    public static boolean isHubInIncidentBuffer(com.yowyob.delivery.route.domain.entity.Hub hub, IncidentDTO incident,
            org.locationtech.jts.io.WKTReader wktReader) {
        try {
            org.locationtech.jts.geom.Point pt = (org.locationtech.jts.geom.Point) wktReader.read(hub.getLocation());
            return isPointInLineBuffer(pt.getY(), pt.getX(), incident.getLineStart(), incident.getLineEnd(),
                    incident.getBufferDistance());
        } catch (Exception e) {
            return false;
        }
    }
}
