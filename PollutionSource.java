package com.cts.ecotrack.entity;
import com.cts.ecotrack.enums.PollutionSourceType;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "pollutionsource")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PollutionSource {

    @Id
    @Column(name = "SourceID")
    private Integer sourceId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "OfficerPollutionControlID", referencedColumnName = "UserID")
    private User officerPollutionControl;

    @Enumerated(EnumType.STRING)
    @Column(name = "Type")
    private PollutionSourceType type;

    @Column(name = "Location", length = 255)
    private String location;

    @Column(name = "Status", length = 50)
    private String status;


}
