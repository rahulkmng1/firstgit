package com.cts.ecotrack.entity;
import com.cts.ecotrack.enums.ResourceType;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "resources")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Resource{

    @Id
    @Column(name = "ResourceID")
    private Integer resourceId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "OfficerPollutionControlID", referencedColumnName = "UserID")
    private User officerPollutionControl;

    @Enumerated(EnumType.STRING)
    @Column(name = "Type")
    private ResourceType type;

    @Column(name = "Capacity", precision = 15, scale = 2)
    private BigDecimal capacity;

    @Column(name = "Usages", precision = 15, scale = 2)
    private BigDecimal usages;

    @Column(name = "Status", length = 50)
    private String status;


}
