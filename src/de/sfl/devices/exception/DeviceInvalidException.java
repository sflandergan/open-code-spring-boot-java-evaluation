package de.sfl.devices.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Exception thrown for invalid device data.
 */
@ResponseStatus(HttpStatus.BAD_REQUEST)
public class DeviceInvalidException extends RuntimeException {
    public DeviceInvalidException(String message) {
        super(message);
    }
}