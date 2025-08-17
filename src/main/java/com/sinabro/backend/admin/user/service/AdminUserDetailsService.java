package com.sinabro.backend.admin.user.service;

import com.sinabro.backend.user.parent.entity.User;
import com.sinabro.backend.user.parent.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.*;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    // 스프링 시큐리티는 username을 받음 → 우리는 userId 사용
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User u = userRepository.findByUserId(username)
                .orElseThrow(() -> new UsernameNotFoundException("사용자를 찾을 수 없습니다: " + username));

        String rawRole = (u.getRole() == null) ? "parent" : u.getRole().toLowerCase();
        String springRole = rawRole.equals("admin") ? "ROLE_ADMIN" : "ROLE_PARENT";

        return org.springframework.security.core.userdetails.User
                .withUsername(u.getUserId())
                .password(u.getUserPw() == null ? "" : u.getUserPw()) // 소셜 계정은 null일 수 있음
                .authorities(List.of(new SimpleGrantedAuthority(springRole)))
                .accountExpired(false)
                .accountLocked(false)
                .credentialsExpired(false)
                .disabled(false)
                .build();
    }
}
