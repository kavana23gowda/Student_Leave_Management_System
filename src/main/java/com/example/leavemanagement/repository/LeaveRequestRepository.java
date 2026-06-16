package com.example.leavemanagement.repository;

import com.example.leavemanagement.entity.LeaveRequest;
import com.example.leavemanagement.entity.User;
import com.example.leavemanagement.enums.LeaveStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface LeaveRequestRepository extends JpaRepository<LeaveRequest, Long> {

    // Get all leaves of a specific student
    List<LeaveRequest> findByStudent(User student);

    // Get all leaves by status (e.g. all PENDING leaves)
    List<LeaveRequest> findByStatus(LeaveStatus status);

    // Get leaves of a student filtered by status
    List<LeaveRequest> findByStudentAndStatus(User student, LeaveStatus status);

    // Get leaves ordered by latest first
    List<LeaveRequest> findAllByOrderByAppliedOnDesc();

    // Get leaves of a student ordered by latest first
    List<LeaveRequest> findByStudentOrderByAppliedOnDesc(User student);

    // Count how many leaves a student has by status
    long countByStudentAndStatus(User student, LeaveStatus status);

    // Custom query — check for overlapping leave dates for same student
    @Query("SELECT l FROM LeaveRequest l WHERE l.student = :student " +
            "AND l.status != 'REJECTED' " +
            "AND (l.fromDate <= :toDate AND l.toDate >= :fromDate)")
    List<LeaveRequest> findOverlappingLeaves(
            @Param("student") User student,
            @Param("fromDate") LocalDate fromDate,
            @Param("toDate") LocalDate toDate
    );

    // Count total leaves for admin dashboard
    long countByStatus(LeaveStatus status);
}