package com.example.xGallery.mapper;

import java.util.Optional;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.example.xGallery.domain.UserAccount;

@Mapper
public interface UserAccountMapper {
    int insert(UserAccount userAccount);
    Optional<UserAccount> findByUsername(@Param("username") String username);
    boolean existsByUsername(@Param("username") String username);
    boolean existsByEmail(@Param("email") String email);
}
