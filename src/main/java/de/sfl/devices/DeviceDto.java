package de.sfl.devices;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

import de.sfl.sensors.SensorDto;

/**
 * DTO representing a Device in API responses.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DeviceDto {

    private java.util.UUID id;
    private String name;
    private String description;

    @NotBlank
    private LocalDateTime createdAt;

    @NotBlank
    private LocalDateTime updatedAt;

    private List<SensorDto> sensors;
}