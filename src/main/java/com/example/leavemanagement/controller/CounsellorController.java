package com.example.leavemanagement.controller;

import com.example.leavemanagement.entity.LeaveRequest;
import com.example.leavemanagement.entity.User;
import com.example.leavemanagement.service.LeaveRequestService;
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
@RequestMapping("/counsellor")
public class CounsellorController {

    private final LeaveRequestService leaveRequestService;
    private final NotificationService notificationService;
    private final UserService userService;

    private User getLoggedInUser(Principal principal) {
        return userService.getUserByIdentifier(principal.getName());
    }

    // ── Dashboard ─────────────────────────────────────────────────
    @GetMapping("/dashboard")
    public String dashboard(Model model, Principal principal) {
        User counsellor = getLoggedInUser(principal);

        // Only show leaves from students under this counsellor
        List<User> myStudents =
                userService.getStudentsUnderCounsellor(counsellor);
        List<LeaveRequest> pendingLeaves =
                leaveRequestService.getPendingLeavesForCounsellor(counsellor);

        model.addAttribute("counsellor", counsellor);
        model.addAttribute("myStudents", myStudents);
        model.addAttribute("pendingLeaves", pendingLeaves);
        model.addAttribute("pendingCount", pendingLeaves.size());
        model.addAttribute("allLeaves",
                leaveRequestService.getAllLeavesForCounsellor(counsellor));
        model.addAttribute("unreadCount",
                notificationService.countUnread(counsellor));

        return "counsellor/dashboard";
    }

    // ── View Leave Detail ─────────────────────────────────────────
    @GetMapping("/leave/{id}")
    public String viewLeave(@PathVariable Long id,
                            Model model, Principal principal) {
        User counsellor = getLoggedInUser(principal);
        model.addAttribute("leave",
                leaveRequestService.getLeaveById(id));
        model.addAttribute("unreadCount",
                notificationService.countUnread(counsellor));
        return "counsellor/leave-detail";
    }

    // ── Approve Leave ─────────────────────────────────────────────
    @PostMapping("/leave/{id}/approve")
    public String approveLeave(@PathVariable Long id,
                               @RequestParam String remarks,
                               Principal principal,
                               RedirectAttributes redirectAttributes) {
        User counsellor = getLoggedInUser(principal);
        leaveRequestService.approveLeave(id, counsellor, remarks);
        redirectAttributes.addFlashAttribute("success",
                "Leave approved successfully!");
        return "redirect:/counsellor/dashboard";
    }

    // ── Reject Leave ──────────────────────────────────────────────
    @PostMapping("/leave/{id}/reject")
    public String rejectLeave(@PathVariable Long id,
                              @RequestParam String remarks,
                              Principal principal,
                              RedirectAttributes redirectAttributes) {
        User counsellor = getLoggedInUser(principal);
        leaveRequestService.rejectLeave(id, counsellor, remarks);
        redirectAttributes.addFlashAttribute("success",
                "Leave rejected.");
        return "redirect:/counsellor/dashboard";
    }

    // ── My Students ───────────────────────────────────────────────
    @GetMapping("/students")
    public String myStudents(Model model, Principal principal) {
        User counsellor = getLoggedInUser(principal);
        model.addAttribute("students",
                userService.getStudentsUnderCounsellor(counsellor));
        model.addAttribute("unreadCount",
                notificationService.countUnread(counsellor));
        return "counsellor/students";
    }

    // ── Notifications ─────────────────────────────────────────────
    @GetMapping("/notifications")
    public String notifications(Model model, Principal principal) {
        User counsellor = getLoggedInUser(principal);
        model.addAttribute("notifications",
                notificationService.getUserNotifications(counsellor));
        notificationService.markAllAsRead(counsellor);
        model.addAttribute("unreadCount", 0);
        return "counsellor/notifications";
    }
}