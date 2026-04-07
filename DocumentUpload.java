package com.cts.ecotrack.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "documentupload")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DocumentUpload {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "documentid")
    private Integer documentId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "officer_environmentalid", referencedColumnName = "userid")
    private User officerEnvironmental;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "entryid", referencedColumnName = "entryid")
    private EnvironmentalDataLog environmentalDataLog;

    @Column(name = "fileuri", length = 512)
    private String fileUri;

    @Column(name = "uploaded_date")
    private LocalDateTime uploadedDate;

    @Column(name = "verification_status")
    private Boolean verificationStatus;
}