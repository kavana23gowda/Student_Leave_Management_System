package com.example.leavemanagement.controller;

import com.example.leavemanagement.entity.LeaveRequest;
import com.example.leavemanagement.entity.User;
import com.example.leavemanagement.enums.LeaveStatus;
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

@Controller
@RequiredArgsConstructor
@RequestMapping("/student")
public class StudentController {

    private final LeaveRequestService leaveRequestService;
    private final LeaveTypeService leaveTypeService;
    private final NotificationService notificationService;
    private final UserService userService;

    // ── Helper to get logged-in user ──────────────────────────────
    private User getLoggedInUser(Principal principal) {
        return userService.getUserByIdentifier(principal.getName());
    }

    // ── Dashboard ─────────────────────────────────────────────────
    @GetMapping("/dashboard")
    public String dashboard(Model model, Principal principal) {
        User student = getLoggedInUser(principal);

        model.addAttribute("student", student);
        model.addAttribute("leaves",
                leaveRequestService.getLeavesByStudent(student));
        model.addAttribute("pendingCount",
                leaveRequestService.countStudentLeavesByStatus(student, LeaveStatus.PENDING));
        model.addAttribute("approvedCount",
                leaveRequestService.countStudentLeavesByStatus(student, LeaveStatus.APPROVED));
        model.addAttribute("rejectedCount",
                leaveRequestService.countStudentLeavesByStatus(student, LeaveStatus.REJECTED));
        model.addAttribute("unreadCount",
                notificationService.countUnread(student));

        return "student/dashboard";
    }

    // ── Show Apply Leave Form ─────────────────────────────────────
    @GetMapping("/apply")
    public String applyLeaveForm(Model model, Principal principal) {
        User student = getLoggedInUser(principal);

        model.addAttribute("leaveRequest", new LeaveRequest());
        model.addAttribute("leaveTypes", leaveTypeService.getAllLeaveTypes());
        model.addAttribute("unreadCount", notificationService.countUnread(student));

        return "student/apply";
    }

    // ── Submit Leave Form ─────────────────────────────────────────
    @PostMapping("/apply")
    public String submitLeave(@ModelAttribute LeaveRequest leaveRequest,
                              Principal principal,
                              RedirectAttributes redirectAttributes) {
        try {
            User student = getLoggedInUser(principal);
            leaveRequest.setStudent(student);
            leaveRequestService.applyLeave(leaveRequest);
            redirectAttributes.addFlashAttribute("success",
                    "Leave applied successfully!");
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/student/dashboard";
    }

    // ── View Single Leave Detail ──────────────────────────────────
    @GetMapping("/leave/{id}")
    public String viewLeave(@PathVariable Long id,
                            Model model,
                            Principal principal) {
        User student = getLoggedInUser(principal);
        LeaveRequest leave = leaveRequestService.getLeaveById(id);

        // Security check — student can only see their own leaves
        if (!leave.getStudent().getId().equals(student.getId())) {
            return "redirect:/student/dashboard";
        }

        model.addAttribute("leave", leave);
        model.addAttribute("unreadCount", notificationService.countUnread(student));
        return "student/leave-detail";
    }

    // ── Notifications ─────────────────────────────────────────────
    @GetMapping("/notifications")
    public String notifications(Model model, Principal principal) {
        User student = getLoggedInUser(principal);

        model.addAttribute("notifications",
                notificationService.getUserNotifications(student));
        notificationService.markAllAsRead(student);
        model.addAttribute("unreadCount", 0);

        return "student/notifications";
    }

    // ── Profile ───────────────────────────────────────────────────
    @GetMapping("/profile")
    public String profile(Model model, Principal principal) {
        User student = getLoggedInUser(principal);
        model.addAttribute("student", student);
        model.addAttribute("unreadCount", notificationService.countUnread(student));
        return "student/profile";
    }
}