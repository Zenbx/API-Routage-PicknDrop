package com.yowyob.delivery.route.service;

import com.yowyob.delivery.route.controller.dto.DriverResponseDTO;
import com.yowyob.delivery.route.repository.DriverRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

/**
 * Service for managing delivery drivers.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DriverService {

    private final DriverRepository driverRepository;

    /**
     * Retrieve all drivers in the system.
     *
     * @return a stream of all drivers
     */
    public Flux<DriverResponseDTO> getAllDrivers() {
        return driverRepository.findAll()
                .map(driver -> DriverResponseDTO.builder()
                        .id(driver.getId())
                        .name(driver.getFirstName() + " " + driver.getLastName())
                        .status(driver.getCurrentState() != null ? driver.getCurrentState().toString() : "UNKNOWN")
                        .build());
    }
}
