package com.example.xGallery.service;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;

import com.example.xGallery.domain.UserAccount;
import com.example.xGallery.domain.XUserProfile;
import com.example.xGallery.mapper.GalleryPostMapper;
import com.example.xGallery.mapper.XUserProfileMapper;

class XUserProfileServiceTest {

    @Test
    void deletesOwnedProfileAndItsCachedPosts() {
        UserAccountService userAccountService = mock(UserAccountService.class);
        XUserProfileMapper profileMapper = mock(XUserProfileMapper.class);
        GalleryPostMapper postMapper = mock(GalleryPostMapper.class);
        UserAccount account = account(7L);
        XUserProfile profile = profile(42L);
        when(userAccountService.findByUsername("account")).thenReturn(Optional.of(account));
        when(profileMapper.findByUserAccountIdOrderByCreatedAtDesc(7L)).thenReturn(List.of(profile));
        when(profileMapper.deleteByIdAndUserAccountId(42L, 7L)).thenReturn(1);
        XUserProfileService service = new XUserProfileService(userAccountService, profileMapper, postMapper);

        service.delete("account", 42L);

        verify(postMapper).deleteByXUserProfileId(42L);
        verify(profileMapper).deleteByIdAndUserAccountId(42L, 7L);
    }

    @Test
    void doesNotDeleteAnotherUsersProfileOrPosts() {
        UserAccountService userAccountService = mock(UserAccountService.class);
        XUserProfileMapper profileMapper = mock(XUserProfileMapper.class);
        GalleryPostMapper postMapper = mock(GalleryPostMapper.class);
        when(userAccountService.findByUsername("account")).thenReturn(Optional.of(account(7L)));
        when(profileMapper.findByUserAccountIdOrderByCreatedAtDesc(7L)).thenReturn(List.of(profile(42L)));
        XUserProfileService service = new XUserProfileService(userAccountService, profileMapper, postMapper);

        assertThatThrownBy(() -> service.delete("account", 99L))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("등록된 X 사용자를 찾을 수 없습니다.");

        verify(postMapper, never()).deleteByXUserProfileId(99L);
        verify(profileMapper, never()).deleteByIdAndUserAccountId(99L, 7L);
    }

    private UserAccount account(long id) {
        UserAccount account = new UserAccount();
        account.setId(id);
        return account;
    }

    private XUserProfile profile(long id) {
        XUserProfile profile = new XUserProfile();
        profile.setId(id);
        return profile;
    }
}
