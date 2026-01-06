package com.yowyob.delivery.route.controller;

import com.yowyob.delivery.route.controller.dto.DriverResponseDTO;
import com.yowyob.delivery.route.service.DriverService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

/**
 * Controller for managing delivery drivers.
 * Provides endpoints for listing and retrieving driver information.
 */
@RestController
@RequestMapping("/api/v1/drivers")
@RequiredArgsConstructor
@Tag(name = "Drivers", description = "Endpoints for managing delivery drivers")
public class DriverController {

    private final DriverService driverService;

    /**
     * List all available drivers in the system.
     *
     * @return a stream of all drivers
     */
    @GetMapping
    @Operation(summary = "List all drivers", description = "Returns a list of all available drivers for assignment.")
    public Flux<DriverResponseDTO> getAllDrivers() {
        return driverService.getAllDrivers();
    }
}
