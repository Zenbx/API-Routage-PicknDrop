package com.yowyob.delivery.route.service.impl;

import com.yowyob.delivery.route.controller.dto.GeoPointRequestDTO;
import com.yowyob.delivery.route.controller.dto.GeoPointResponseDTO;
import com.yowyob.delivery.route.domain.entity.Hub;
import com.yowyob.delivery.route.domain.enums.HubType;
import com.yowyob.delivery.route.repository.HubRepository;
import com.yowyob.delivery.route.service.HubService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Service implementation for managing logistics hubs and geographical points.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class HubServiceImpl implements HubService {

    private final HubRepository hubRepository;

    // Pattern pour extraire lat/lon depuis WKT: "POINT(lon lat)"
    private static final Pattern WKT_PATTERN = Pattern.compile("POINT\\s*\\(\\s*([\\d.-]+)\\s+([\\d.-]+)\\s*\\)");

    @Override
    @Transactional
    public Mono<GeoPointResponseDTO> createHub(GeoPointRequestDTO request) {
        log.info("Creating hub: {}", request.getAddress());

        // Convertir lat/lon en WKT format: POINT(longitude latitude)
        String wkt = String.format(java.util.Locale.US, "POINT(%f %f)", request.getLongitude(), request.getLatitude());

        Hub hub = Hub.builder()
                .address(request.getAddress())
                .type(HubType.valueOf(request.getType()))
                .location(wkt)
                .build();

        return hubRepository.saveWithGeometry(hub)
                .doOnSuccess(saved -> log.info("Hub created with ID: {}", saved.getId()))
                .map(this::toResponseDTO);
    }

    @Override
    public Mono<GeoPointResponseDTO> getHub(UUID id) {
        log.debug("Fetching hub with ID: {}", id);

        return hubRepository.findByIdWithLocation(id)
                .map(this::toResponseDTO)
                .switchIfEmpty(Mono.error(
                        new RuntimeException("Hub not found with ID: " + id)));
    }

    @Override
    public Flux<GeoPointResponseDTO> getAllHubs() {
        log.debug("Fetching all hubs");

        return hubRepository.findAllWithLocation()
                .map(this::toResponseDTO)
                .doOnComplete(() -> log.debug("Finished fetching all hubs"));
    }

    /**
     * Convertit une entité Hub en DTO de réponse.
     * Parse le WKT pour extraire latitude et longitude.
     */
    private GeoPointResponseDTO toResponseDTO(Hub hub) {
        double[] coordinates = parseWKT(hub.getLocation());

        return GeoPointResponseDTO.builder()
                .id(hub.getId())
                .address(hub.getAddress())
                .longitude(coordinates[0])
                .latitude(coordinates[1])
                .type(hub.getType().name())
                .build();
    }

    /**
     * Parse une chaîne WKT pour extraire longitude et latitude.
     * Format attendu: "POINT(longitude latitude)"
     */
    private double[] parseWKT(String wkt) {
        if (wkt == null || wkt.trim().isEmpty()) {
            log.warn("Empty WKT string, returning default coordinates");
            return new double[] { 0.0, 0.0 };
        }

        Matcher matcher = WKT_PATTERN.matcher(wkt);
        if (matcher.find()) {
            try {
                double longitude = Double.parseDouble(matcher.group(1));
                double latitude = Double.parseDouble(matcher.group(2));
                return new double[] { longitude, latitude };
            } catch (NumberFormatException e) {
                log.error("Failed to parse coordinates from WKT: {}", wkt, e);
                return new double[] { 0.0, 0.0 };
            }
        }

        log.warn("Invalid WKT format: {}", wkt);
        return new double[] { 0.0, 0.0 };
    }
}