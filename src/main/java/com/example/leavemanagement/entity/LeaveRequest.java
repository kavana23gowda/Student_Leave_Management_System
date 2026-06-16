package com.example.leavemanagement.entity;

import com.example.leavemanagement.enums.LeaveStatus;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

@Entity
@Table(name = "leave_requests")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LeaveRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Many students can have many leave requests
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", nullable = false)
    private User student;

    // Many leave requests can be of one leave type
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "leave_type_id", nullable = false)
    private LeaveType leaveType;

    // Who approved/rejected (Teacher or Admin)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reviewed_by")
    private User reviewedBy;

    @Column(name = "from_date", nullable = false)
    private LocalDate fromDate;

    @Column(name = "to_date", nullable = false)
    private LocalDate toDate;

    @Column(nullable = false, length = 500)
    private String reason;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private LeaveStatus status = LeaveStatus.PENDING;
    @Column(name = "applied_on")
    @Builder.Default
    private LocalDateTime appliedOn = LocalDateTime.now();

    @Column(name = "reviewed_on")
    private LocalDateTime reviewedOn;

    @Column(name = "remarks")               // Teacher's comment when approving/rejecting
    private String remarks;

    // Helper method — auto calculates number of days
    @Transient
    public long getNumberOfDays() {
        if (fromDate != null && toDate != null) {
            return ChronoUnit.DAYS.between(fromDate, toDate) + 1;
        }
        return 0;
    }
}