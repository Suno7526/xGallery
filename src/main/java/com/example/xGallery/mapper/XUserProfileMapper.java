package com.example.xGallery.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.example.xGallery.domain.XUserProfile;

@Mapper
public interface XUserProfileMapper {
    int insert(XUserProfile profile);
    List<XUserProfile> findByUserAccountIdOrderByCreatedAtDesc(@Param("userAccountId") Long userAccountId);
    boolean existsByUserAccountIdAndScreenName(@Param("userAccountId") Long userAccountId, @Param("screenName") String screenName);
}
