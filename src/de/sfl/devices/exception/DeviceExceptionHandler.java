package de.sfl.devices.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * Exception handler for device management operations.
 */
@RestControllerAdvice
public class DeviceExceptionHandler {
    
    /**
     * Handles DeviceNotFoundException.
     */
    @ExceptionHandler(DeviceNotFoundException.class)
    public ResponseEntity<String> handleDeviceNotFoundException(DeviceNotFoundException ex) {
        return new ResponseEntity<>(ex.getMessage(), HttpStatus.NOT_FOUND);
    }

    /**
     * Handles DeviceAlreadyExistsException.
     */
    @ExceptionHandler(DeviceAlreadyExistsException.class)
    public ResponseEntity<String> handleDeviceAlreadyExistsException(DeviceAlreadyExistsException ex) {
        return new ResponseEntity<>(ex.getMessage(), HttpStatus.CONFLICT);
    }

    /**
     * Handles DeviceInvalidException.
     */
    @ExceptionHandler(DeviceInvalidException.class)
    public ResponseEntity<String> handleDeviceInvalidException(DeviceInvalidException ex) {
        return new ResponseEntity<>(ex.getMessage(), HttpStatus.BAD_REQUEST);
    }
}