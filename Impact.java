package com.cts.ecotrack.entity;
import com.cts.ecotrack.enums.Status;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDate;
import java.util.Map;

@Entity
@Table(name = "impact")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Impact {

    @Id
    @Column(name = "ImpactID")
    private Integer impactId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ProjectID", referencedColumnName = "ProjectID")
    private Project project;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ProjectManagerID", referencedColumnName = "UserID")
    private User projectManager;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "MetricsJSON", columnDefinition = "JSON")
    private Map<String, Object> metricsJson;

    @Column(name = "Date")
    private LocalDate date;

    @Column(name = "Status", length = 50)
    private String status;


}
