package com.cts.ecotrack.dto;


import com.cts.ecotrack.enums.ResourceType;
import lombok.*;
import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ResourcesDTO {
    private Integer resourceId;
    private Integer officerId;
    private ResourceType type;
    private BigDecimal capacity;
    private BigDecimal usages;
    private String status;
}