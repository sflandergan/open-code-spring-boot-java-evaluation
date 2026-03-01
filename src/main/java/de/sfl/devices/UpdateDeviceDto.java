package de.sfl.devices;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateDeviceDto {

    @NotBlank
    private String name;

    private String description;
}