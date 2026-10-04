package com.guruge.hardware.security;

import com.guruge.hardware.entity.User;
import com.guruge.hardware.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String usernameOrEmail) throws UsernameNotFoundException {
        User user = userRepository.findByUsername(usernameOrEmail)
                .or(() -> userRepository.findByEmail(usernameOrEmail))
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + usernameOrEmail));

        String status = user.getStatus();
        if ("LOCKED".equalsIgnoreCase(status)) {
            throw new LockedException("User account is locked: " + usernameOrEmail);
        }
        if (!"ACTIVE".equalsIgnoreCase(status)) {
            throw new DisabledException("User account is not active: " + usernameOrEmail);
        }
        return UserPrincipal.fromUser(user);
    }
}
