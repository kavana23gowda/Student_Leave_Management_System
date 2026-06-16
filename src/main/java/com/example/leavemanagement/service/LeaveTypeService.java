package com.example.leavemanagement.service;

import com.example.leavemanagement.entity.LeaveType;
import com.example.leavemanagement.repository.LeaveTypeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class LeaveTypeService {

    private final LeaveTypeRepository leaveTypeRepository;

    // Add new leave type (Admin only)
    public LeaveType addLeaveType(LeaveType leaveType) {
        if (leaveTypeRepository.existsByName(leaveType.getName())) {
            throw new RuntimeException("Leave type already exists!");
        }
        return leaveTypeRepository.save(leaveType);
    }

    // Get all leave types
    public List<LeaveType> getAllLeaveTypes() {
        return leaveTypeRepository.findAll();
    }

    // Get leave type by ID
    public LeaveType getLeaveTypeById(Long id) {
        return leaveTypeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Leave type not found!"));
    }

    // Update leave type
    public LeaveType updateLeaveType(LeaveType leaveType) {
        return leaveTypeRepository.save(leaveType);
    }

    // Delete leave type
    public void deleteLeaveType(Long id) {
        leaveTypeRepository.deleteById(id);
    }
}