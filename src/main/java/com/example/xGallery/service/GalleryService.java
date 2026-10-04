package com.example.xGallery.service;

import java.net.URI;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.util.UriBuilder;

import tools.jackson.databind.JsonNode;

import com.example.xGallery.GalleryPost;
import com.example.xGallery.domain.XUserProfile;
import com.example.xGallery.mapper.GalleryPostMapper;

@Service
public class GalleryService {

    private final RestClient restClient;
    private final GalleryPostMapper galleryPostMapper;
    private final String bearerToken;
    private final int maxResults;

    @Autowired
    public GalleryService(
        GalleryPostMapper galleryPostMapper,
        @Value("${x.api.base-url:https://api.x.com}") String baseUrl,
        @Value("${x.api.bearer-token:}") String bearerToken,
        @Value("${x.api.max-results:20}") int maxResults
    ) {
        this(RestClient.builder(), galleryPostMapper, baseUrl, bearerToken, maxResults);
    }

    GalleryService(RestClient.Builder restClientBuilder, GalleryPostMapper galleryPostMapper, String baseUrl, String bearerToken, int maxResults) {
        String normalizedBaseUrl = baseUrl.endsWith("/") ? baseUrl.substring(0, baseUrl.length() - 1) : baseUrl;
        this.restClient = restClientBuilder.baseUrl(normalizedBaseUrl).build();
        this.galleryPostMapper = galleryPostMapper;
        this.bearerToken = bearerToken;
        this.maxResults = Math.max(5, Math.min(maxResults, 100));
    }

    @Transactional
    public List<GalleryPost> getPosts(XUserProfile xUser) {
        List<GalleryPost> cachedPosts = galleryPostMapper.findByXUserProfileId(xUser.getId());
        if (bearerToken == null || bearerToken.isBlank() || bearerToken.contains("your-")) {
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "X API bearer token is not configured");
        }

        String username = xUser.getScreenName().trim().replaceFirst("^@", "");
        JsonNode userResponse = get(uriBuilder -> uriBuilder
            .path("/2/users/by/username/{username}")
            .queryParam("user.fields", "id")
            .build(username));
        String userId = text(userResponse.path("data").path("id"));
        if (userId.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "X API did not return a user ID");
        }

        boolean refreshCachedImages = cachedPosts.stream()
            .anyMatch(post -> post.imageUrl() == null || post.imageUrl().isBlank());
        JsonNode timeline = get(uriBuilder -> {
            uriBuilder
                .path("/2/users/{userId}/tweets")
                .queryParam("max_results", maxResults)
                .queryParam("tweet.fields", "created_at,attachments")
                .queryParam("expansions", "attachments.media_keys")
                .queryParam("media.fields", "media_key,type,url,preview_image_url");
            if (!refreshCachedImages && !cachedPosts.isEmpty() && cachedPosts.get(0).tweetId() != null) {
                uriBuilder.queryParam("since_id", cachedPosts.get(0).tweetId());
            }
            return uriBuilder.build(userId);
        });

        Map<String, String> imageUrls = imageUrls(timeline);
        JsonNode tweets = timeline.path("data");
        Set<String> cachedIds = new HashSet<>();
        Map<String, GalleryPost> cachedPostsById = new HashMap<>();
        for (GalleryPost cachedPost : cachedPosts) {
            cachedIds.add(cachedPost.tweetId());
            cachedPostsById.put(cachedPost.tweetId(), cachedPost);
        }
        if (tweets.isArray()) {
            for (JsonNode tweet : tweets) {
                String tweetId = text(tweet.path("id"));
                if (tweetId.isBlank()) {
                    continue;
                }
                String imageUrl = imageUrl(tweet, imageUrls);
                if (!cachedIds.add(tweetId)) {
                    GalleryPost cachedPost = cachedPostsById.get(tweetId);
                    if (cachedPost != null
                        && (cachedPost.imageUrl() == null || cachedPost.imageUrl().isBlank())
                        && imageUrl != null) {
                        galleryPostMapper.updateImageUrl(xUser.getId(), tweetId, imageUrl);
                    }
                    continue;
                }
                galleryPostMapper.insert(xUser.getId(), new GalleryPost(
                    tweetId,
                    "@" + username,
                    text(tweet.path("text")),
                    imageUrl,
                    text(tweet.path("created_at"))
                ));
            }
        }
        List<GalleryPost> updatedPosts = galleryPostMapper.findByXUserProfileId(xUser.getId());
        return updatedPosts.isEmpty() ? cachedPosts : updatedPosts;
    }

    private JsonNode get(Function<UriBuilder, URI> uriFactory) {
        try {
            return restClient.get()
                .uri(uriFactory)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + bearerToken.trim())
                .retrieve()
                .body(JsonNode.class);
        } catch (RestClientResponseException exception) {
            HttpStatus status = exception.getStatusCode().value() == HttpStatus.TOO_MANY_REQUESTS.value()
                ? HttpStatus.TOO_MANY_REQUESTS
                : HttpStatus.BAD_GATEWAY;
            String message = status == HttpStatus.TOO_MANY_REQUESTS
                ? "X API rate limit exceeded"
                : "X API request failed";
            throw new ResponseStatusException(status, message, exception);
        } catch (RestClientException exception) {
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "X API request failed", exception);
        }
    }

    private Map<String, String> imageUrls(JsonNode timeline) {
        Map<String, String> imageUrls = new HashMap<>();
        JsonNode media = timeline.path("includes").path("media");
        if (!media.isArray()) {
            return imageUrls;
        }

        for (JsonNode item : media) {
            String url = text(item.path("url"));
            if (url.isBlank()) {
                url = text(item.path("preview_image_url"));
            }
            if (!url.isBlank()) {
                imageUrls.put(text(item.path("media_key")), url);
            }
        }
        return imageUrls;
    }

    private String imageUrl(JsonNode tweet, Map<String, String> imageUrls) {
        JsonNode mediaKeys = tweet.path("attachments").path("media_keys");
        if (mediaKeys.isArray()) {
            for (JsonNode mediaKey : mediaKeys) {
                String imageUrl = imageUrls.get(text(mediaKey));
                if (imageUrl != null) {
                    return imageUrl;
                }
            }
        }
        return null;
    }

    private String text(JsonNode node) {
        return node.isMissingNode() || node.isNull() ? "" : node.asString();
    }
}
