package com.example.leavemanagement.controller;

import com.example.leavemanagement.entity.User;
import com.example.leavemanagement.enums.Role;
import com.example.leavemanagement.repository.UserRepository;
import com.example.leavemanagement.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;

@Controller
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;
    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder;

    // ── Login ──────────────────────────────────────────────────────
    @GetMapping("/login")
    public String loginPage() {
        return "auth/login";
    }

    // ── Dashboard redirect by role ─────────────────────────────────
    @GetMapping("/dashboard")
    public String dashboard(Principal principal) {
        var user = userService.getUserByIdentifier(principal.getName());
        return switch (user.getRole()) {
            case ADMIN      -> "redirect:/admin/dashboard";
            case COUNSELLOR -> "redirect:/counsellor/dashboard";
            case STUDENT    -> "redirect:/student/dashboard";
        };
    }

    // ── Student Register GET ───────────────────────────────────────
    @GetMapping("/register")
    public String registerPage() {
        return "auth/register";
    }

    // ── Student Register POST ──────────────────────────────────────
    @PostMapping("/register")
    public String registerStudent(
            @RequestParam String name,
            @RequestParam String usn,
            @RequestParam String password,
            @RequestParam String confirmPassword,
            @RequestParam(required = false) String department,
            @RequestParam(required = false) String academicYear,
            RedirectAttributes redirectAttributes) {

        // Check passwords match
        if (!password.equals(confirmPassword)) {
            redirectAttributes.addFlashAttribute("error",
                    "Passwords do not match!");
            return "redirect:/register";
        }

        try {
            User student = User.builder()
                    .name(name)
                    .usn(usn.toUpperCase())
                    .password(password)
                    .department(department)
                    .academicYear(academicYear)
                    .role(Role.STUDENT)
                    .build();

            userService.registerStudent(student);

            redirectAttributes.addFlashAttribute("success",
                    "Registration successful! Please login.");
            return "redirect:/login";

        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("error",
                    e.getMessage());
            return "redirect:/register";
        }
    }

    // ── Forgot Password GET ────────────────────────────────────────
    @GetMapping("/forgot-password")
    public String forgotPasswordPage(
            @RequestParam(required = false) String role,
            Model model) {
        model.addAttribute("role",
                role != null ? role : "STUDENT");
        return "auth/forgot-password";
    }

    // ── Forgot Password POST - Step 1 (verify identity) ───────────
    @PostMapping("/forgot-password")
    public String verifyIdentity(
            @RequestParam String identifier,
            @RequestParam String role,
            Model model) {

        try {
            // Check if user exists
            User user = userService.getUserByIdentifier(
                    identifier.toUpperCase());

            // Found — go to step 2
            model.addAttribute("step", "2");
            model.addAttribute("identifier",
                    identifier.toUpperCase());
            model.addAttribute("role", role);

        } catch (RuntimeException e) {
            model.addAttribute("error",
                    "No account found with this " +
                            (role.equals("STUDENT") ? "USN" : "Employee ID")
                            + ". Please check and try again.");
            model.addAttribute("role", role);
        }

        return "auth/forgot-password";
    }

    // ── Forgot Password POST - Step 2 (reset password) ────────────
    @PostMapping("/forgot-password/reset")
    public String resetPassword(
            @RequestParam String identifier,
            @RequestParam String newPassword,
            @RequestParam String confirmPassword,
            RedirectAttributes redirectAttributes) {

        if (!newPassword.equals(confirmPassword)) {
            redirectAttributes.addFlashAttribute("error",
                    "Passwords do not match!");
            return "redirect:/forgot-password";
        }

        try {
            User user = userService.getUserByIdentifier(
                    identifier.toUpperCase());
            user.setPassword(
                    passwordEncoder.encode(newPassword));
            userService.updateUser(user);

            redirectAttributes.addFlashAttribute("success",
                    "Password reset successful! Please login.");
            return "redirect:/login";

        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("error",
                    "Something went wrong. Please try again.");
            return "redirect:/forgot-password";
        }
    }
}