package com.yowyob.delivery.route.service.impl;

import com.yowyob.delivery.route.controller.dto.ParcelRequestDTO;
import com.yowyob.delivery.route.controller.dto.ParcelResponseDTO;
import com.yowyob.delivery.route.domain.entity.Parcel;
import com.yowyob.delivery.route.domain.enums.ParcelState;
import com.yowyob.delivery.route.mapper.ParcelMapper;
import com.yowyob.delivery.route.repository.ParcelRepository;
import com.yowyob.delivery.route.repository.HubRepository;
import com.yowyob.delivery.route.service.ParcelService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

/**
 * Implementation of {@link ParcelService} using R2DBC for reactive persistence.
 * Handles the logic for generating tracking codes and managing parcel states.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ParcelServiceImpl implements ParcelService {

    private final ParcelRepository parcelRepository;
    private final HubRepository hubRepository;
    private final ParcelMapper parcelMapper;
    private final com.yowyob.delivery.route.client.PetriNetClient petriNetClient;

    /**
     * {@inheritDoc}
     * Converts the DTO to an entity, generates a random tracking code (TRK-XXXX),
     * sets the initial state to PLANIFIE, and saves to the repository.
     */
    @Override
    public Mono<ParcelResponseDTO> createParcel(ParcelRequestDTO request) {
        return Mono.zip(
                resolveLocation(request.getPickupLocation()),
                resolveLocation(request.getDeliveryLocation())).flatMap(locations -> {
                    Parcel parcel = parcelMapper.toEntity(request);

                    // Set resolved coordinates and addresses
                    parcel.setPickupLocation(locations.getT1().location);
                    if (parcel.getPickupAddress() == null || parcel.getPickupAddress().isEmpty()
                            || parcel.getPickupAddress().equals("Address not specified")) {
                        parcel.setPickupAddress(locations.getT1().address);
                    }

                    parcel.setDeliveryLocation(locations.getT2().location);
                    if (parcel.getDeliveryAddress() == null || parcel.getDeliveryAddress().isEmpty()
                            || parcel.getDeliveryAddress().equals("Address not specified")) {
                        parcel.setDeliveryAddress(locations.getT2().address);
                    }

                    parcel.setTrackingCode("TRK-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
                    parcel.setCurrentState(ParcelState.PLANNED);
                    parcel.setPriority(com.yowyob.delivery.route.domain.enums.ParcelPriority.NORMAL);

                    if (parcel.getDeliveryFeeXaf() == null) {
                        parcel.setDeliveryFeeXaf(0.0);
                    }

                    return parcelRepository.saveWithGeometry(parcel)
                            .flatMap(savedParcel -> petriNetClient.initializeParcelNet(savedParcel.getId())
                                    .flatMap(netId -> {
                                        savedParcel.setPetriNetId(netId);
                                        return parcelRepository.save(savedParcel); // Update with netId
                                    })
                                    .thenReturn(savedParcel)
                                    .map(parcelMapper::toResponseDTO));
                });
    }

    private static class ResolvedLocation {
        final String location;
        final String address;

        ResolvedLocation(String location, String address) {
            this.location = location;
            this.address = address;
        }
    }

    private Mono<ResolvedLocation> resolveLocation(String locationStr) {
        if (locationStr == null || locationStr.isEmpty()) {
            return Mono.just(new ResolvedLocation("POINT(0 0)", "Unknown"));
        }

        // Check if it's a UUID (Hub ID)
        try {
            UUID hubId = UUID.fromString(locationStr);
            return hubRepository.findByIdWithLocation(hubId)
                    .map(hub -> new ResolvedLocation(hub.getLocation(), hub.getAddress()))
                    .switchIfEmpty(Mono.just(new ResolvedLocation("POINT(0 0)", "Unknown Hub")));
        } catch (IllegalArgumentException e) {
            // Not a UUID, assume it's already WKT or lat,lng
            if (locationStr.toUpperCase().startsWith("POINT")) {
                return Mono.just(new ResolvedLocation(locationStr, "Address not specified"));
            }
            // If it's "lat,lng" format, convert to WKT
            if (locationStr.contains(",")) {
                try {
                    String[] parts = locationStr.split(",");
                    double lat = Double.parseDouble(parts[0].trim());
                    double lng = Double.parseDouble(parts[1].trim());
                    return Mono.just(new ResolvedLocation(String.format("POINT(%f %f)", lng, lat), "Coordinates"));
                } catch (Exception ex) {
                    log.warn("Failed to parse coordinates: {}", locationStr);
                }
            }
            return Mono.just(new ResolvedLocation(locationStr, "Address not specified"));
        }
    }

    /**
     * {@inheritDoc}
     * Retrieves a parcel record by its unique identifier.
     */
    @Override
    public Mono<ParcelResponseDTO> getParcel(UUID id) {
        return parcelRepository.findById(id)
                .map(parcelMapper::toResponseDTO);
    }

    /**
     * {@inheritDoc}
     * Lists all parcels currently stored in the system.
     */
    @Override
    public Flux<ParcelResponseDTO> getAllParcels() {
        return parcelRepository.findAll()
                .map(parcelMapper::toResponseDTO);
    }
}
