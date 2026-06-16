package com.example.leavemanagement.security;

import com.example.leavemanagement.entity.User;
import com.example.leavemanagement.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.*;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String identifier)
            throws UsernameNotFoundException {

        System.out.println("🔍 Trying to login with: " + identifier);

        User user = userRepository.findByUsn(identifier)
                .or(() -> userRepository.findByEmployeeId(identifier))
                .orElseThrow(() ->
                        new UsernameNotFoundException(
                                "No user found with: " + identifier));

        System.out.println("✅ Found user: " + user.getName()
                + " Role: " + user.getRole());

        if (!user.isActive()) {
            throw new UsernameNotFoundException(
                    "Account deactivated: " + identifier);
        }

        SimpleGrantedAuthority authority =
                new SimpleGrantedAuthority("ROLE_" + user.getRole().name());

        return new org.springframework.security.core.userdetails.User(
                user.getUsn() != null
                        ? user.getUsn()
                        : user.getEmployeeId(),
                user.getPassword(),
                List.of(authority)
        );
    }
}