package com.example.leavemanagement.entity;

import com.example.leavemanagement.enums.Role;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "users")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    // Used by STUDENT for login
    @Column(unique = true)
    private String usn;

    // Used by COUNSELLOR and ADMIN for login
    @Column(name = "employee_id", unique = true)
    private String employeeId;

    @Column(nullable = false)
    private String password;

    @Column(name = "email")
    private String email;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role; // STUDENT, COUNSELLOR, ADMIN

    @Column(name = "department")
    private String department;

    @Column(name = "academic_year")
    private String academicYear; // e.g. 2023-24, 2024-25

    // Which counsellor this student is assigned to
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "counsellor_id")
    private User counsellor;

    @Builder.Default
    @Column(name = "is_active")
    private boolean isActive = true;
}