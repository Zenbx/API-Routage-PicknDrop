package com.yowyob.delivery.route.controller.exception;

/**
 * Exception thrown when routing algorithm cannot find a path between hubs.
 */
public class NoPathFoundException extends RuntimeException {

    public NoPathFoundException(String message) {
        super(message);
    }
}
