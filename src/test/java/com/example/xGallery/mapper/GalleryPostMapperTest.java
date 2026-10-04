package com.example.xGallery.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;

import com.example.xGallery.GalleryPost;

@SpringBootTest
class GalleryPostMapperTest {

    private static final long TEST_PROFILE_ID = 987654321L;

    @Autowired
    private GalleryPostMapper galleryPostMapper;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void storesAndLoadsPostsFromDatabase() {
        jdbcTemplate.execute("""
            CREATE TABLE IF NOT EXISTS gallery_post (
                id BIGINT AUTO_INCREMENT PRIMARY KEY,
                x_user_profile_id BIGINT NOT NULL,
                tweet_id VARCHAR(50) NOT NULL,
                author VARCHAR(100) NOT NULL,
                post_text TEXT NOT NULL,
                image_url VARCHAR(2048),
                tweet_created_at VARCHAR(50),
                cached_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                CONSTRAINT uk_gallery_post_tweet UNIQUE (x_user_profile_id, tweet_id)
            )
            """);
        jdbcTemplate.update("DELETE FROM gallery_post WHERE x_user_profile_id = ?", TEST_PROFILE_ID);
        GalleryPost post = new GalleryPost("tweet-42", "@alice", "Saved post", null, "2026-10-04T10:00:00Z");

        galleryPostMapper.insert(TEST_PROFILE_ID, post);

        List<GalleryPost> savedPosts = galleryPostMapper.findByXUserProfileId(TEST_PROFILE_ID);
        assertThat(savedPosts).containsExactly(post);

        galleryPostMapper.updateImageUrl(TEST_PROFILE_ID, "tweet-42", "https://example.com/updated.jpg");
        assertThat(galleryPostMapper.findByXUserProfileId(TEST_PROFILE_ID))
            .containsExactly(new GalleryPost(
                "tweet-42", "@alice", "Saved post", "https://example.com/updated.jpg", "2026-10-04T10:00:00Z"));

        assertThat(galleryPostMapper.deleteByXUserProfileId(TEST_PROFILE_ID)).isEqualTo(1);
        assertThat(galleryPostMapper.findByXUserProfileId(TEST_PROFILE_ID)).isEmpty();
    }
}
