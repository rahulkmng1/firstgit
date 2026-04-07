package com.cts.ecotrack.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.*;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LogResponseDTO {
    private Integer entryId;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime date;
    private String type;
    private String location;
    private String value;       // Includes unit (e.g., "150 AQI")
    private String status;      // "Verified" or "Pending"
    private Long documentCount; // Shows how many files are uploaded
    private Integer officerId;
    private String officerName;
}