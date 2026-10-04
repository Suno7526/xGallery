package com.example.xGallery;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.Test;

import com.example.xGallery.controller.GalleryController;
import com.example.xGallery.service.GalleryService;

class GalleryControllerTest {

    @Test
    void galleryApiShouldReturnPosts() {
        GalleryService galleryService = mock(GalleryService.class);
        when(galleryService.getPosts()).thenReturn(List.of(new GalleryPost("alice", "hello", "https://example.com/p1.jpg")));

        GalleryController controller = new GalleryController(galleryService);
        List<GalleryPost> posts = controller.gallery();

        assertThat(posts).hasSize(1);
        assertThat(posts.get(0).author()).isEqualTo("alice");
    }
}
