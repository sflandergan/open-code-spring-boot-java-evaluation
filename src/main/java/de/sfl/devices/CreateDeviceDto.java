package de.sfl.devices;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateDeviceDto {

    @NotBlank
    private String name;

    @NotBlank
    private String description;
}