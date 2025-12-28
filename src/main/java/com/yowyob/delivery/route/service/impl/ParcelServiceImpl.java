package com.yowyob.delivery.route.service.impl;

import com.yowyob.delivery.route.controller.dto.ParcelRequestDTO;
import com.yowyob.delivery.route.controller.dto.ParcelResponseDTO;
import com.yowyob.delivery.route.domain.entity.Parcel;
import com.yowyob.delivery.route.domain.enums.ParcelState;
import com.yowyob.delivery.route.mapper.ParcelMapper;
import com.yowyob.delivery.route.repository.ParcelRepository;
import com.yowyob.delivery.route.service.ParcelService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

/**
 * Implementation of {@link ParcelService} using R2DBC for reactive persistence.
 * Handles the logic for generating tracking codes and managing parcel states.
 */
@Service
@RequiredArgsConstructor
public class ParcelServiceImpl implements ParcelService {

    private final ParcelRepository parcelRepository;
    private final ParcelMapper parcelMapper;

    /**
     * {@inheritDoc}
     * Converts the DTO to an entity, generates a random tracking code (TRK-XXXX),
     * sets the initial state to PLANIFIE, and saves to the repository.
     */
    @Override
    public Mono<ParcelResponseDTO> createParcel(ParcelRequestDTO request) {
        Parcel parcel = parcelMapper.toEntity(request);
        parcel.setTrackingCode("TRK-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        parcel.setCurrentState(ParcelState.PLANNED);
        parcel.setPriority(com.yowyob.delivery.route.domain.enums.ParcelPriority.NORMAL);

        // Set default values for NOT NULL fields
        if (parcel.getPickupAddress() == null || parcel.getPickupAddress().isEmpty()) {
            parcel.setPickupAddress("Address not specified");
        }
        if (parcel.getDeliveryAddress() == null || parcel.getDeliveryAddress().isEmpty()) {
            parcel.setDeliveryAddress("Address not specified");
        }
        if (parcel.getDeliveryFeeXaf() == null) {
            parcel.setDeliveryFeeXaf(0.0); // Will be calculated later
        }

        return parcelRepository.save(parcel)
                .map(parcelMapper::toResponseDTO);
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
