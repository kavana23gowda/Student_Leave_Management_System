package com.example.leavemanagement.repository;

import com.example.leavemanagement.entity.LeaveType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface LeaveTypeRepository extends JpaRepository<LeaveType, Long> {

    // Find leave type by name
    Optional<LeaveType> findByName(String name);

    // Check if leave type already exists
    boolean existsByName(String name);
}