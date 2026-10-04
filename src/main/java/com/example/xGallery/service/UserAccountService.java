package com.example.xGallery.service;

import java.util.Optional;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.example.xGallery.domain.UserAccount;
import com.example.xGallery.mapper.UserAccountMapper;

@Service
public class UserAccountService {

    private final UserAccountMapper userAccountMapper;
    private final PasswordEncoder passwordEncoder;

    public UserAccountService(UserAccountMapper userAccountMapper, PasswordEncoder passwordEncoder) {
        this.userAccountMapper = userAccountMapper;
        this.passwordEncoder = passwordEncoder;
    }

    public UserAccount register(String username, String password, String email) {
        String trimmedUsername = username == null ? "" : username.trim();
        String trimmedEmail = email == null ? "" : email.trim();

        if (trimmedUsername.isBlank() || password == null || password.isBlank() || trimmedEmail.isBlank()) {
            throw new IllegalArgumentException("아이디, 비밀번호, 이메일을 모두 입력해야 합니다.");
        }
        if (userAccountMapper.existsByUsername(trimmedUsername)) {
            throw new IllegalArgumentException("이미 사용 중인 사용자 이름입니다.");
        }
        if (userAccountMapper.existsByEmail(trimmedEmail)) {
            throw new IllegalArgumentException("이미 사용 중인 이메일입니다.");
        }

        UserAccount account = new UserAccount();
        account.setUsername(trimmedUsername);
        account.setPassword(passwordEncoder.encode(password));
        account.setEmail(trimmedEmail);
        userAccountMapper.insert(account);
        return account;
    }

    public Optional<UserAccount> findByUsername(String username) {
        return userAccountMapper.findByUsername(username == null ? "" : username.trim());
    }
}
