package com.example.xGallery;

import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.example.xGallery.domain.UserAccount;
import com.example.xGallery.service.UserAccountService;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UserAccountService userAccountService;

    public CustomUserDetailsService(UserAccountService userAccountService) {
        this.userAccountService = userAccountService;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        UserAccount account = userAccountService.findByUsername(username)
            .orElseThrow(() -> new UsernameNotFoundException("사용자를 찾을 수 없습니다: " + username));

        return User.withUsername(account.getUsername())
            .password(account.getPassword())
            .roles("USER")
            .build();
    }
}
