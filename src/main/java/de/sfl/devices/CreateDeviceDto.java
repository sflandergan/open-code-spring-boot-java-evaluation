package de.sfl.devices;

import jakarta.validation.constraints.NotBlank;

public record CreateDeviceDto(
    @NotBlank(message = "Device name cannot be blank")
    String name,
    
    String description) {
}