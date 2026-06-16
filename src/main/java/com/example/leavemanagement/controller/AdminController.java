package com.example.leavemanagement.controller;

import com.example.leavemanagement.entity.LeaveRequest;
import com.example.leavemanagement.entity.LeaveType;
import com.example.leavemanagement.entity.User;
import com.example.leavemanagement.enums.Role;
import com.example.leavemanagement.service.LeaveRequestService;
import com.example.leavemanagement.service.LeaveTypeService;
import com.example.leavemanagement.service.NotificationService;
import com.example.leavemanagement.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;
import java.util.List;

@Controller
@RequiredArgsConstructor
@RequestMapping("/admin")
public class AdminController {

    private final LeaveRequestService leaveRequestService;
    private final LeaveTypeService leaveTypeService;
    private final NotificationService notificationService;
    private final UserService userService;

    // ── Helper ────────────────────────────────────────────────────
    private User getLoggedInUser(Principal principal) {
        return userService.getUserByIdentifier(principal.getName());
    }

    // ── Dashboard ─────────────────────────────────────────────────
    @GetMapping("/dashboard")
    public String dashboard(Model model, Principal principal) {
        User admin = getLoggedInUser(principal);

        model.addAttribute("admin", admin);
        model.addAttribute("totalStudents",
                userService.getAllStudents().size());
        model.addAttribute("totalCounsellors",
                userService.getAllCounsellors().size());
        model.addAttribute("pendingCount",
                leaveRequestService.countPending());
        model.addAttribute("approvedCount",
                leaveRequestService.countApproved());
        model.addAttribute("rejectedCount",
                leaveRequestService.countRejected());
        model.addAttribute("recentLeaves",
                leaveRequestService.getAllLeaves());
        model.addAttribute("unreadCount",
                notificationService.countUnread(admin));

        return "admin/dashboard";
    }

    // ── All Leaves ────────────────────────────────────────────────
    @GetMapping("/leaves")
    public String allLeaves(Model model, Principal principal) {
        User admin = getLoggedInUser(principal);

        model.addAttribute("leaves", leaveRequestService.getAllLeaves());
        model.addAttribute("unreadCount",
                notificationService.countUnread(admin));

        return "admin/leaves";
    }

    // ── View Single Leave ─────────────────────────────────────────
    @GetMapping("/leave/{id}")
    public String viewLeave(@PathVariable Long id,
                            Model model,
                            Principal principal) {
        User admin = getLoggedInUser(principal);

        model.addAttribute("leave", leaveRequestService.getLeaveById(id));
        model.addAttribute("unreadCount",
                notificationService.countUnread(admin));

        return "admin/leave-detail";
    }

    // ── Approve Leave ─────────────────────────────────────────────
    @PostMapping("/leave/{id}/approve")
    public String approveLeave(@PathVariable Long id,
                               @RequestParam String remarks,
                               Principal principal,
                               RedirectAttributes redirectAttributes) {
        User admin = getLoggedInUser(principal);
        leaveRequestService.approveLeave(id, admin, remarks);
        redirectAttributes.addFlashAttribute("success",
                "Leave approved successfully!");
        return "redirect:/admin/leaves";
    }

    // ── Reject Leave ──────────────────────────────────────────────
    @PostMapping("/leave/{id}/reject")
    public String rejectLeave(@PathVariable Long id,
                              @RequestParam String remarks,
                              Principal principal,
                              RedirectAttributes redirectAttributes) {
        User admin = getLoggedInUser(principal);
        leaveRequestService.rejectLeave(id, admin, remarks);
        redirectAttributes.addFlashAttribute("success",
                "Leave rejected.");
        return "redirect:/admin/leaves";
    }

    // ── Manage Users ──────────────────────────────────────────────
    @GetMapping("/users")
    public String manageUsers(Model model, Principal principal) {
        User admin = getLoggedInUser(principal);

        model.addAttribute("students", userService.getAllStudents());
        model.addAttribute("teachers", userService.getAllCounsellors());
        model.addAttribute("unreadCount",
                notificationService.countUnread(admin));

        return "admin/users";
    }

