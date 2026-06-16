package com.example.leavemanagement.service;

import com.example.leavemanagement.entity.LeaveRequest;
import com.example.leavemanagement.entity.User;
import com.example.leavemanagement.enums.LeaveStatus;
import com.example.leavemanagement.repository.LeaveRequestRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class LeaveRequestService {

    private final LeaveRequestRepository leaveRequestRepository;
    private final NotificationService notificationService;

    // Student applies for leave
    public LeaveRequest applyLeave(LeaveRequest leaveRequest) {

        // Check for overlapping leaves
        List<LeaveRequest> overlapping = leaveRequestRepository.findOverlappingLeaves(
                leaveRequest.getStudent(),
                leaveRequest.getFromDate(),
                leaveRequest.getToDate()
        );

        if (!overlapping.isEmpty()) {
            throw new RuntimeException("You already have a leave applied for these dates!");
        }

        leaveRequest.setStatus(LeaveStatus.PENDING);
        leaveRequest.setAppliedOn(LocalDateTime.now());

        LeaveRequest saved = leaveRequestRepository.save(leaveRequest);

        // Notify student that leave was submitted
        notificationService.sendNotification(
                leaveRequest.getStudent(),
                "Your leave request has been submitted successfully and is pending approval."
        );

        return saved;
    }

    // Teacher/Admin approves leave
    public LeaveRequest approveLeave(Long leaveId, User reviewedBy, String remarks) {
        LeaveRequest leave = getLeaveById(leaveId);

        leave.setStatus(LeaveStatus.APPROVED);
        leave.setReviewedBy(reviewedBy);
        leave.setRemarks(remarks);
        leave.setReviewedOn(LocalDateTime.now());

        LeaveRequest updated = leaveRequestRepository.save(leave);

        // Notify student
        notificationService.sendNotification(
                leave.getStudent(),
                "Your leave request from " + leave.getFromDate() +
                        " to " + leave.getToDate() + " has been APPROVED."
        );

        return updated;
    }

    // Teacher/Admin rejects leave
    public LeaveRequest rejectLeave(Long leaveId, User reviewedBy, String remarks) {
        LeaveRequest leave = getLeaveById(leaveId);

        leave.setStatus(LeaveStatus.REJECTED);
        leave.setReviewedBy(reviewedBy);
        leave.setRemarks(remarks);
        leave.setReviewedOn(LocalDateTime.now());

        LeaveRequest updated = leaveRequestRepository.save(leave);

        // Notify student
        notificationService.sendNotification(
                leave.getStudent(),
                "Your leave request from " + leave.getFromDate() +
                        " to " + leave.getToDate() + " has been REJECTED. Reason: " + remarks
        );

        return updated;
    }

    // Get single leave by ID
    public LeaveRequest getLeaveById(Long id) {
        return leaveRequestRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Leave request not found!"));
    }

    // Get all leaves of a student
    public List<LeaveRequest> getLeavesByStudent(User student) {
        return leaveRequestRepository.findByStudentOrderByAppliedOnDesc(student);
    }

    // Get all pending leaves (for teacher/admin)
    public List<LeaveRequest> getAllPendingLeaves() {
        return leaveRequestRepository.findByStatus(LeaveStatus.PENDING);
    }

    // Get all leaves (for admin)
    public List<LeaveRequest> getAllLeaves() {
        return leaveRequestRepository.findAllByOrderByAppliedOnDesc();
    }

    // Dashboard counts for admin
    public long countPending() {
        return leaveRequestRepository.countByStatus(LeaveStatus.PENDING);
    }

    public long countApproved() {
        return leaveRequestRepository.countByStatus(LeaveStatus.APPROVED);
    }

    public long countRejected() {
        return leaveRequestRepository.countByStatus(LeaveStatus.REJECTED);
    }

    // Student leave counts
    public long countStudentLeavesByStatus(User student, LeaveStatus status) {
        return leaveRequestRepository.countByStudentAndStatus(student, status);
    }
    // Get pending leaves only for students under this counsellor
    public List<LeaveRequest> getPendingLeavesForCounsellor(User counsellor) {
        return leaveRequestRepository.findAll().stream()
                .filter(l -> l.getStudent().getCounsellor() != null
                        && l.getStudent().getCounsellor().getId()
                        .equals(counsellor.getId())
                        && l.getStatus() == LeaveStatus.PENDING)
                .toList();
    }

    // Get all leaves for students under this counsellor
    public List<LeaveRequest> getAllLeavesForCounsellor(User counsellor) {
        return leaveRequestRepository.findAll().stream()
                .filter(l -> l.getStudent().getCounsellor() != null
                        && l.getStudent().getCounsellor().getId()
                        .equals(counsellor.getId()))
                .sorted((a, b) -> b.getAppliedOn().compareTo(a.getAppliedOn()))
                .toList();
    }
}