package de.sfl.devices;

import java.util.UUID;

public class DeviceNotFoundException extends RuntimeException {
    
    public DeviceNotFoundException(UUID deviceId) {
        super("Device with id " + deviceId + " not found");
    }
}