package com.example.xGallery.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.xGallery.domain.UserAccount;
import com.example.xGallery.domain.XUserProfile;
import com.example.xGallery.mapper.GalleryPostMapper;
import com.example.xGallery.mapper.XUserProfileMapper;

@Service
public class XUserProfileService {

    private final UserAccountService userAccountService;
    private final XUserProfileMapper xUserProfileMapper;
    private final GalleryPostMapper galleryPostMapper;

    public XUserProfileService(
        UserAccountService userAccountService,
        XUserProfileMapper xUserProfileMapper,
        GalleryPostMapper galleryPostMapper
    ) {
        this.userAccountService = userAccountService;
        this.xUserProfileMapper = xUserProfileMapper;
        this.galleryPostMapper = galleryPostMapper;
    }

    @Transactional
    public XUserProfile register(String username, String screenName) {
        String normalized = screenName == null ? "" : screenName.trim();
        if (normalized.isBlank()) {
            throw new IllegalArgumentException("X 사용자 ID를 입력해 주세요.");
        }

        UserAccount userAccount = userAccountService.findByUsername(username)
            .orElseThrow(() -> new IllegalArgumentException("로그인된 사용자를 찾을 수 없습니다."));

        if (xUserProfileMapper.existsByUserAccountIdAndScreenName(userAccount.getId(), normalized)) {
            throw new IllegalArgumentException("이미 등록된 X 사용자입니다.");
        }

        XUserProfile profile = new XUserProfile();
        profile.setUserAccountId(userAccount.getId());
        profile.setScreenName(normalized);
        profile.setDisplayName(normalized);
        xUserProfileMapper.insert(profile);
        return profile;
    }

    public List<XUserProfile> findByUsername(String username) {
        UserAccount userAccount = userAccountService.findByUsername(username)
            .orElseThrow(() -> new IllegalArgumentException("로그인된 사용자를 찾을 수 없습니다."));
        return xUserProfileMapper.findByUserAccountIdOrderByCreatedAtDesc(userAccount.getId());
    }

    @Transactional
    public void delete(String username, Long profileId) {
        UserAccount userAccount = userAccountService.findByUsername(username)
            .orElseThrow(() -> new IllegalArgumentException("로그인된 사용자를 찾을 수 없습니다."));
        boolean profileBelongsToUser = xUserProfileMapper.findByUserAccountIdOrderByCreatedAtDesc(userAccount.getId())
            .stream()
            .anyMatch(profile -> profile.getId().equals(profileId));
        if (!profileBelongsToUser) {
            throw new IllegalArgumentException("등록된 X 사용자를 찾을 수 없습니다.");
        }

        galleryPostMapper.deleteByXUserProfileId(profileId);
        if (xUserProfileMapper.deleteByIdAndUserAccountId(profileId, userAccount.getId()) != 1) {
            throw new IllegalStateException("X 사용자 삭제에 실패했습니다.");
        }
    }
}
