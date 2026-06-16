package com.example.leavemanagement.service;

import com.example.leavemanagement.entity.User;
import com.example.leavemanagement.enums.Role;
import com.example.leavemanagement.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder;

    // ── Register Student ───────────────────────────────────────────
    public User registerStudent(User user) {
        if (userRepository.existsByUsn(user.getUsn())) {
            throw new RuntimeException("USN already registered: " + user.getUsn());
        }
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setRole(Role.STUDENT);
        user.setActive(true);
        return userRepository.save(user);
    }

    // ── Register Counsellor (Admin only) ───────────────────────────
    public User registerCounsellor(User user) {
        if (userRepository.existsByEmployeeId(user.getEmployeeId())) {
            throw new RuntimeException("Employee ID already exists!");
        }
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setRole(Role.COUNSELLOR);
        user.setActive(true);
        return userRepository.save(user);
    }

    // ── Get logged in user by USN or Employee ID ───────────────────
    public User getUserByIdentifier(String identifier) {
        return userRepository.findByUsn(identifier)
                .or(() -> userRepository.findByEmployeeId(identifier))
                .orElseThrow(() ->
                        new RuntimeException("User not found: " + identifier));
    }

    // ── Get by ID ──────────────────────────────────────────────────
    public User getUserById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found!"));
    }

    // ── Get all students ───────────────────────────────────────────
    public List<User> getAllStudents() {
        return userRepository.findByRole(Role.STUDENT);
    }

    // ── Get all counsellors ────────────────────────────────────────
    public List<User> getAllCounsellors() {
        return userRepository.findByRole(Role.COUNSELLOR);
    }

    // ── Get students under a counsellor (ordered by USN) ──────────
    public List<User> getStudentsUnderCounsellor(User counsellor) {
        return userRepository.findByCounsellorOrderByUsnAsc(counsellor);
    }

    // ── Assign counsellor to student ───────────────────────────────
    public void assignCounsellor(Long studentId, Long counsellorId) {
        User student = getUserById(studentId);
        User counsellor = getUserById(counsellorId);
        student.setCounsellor(counsellor);
        userRepository.save(student);
    }

    // ── Auto assign counsellor based on USN order ──────────────────
    public void autoAssignCounsellors() {
        List<User> students = userRepository
                .findByRoleAndIsActive(Role.STUDENT, true);
        List<User> counsellors = userRepository
                .findByRoleAndIsActive(Role.COUNSELLOR, true);

        if (counsellors.isEmpty()) return;

        // Sort students by USN
        students.sort((a, b) -> a.getUsn().compareTo(b.getUsn()));

        // Divide students equally among counsellors
        int groupSize = (int) Math.ceil(
                (double) students.size() / counsellors.size());

        for (int i = 0; i < students.size(); i++) {
            int counsellorIndex = i / groupSize;
            if (counsellorIndex >= counsellors.size()) {
                counsellorIndex = counsellors.size() - 1;
            }
            students.get(i).setCounsellor(counsellors.get(counsellorIndex));
            userRepository.save(students.get(i));
        }
    }

    // ── Get students by academic year ──────────────────────────────
    public List<User> getStudentsByAcademicYear(String academicYear) {
        return userRepository.findByRoleAndAcademicYear(
                Role.STUDENT, academicYear);
    }

    // ── Deactivate user ────────────────────────────────────────────
    public void deactivateUser(Long id) {
        User user = getUserById(id);
        user.setActive(false);
        userRepository.save(user);
    }

    // ── Activate user ──────────────────────────────────────────────
    public void activateUser(Long id) {
        User user = getUserById(id);
        user.setActive(true);
        userRepository.save(user);
    }

    // ── Update user ────────────────────────────────────────────────
    public User updateUser(User user) {
        return userRepository.save(user);
    }

    // ── Get all users ──────────────────────────────────────────────
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }
}