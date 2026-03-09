package de.sfl.devices;

public record UpdateDeviceDto(
    String name,
    String description
) {
    public void applyToEntity(Device device) {
        if (name != null && !name.isBlank()) {
            device.setName(name);
        }
        if (description != null && !description.isBlank()) {
            device.setDescription(description);
        }
    }
}