    // ── Deactivate User ───────────────────────────────────────────
    @PostMapping("/users/{id}/deactivate")
    public String deactivateUser(@PathVariable Long id,
                                 RedirectAttributes redirectAttributes) {
        userService.deactivateUser(id);
        redirectAttributes.addFlashAttribute("success",
                "User deactivated successfully!");
        return "redirect:/admin/users";
    }

    // ── Manage Leave Types ────────────────────────────────────────
    @GetMapping("/leave-types")
    public String leaveTypes(Model model, Principal principal) {
        User admin = getLoggedInUser(principal);

        model.addAttribute("leaveTypes",
                leaveTypeService.getAllLeaveTypes());
        model.addAttribute("newLeaveType", new LeaveType());
        model.addAttribute("unreadCount",
                notificationService.countUnread(admin));

        return "admin/leave-types";
    }

    // ── Add Leave Type ────────────────────────────────────────────
    @PostMapping("/leave-types/add")
    public String addLeaveType(@ModelAttribute LeaveType leaveType,
                               RedirectAttributes redirectAttributes) {
        try {
            leaveTypeService.addLeaveType(leaveType);
            redirectAttributes.addFlashAttribute("success",
                    "Leave type added successfully!");
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/leave-types";
    }

    // ── Delete Leave Type ─────────────────────────────────────────
    @PostMapping("/leave-types/{id}/delete")
    public String deleteLeaveType(@PathVariable Long id,
                                  RedirectAttributes redirectAttributes) {
        leaveTypeService.deleteLeaveType(id);
        redirectAttributes.addFlashAttribute("success",
                "Leave type deleted!");
        return "redirect:/admin/leave-types";
    }

    // ── Notifications ─────────────────────────────────────────────
    @GetMapping("/notifications")
    public String notifications(Model model, Principal principal) {
        User admin = getLoggedInUser(principal);

        model.addAttribute("notifications",
                notificationService.getUserNotifications(admin));
        notificationService.markAllAsRead(admin);
        model.addAttribute("unreadCount", 0);

        return "admin/notifications";
    }
    // ── Add Student ───────────────────────────────────────────────
    @GetMapping("/students/add")
    public String addStudentForm(Model model, Principal principal) {
        User admin = getLoggedInUser(principal);
        model.addAttribute("student", new User());
        model.addAttribute("counsellors", userService.getAllCounsellors());
        model.addAttribute("unreadCount",
                notificationService.countUnread(admin));
        return "admin/add-student";
    }

    @PostMapping("/students/add")
    public String addStudent(@ModelAttribute User student,
                             @RequestParam Long counsellorId,
                             @RequestParam String academicYear,
                             RedirectAttributes redirectAttributes) {
        try {
            student.setAcademicYear(academicYear);
            User saved = userService.registerStudent(student);
            userService.assignCounsellor(saved.getId(), counsellorId);
            redirectAttributes.addFlashAttribute("success",
                    "Student added successfully!");
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/users";
    }

    // ── Add Counsellor ─────────────────────────────────────────────
    @GetMapping("/counsellors/add")
    public String addCounsellorForm(Model model, Principal principal) {
        User admin = getLoggedInUser(principal);
        model.addAttribute("counsellor", new User());
        model.addAttribute("unreadCount",
                notificationService.countUnread(admin));
        return "admin/add-counsellor";
    }

    @PostMapping("/counsellors/add")
    public String addCounsellor(@ModelAttribute User counsellor,
                                RedirectAttributes redirectAttributes) {
        try {
            userService.registerCounsellor(counsellor);
            redirectAttributes.addFlashAttribute("success",
                    "Counsellor added successfully!");
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/users";
    }

    // ── Auto Assign Counsellors ────────────────────────────────────
    @PostMapping("/students/auto-assign")
    public String autoAssign(RedirectAttributes redirectAttributes) {
        userService.autoAssignCounsellors();
        redirectAttributes.addFlashAttribute("success",
                "Students auto-assigned to counsellors by USN order!");
        return "redirect:/admin/users";
    }

    // ── Activate User ──────────────────────────────────────────────
    @PostMapping("/users/{id}/activate")
    public String activateUser(@PathVariable Long id,
                               RedirectAttributes redirectAttributes) {
        userService.activateUser(id);
        redirectAttributes.addFlashAttribute("success",
                "User activated successfully!");
        return "redirect:/admin/users";
    }
}