package com.example.leavemanagement.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "leave_types")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LeaveType {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String name;  // e.g. Medical, Casual, Emergency, Duty Leave

    @Column
    private String description;

    @Column(name = "max_days")
    private int maxDays;  // max allowed days for this leave type
}