package com.yowyob.delivery.route.repository;

import com.yowyob.delivery.route.domain.entity.Route;
import reactor.core.publisher.Mono;

public interface CustomRouteRepository {
    Mono<Route> saveWithGeometry(Route route);
}
