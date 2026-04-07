package com.cts.ecotrack.entity;
import com.cts.ecotrack.enums.Status;
import com.cts.ecotrack.enums.UserRole;
import com.cts.ecotrack.enums.UserStatus;
import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Entity
@Table(name = "user")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {
    @Id
    @Column(name = "UserID")
    private Integer userId;

    @Column(name = "Name", nullable = false, length = 100)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(name = "Role", nullable = false)
    private UserRole role;

    @Column(name = "Email", unique = true, nullable = false, length = 100)
    private String email;

    @Column(name = "passwordHash", nullable = false, length = 100)
    private String passwordHash;

    @Column(name = "Phone", length = 20)
    private String phone;

    @Enumerated(EnumType.STRING)
    @Column(name = "Status", nullable = false)
    private UserStatus status;

//    @OneToMany(mappedBy = "officerEnvironmental", cascade = CascadeType.ALL, orphanRemoval = true)
//    private List<EnvironmentalDataLog> environmentalLogs;
//
//    @OneToMany(mappedBy = "citizen", cascade = CascadeType.ALL, orphanRemoval = true)
//    private List<EnvironmentalDataLog> citizenLogs;
//
//    @OneToMany(mappedBy = "citizen", cascade = CascadeType.ALL, orphanRemoval = true)
//    private List<Issue> issues;
//
//    @OneToMany(mappedBy = "citizen", cascade = CascadeType.ALL, orphanRemoval = true)
//    private List<Feedback> feedbacks;

}
