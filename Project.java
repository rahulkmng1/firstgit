package com.cts.ecotrack.entity;

import com.cts.ecotrack.enums.ProjectStatus;
import com.cts.ecotrack.enums.Status;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Entity
@Table(name = "project")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Project {

    @Id
    @Column(name = "ProjectID")
    private Integer projectId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ProjectManagerID", referencedColumnName = "UserID",
                foreignKey = @ForeignKey(name = "fk_project_manager"))
    private User projectManager;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ResourceID", referencedColumnName = "ResourceID",
                foreignKey = @ForeignKey(name = "fk_project_resource"))
    private Resource resource;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "EntryID", referencedColumnName = "EntryID",
                foreignKey = @ForeignKey(name = "fk_project_entry"))
    private EnvironmentalDataLog environmentalDataLog;


    @Column(name = "Title", nullable = false, length = 255)
    private String title;

    @Column(name = "Description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "StartDate")
    private LocalDate startDate;

    @Column(name = "EndDate")
    private LocalDate endDate;

    @Column(name = "Budget", precision = 15, scale = 2)
    private BigDecimal budget;

    @Column(name = "Status", length = 50)
    private String status;


    @OneToMany(mappedBy = "project", cascade = CascadeType.ALL, orphanRemoval = true)
    @ToString.Exclude
    private List<Milestone> milestones;

    @OneToMany(mappedBy = "project", cascade = CascadeType.ALL, orphanRemoval = true)
    @ToString.Exclude
    private List<Impact> impacts;
}
