package com.example.xGallery.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.queryParam;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;
import org.springframework.web.server.ResponseStatusException;

import com.example.xGallery.GalleryPost;
import com.example.xGallery.domain.XUserProfile;
import com.example.xGallery.mapper.GalleryPostMapper;

class GalleryServiceTest {

    @Test
    void fetchesRegisteredUsersTweetsAndMediaFromXApi() {
        RestClient.Builder restClientBuilder = RestClient.builder();
        MockRestServiceServer server = MockRestServiceServer.bindTo(restClientBuilder).build();
        GalleryPostMapper postMapper = mock(GalleryPostMapper.class);
        XUserProfile xUser = xUser(42L, "alice");
        GalleryPost savedPost = new GalleryPost("tweet-1", "@alice", "Live post", "https://images.example/post.jpg", "2026-10-04T10:00:00Z");
        when(postMapper.findByXUserProfileId(42L)).thenReturn(List.of()).thenReturn(List.of(savedPost));
        GalleryService galleryService = new GalleryService(restClientBuilder, postMapper, "https://api.x.com", "test-token", 10);

        server.expect(requestTo("https://api.x.com/2/users/by/username/alice?user.fields=id"))
            .andExpect(method(HttpMethod.GET))
            .andExpect(header("Authorization", "Bearer test-token"))
            .andRespond(withSuccess("""
                {"data":{"id":"123"}}
                """, MediaType.APPLICATION_JSON));
        server.expect(requestTo("https://api.x.com/2/users/123/tweets?max_results=10&tweet.fields=created_at,attachments&expansions=attachments.media_keys&media.fields=media_key,type,url,preview_image_url"))
            .andExpect(method(HttpMethod.GET))
            .andExpect(header("Authorization", "Bearer test-token"))
            .andExpect(queryParam("max_results", "10"))
            .andRespond(withSuccess("""
                {
                  "data":[{"id":"tweet-1","text":"Live post","created_at":"2026-10-04T10:00:00Z","attachments":{"media_keys":["media-1"]}}],
                  "includes":{"media":[{"media_key":"media-1","type":"photo","url":"https://images.example/post.jpg"}]}
                }
                """, MediaType.APPLICATION_JSON));

        List<GalleryPost> posts = galleryService.getPosts(xUser);

        assertThat(posts).containsExactly(savedPost);
        verify(postMapper).insert(42L, savedPost);
        server.verify();
    }

    @Test
    void returnsCachedPostsWhenXHasNoNewPosts() {
        RestClient.Builder restClientBuilder = RestClient.builder();
        MockRestServiceServer server = MockRestServiceServer.bindTo(restClientBuilder).build();
        GalleryPostMapper postMapper = mock(GalleryPostMapper.class);
        XUserProfile xUser = xUser(42L, "alice");
        GalleryPost cachedPost = new GalleryPost(
            "tweet-1", "@alice", "Saved post", "https://images.example/post.jpg", "2026-10-04T10:00:00Z");
        when(postMapper.findByXUserProfileId(42L)).thenReturn(List.of(cachedPost));
        GalleryService galleryService = new GalleryService(restClientBuilder, postMapper, "https://api.x.com", "test-token", 10);

        server.expect(requestTo("https://api.x.com/2/users/by/username/alice?user.fields=id"))
            .andRespond(withSuccess("""
                {"data":{"id":"123"}}
                """, MediaType.APPLICATION_JSON));
        server.expect(requestTo("https://api.x.com/2/users/123/tweets?max_results=10&tweet.fields=created_at,attachments&expansions=attachments.media_keys&media.fields=media_key,type,url,preview_image_url&since_id=tweet-1"))
            .andExpect(queryParam("since_id", "tweet-1"))
            .andRespond(withSuccess("""
                {"data":[]}
                """, MediaType.APPLICATION_JSON));

        assertThat(galleryService.getPosts(xUser)).containsExactly(cachedPost);
        server.verify();
    }

    @Test
    void refreshesImageForCachedPostsThatWerePreviouslyStoredWithoutOne() {
        RestClient.Builder restClientBuilder = RestClient.builder();
        MockRestServiceServer server = MockRestServiceServer.bindTo(restClientBuilder).build();
        GalleryPostMapper postMapper = mock(GalleryPostMapper.class);
        XUserProfile xUser = xUser(42L, "alice");
        GalleryPost cachedPost = new GalleryPost("tweet-1", "@alice", "Saved post", null, "2026-10-04T10:00:00Z");
        GalleryPost postWithImage = new GalleryPost(
            "tweet-1", "@alice", "Saved post", "https://images.example/preview.jpg", "2026-10-04T10:00:00Z");
        when(postMapper.findByXUserProfileId(42L)).thenReturn(List.of(cachedPost)).thenReturn(List.of(postWithImage));
        GalleryService galleryService = new GalleryService(restClientBuilder, postMapper, "https://api.x.com", "test-token", 10);

        server.expect(requestTo("https://api.x.com/2/users/by/username/alice?user.fields=id"))
            .andRespond(withSuccess("""
                {"data":{"id":"123"}}
                """, MediaType.APPLICATION_JSON));
        server.expect(requestTo("https://api.x.com/2/users/123/tweets?max_results=10&tweet.fields=created_at,attachments&expansions=attachments.media_keys&media.fields=media_key,type,url,preview_image_url"))
            .andRespond(withSuccess("""
                {
                  "data":[{"id":"tweet-1","text":"Saved post","created_at":"2026-10-04T10:00:00Z","attachments":{"media_keys":["media-1"]}}],
                  "includes":{"media":[{"media_key":"media-1","type":"video","preview_image_url":"https://images.example/preview.jpg"}]}
                }
                """, MediaType.APPLICATION_JSON));

        assertThat(galleryService.getPosts(xUser)).containsExactly(postWithImage);

        verify(postMapper).updateImageUrl(42L, "tweet-1", "https://images.example/preview.jpg");
        server.verify();
    }

    @Test
    void preservesRateLimitStatusFromXApi() {
        RestClient.Builder restClientBuilder = RestClient.builder();
        MockRestServiceServer server = MockRestServiceServer.bindTo(restClientBuilder).build();
        GalleryService galleryService = new GalleryService(restClientBuilder, mock(GalleryPostMapper.class), "https://api.x.com", "test-token", 10);
        XUserProfile xUser = xUser(42L, "alice");

        server.expect(requestTo("https://api.x.com/2/users/by/username/alice?user.fields=id"))
            .andRespond(withStatus(HttpStatus.TOO_MANY_REQUESTS));

        assertThatThrownBy(() -> galleryService.getPosts(xUser))
            .isInstanceOfSatisfying(ResponseStatusException.class, exception ->
                assertThat(exception.getStatusCode()).isEqualTo(HttpStatus.TOO_MANY_REQUESTS));

        server.verify();
    }

    private XUserProfile xUser(long id, String screenName) {
        XUserProfile xUser = new XUserProfile();
        xUser.setId(id);
        xUser.setScreenName(screenName);
        return xUser;
    }
}