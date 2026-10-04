package com.example.xGallery;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.security.Principal;
import java.util.List;

import org.junit.jupiter.api.Test;

import com.example.xGallery.controller.GalleryController;
import com.example.xGallery.domain.XUserProfile;
import com.example.xGallery.service.GalleryService;
import com.example.xGallery.service.XUserProfileService;

class GalleryControllerTest {

    @Test
    void galleryApiShouldReturnPosts() {
        GalleryService galleryService = mock(GalleryService.class);
        XUserProfileService xUserProfileService = mock(XUserProfileService.class);
        XUserProfile profile = new XUserProfile();
        profile.setId(1L);
        profile.setScreenName("@alice");
        when(xUserProfileService.findByUsername("account")).thenReturn(List.of(profile));
        when(galleryService.getPosts(profile)).thenReturn(List.of(new GalleryPost("@alice", "hello", "https://example.com/p1.jpg")));

        GalleryController controller = new GalleryController(galleryService, xUserProfileService);
        Principal principal = () -> "account";
        List<GalleryPost> posts = controller.gallery("@alice", principal);

        assertThat(posts).hasSize(1);
        assertThat(posts.get(0).author()).isEqualTo("@alice");
    }

    @Test
    void allGalleryReturnsPostsForEveryRegisteredXUser() {
        GalleryService galleryService = mock(GalleryService.class);
        XUserProfileService xUserProfileService = mock(XUserProfileService.class);
        XUserProfile alice = new XUserProfile();
        alice.setId(1L);
        alice.setScreenName("alice");
        XUserProfile sunho = new XUserProfile();
        sunho.setId(2L);
        sunho.setScreenName("@sunho314");
        when(xUserProfileService.findByUsername("account")).thenReturn(List.of(alice, sunho));
        GalleryPost alicePost = new GalleryPost("@alice", "hello", null);
        GalleryPost sunhoPost = new GalleryPost("@sunho314", "world", null);
        when(galleryService.getPosts(alice)).thenReturn(List.of(alicePost));
        when(galleryService.getPosts(sunho)).thenReturn(List.of(sunhoPost));

        GalleryController controller = new GalleryController(galleryService, xUserProfileService);

        assertThat(controller.allGallery(() -> "account")).containsExactly(alicePost, sunhoPost);
        verify(galleryService, times(1)).getPosts(alice);
        verify(galleryService, times(1)).getPosts(sunho);
    }

    @Test
    void galleryRejectsUnregisteredXUser() {
        XUserProfileService xUserProfileService = mock(XUserProfileService.class);
        when(xUserProfileService.findByUsername("account")).thenReturn(List.of());
        GalleryController controller = new GalleryController(mock(GalleryService.class), xUserProfileService);

        org.assertj.core.api.Assertions.assertThatThrownBy(() -> controller.gallery("alice", () -> "account"))
            .isInstanceOf(org.springframework.web.server.ResponseStatusException.class)
            .hasMessageContaining("Registered X user not found");
    }
}
