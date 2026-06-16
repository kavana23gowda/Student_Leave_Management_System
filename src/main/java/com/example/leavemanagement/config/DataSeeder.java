package com.example.leavemanagement.config;

import com.example.leavemanagement.entity.LeaveType;
import com.example.leavemanagement.entity.User;
import com.example.leavemanagement.enums.Role;
import com.example.leavemanagement.repository.LeaveTypeRepository;
import com.example.leavemanagement.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DataSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final LeaveTypeRepository leaveTypeRepository;
    private final BCryptPasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {

        // ── Seed Admin ─────────────────────────────────────────────
        if (!userRepository.existsByEmployeeId("ADMIN001")) {
            userRepository.save(User.builder()
                    .name("Admin")
                    .employeeId("ADMIN001")
                    .email("[email protected]")
                    .password(passwordEncoder.encode("admin123"))
                    .role(Role.ADMIN)
                    .isActive(true)
                    .build());
            System.out.println("✅ Admin created → ID: ADMIN001 / Pass: admin123");
        }

        // ── Seed Counsellor ────────────────────────────────────────
        if (!userRepository.existsByEmployeeId("EMP001")) {
            userRepository.save(User.builder()
                    .name("Mr. Kumar")
                    .employeeId("EMP001")
                    .email("[email protected]")
                    .password(passwordEncoder.encode("counsellor123"))
                    .role(Role.COUNSELLOR)
                    .department("Computer Science")
                    .isActive(true)
                    .build());
            System.out.println("✅ Counsellor created → ID: EMP001 / Pass: counsellor123");
        }

        // ── Seed Leave Types ───────────────────────────────────────
        if (leaveTypeRepository.count() == 0) {
            leaveTypeRepository.save(LeaveType.builder()
                    .name("Medical Leave")
                    .description("Health-related absence")
                    .maxDays(10).build());
            leaveTypeRepository.save(LeaveType.builder()
                    .name("Casual Leave")
                    .description("Personal work")
                    .maxDays(5).build());
            leaveTypeRepository.save(LeaveType.builder()
                    .name("Emergency Leave")
                    .description("Urgent situations")
                    .maxDays(3).build());
            leaveTypeRepository.save(LeaveType.builder()
                    .name("Duty Leave")
                    .description("College events")
                    .maxDays(7).build());
            System.out.println("✅ Leave types seeded!");
        }
    }
}