package com.cts.ecotrack.entity;

import com.cts.ecotrack.enums.EnvironmentalDataLogType;
import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonIdentityReference;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "environmental_data_log")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EnvironmentalDataLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "EntryID")
    private Integer entryId;

    @ManyToOne(fetch = FetchType.EAGER) // Changed to EAGER for easier JSON testing
    @JoinColumn(name = "OfficerEnvironmentalID", referencedColumnName = "UserID")
    @JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "userId")
    @JsonIdentityReference(alwaysAsId = true)
    private User officerEnvironmental;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "CitizenID", referencedColumnName = "UserID")
    @JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "userId")
    @JsonIdentityReference(alwaysAsId = true)
    private User citizen;

    @Enumerated(EnumType.STRING)
    @Column(name = "Type")
    private EnvironmentalDataLogType type;

    @Column(name = "Location")
    private String location;

    @Column(name = "Value", precision = 10, scale = 2)
    private BigDecimal value;

    @Column(name = "Date")
    private LocalDateTime date;

    @Column(name = "Status")
    private String status;
}