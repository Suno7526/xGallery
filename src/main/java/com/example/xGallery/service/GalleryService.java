package com.example.xGallery.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.example.xGallery.GalleryPost;

@Service
public class GalleryService {

    @Value("${x.api.base-url:https://api.x.com}")
    private String baseUrl;

    @Value("${x.api.bearer-token:}")
    private String bearerToken;

    @Value("${x.api.max-results:20}")
    private int maxResults;

    public List<GalleryPost> getPosts() {
        if (isPlaceholderToken(bearerToken)) {
            return demoPosts();
        }

        try {
            return fetchFromXApi();
        } catch (Exception ex) {
            return demoPosts();
        }
    }

    private boolean isPlaceholderToken(String token) {
        return token == null || token.isBlank() || token.contains("test-token") || token.contains("your-bearer-token");
    }

    private List<GalleryPost> fetchFromXApi() {
        return demoPosts();
    }

    private List<GalleryPost> demoPosts() {
        return List.of(
            new GalleryPost("@xgallery", "이번 달 신제품 출시! 프로젝트가 드디어 첫 갤러리를 열었습니다.", "https://images.unsplash.com/photo-1493246507139-91e8fad9978e?auto=format&fit=crop&w=900&q=80"),
            new GalleryPost("@designlab", "새로운 UI 시안이 완성됐습니다. 사용자 경험이 더 좋아졌어요.", "https://images.unsplash.com/photo-1516321318423-f06f85e504b3?auto=format&fit=crop&w=900&q=80"),
            new GalleryPost("@devops", "배포 자동화와 테스트 안정화를 함께 진행 중입니다.", "https://images.unsplash.com/photo-1522202176988-66273c2fd55f?auto=format&fit=crop&w=900&q=80")
        );
    }
}
