package de.sfl.devices;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class UpdateDeviceDto {
    @NotBlank(message = "Device name cannot be blank, may be omitted to keep current value")
    private String name;
    
    @Size(max = 1000, message = "Description must be less than 1000 characters")
    private String description;
    
    // Getters, setters
    public UpdateDeviceDto(String name, String description) {
        this.name = name;
        this.description = description;
    }
    
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
}