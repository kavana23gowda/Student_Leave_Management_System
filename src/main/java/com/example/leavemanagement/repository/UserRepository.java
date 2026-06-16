package com.example.leavemanagement.repository;

import com.example.leavemanagement.entity.User;
import com.example.leavemanagement.enums.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByUsn(String usn);

    Optional<User> findByEmployeeId(String employeeId);

    boolean existsByUsn(String usn);

    boolean existsByEmployeeId(String employeeId);

    List<User> findByRole(Role role);

    List<User> findByRoleAndIsActive(Role role, boolean isActive);

    // Get all students under a specific counsellor
    List<User> findByCounsellor(User counsellor);

    // Get students by academic year
    List<User> findByRoleAndAcademicYear(Role role, String academicYear);

    // Get students under counsellor ordered by USN
    List<User> findByCounsellorOrderByUsnAsc(User counsellor);

    // Search student by USN pattern
    @Query("SELECT u FROM User u WHERE u.role = 'STUDENT' " +
            "AND u.usn LIKE %:usn%")
    List<User> searchByUsn(@Param("usn") String usn);
}