package com.cts.ecotrack.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(name = "milestone")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Milestone {

    @Id
    @Column(name = "MilestoneID")
    private Integer milestoneId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ProjectID", referencedColumnName = "ProjectID")
    private Project project;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ProjectManagerID", referencedColumnName = "UserID")
    private User projectManager;

    @Column(name = "Title", length = 255)
    private String title;

    @Column(name = "Date")
    private LocalDate date;

    @Column(name = "Status", length = 50)
    private String status;


}
