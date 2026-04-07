package com.cts.ecotrack.dto;

import jakarta.validation.constraints.*;
import lombok.*;
import com.fasterxml.jackson.annotation.JsonFormat;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LogRequestDTO {

    @NotNull(message = "Citizen ID is required")
    private Integer citizenId;

    private Integer officerId; // Optional during creation

    @NotBlank(message = "Type is required (e.g., AIR_QUALITY, WATER_QUALITY, SOIL_QUALITY)")
    private String type;

    @NotBlank(message = "Location is required")
    private String location;

    @NotNull(message = "Value cannot be null")
    @PositiveOrZero(message = "Value must be 0 or greater")
    private Double value;

    @NotBlank(message = "Date is required")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private String date; // Expected format: yyyy-MM-dd HH:mm:ss
}