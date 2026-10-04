package com.example.xGallery.controller;

import java.security.Principal;
import java.util.ArrayList;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.example.xGallery.GalleryPost;
import com.example.xGallery.domain.XUserProfile;
import com.example.xGallery.service.GalleryService;
import com.example.xGallery.service.XUserProfileService;

@RestController
@RequestMapping("/api")
public class GalleryController {

    private final GalleryService galleryService;
    private final XUserProfileService xUserProfileService;

    public GalleryController(GalleryService galleryService, XUserProfileService xUserProfileService) {
        this.galleryService = galleryService;
        this.xUserProfileService = xUserProfileService;
    }

    @GetMapping("/gallery/all")
    public List<GalleryPost> allGallery(Principal principal) {
        List<GalleryPost> posts = new ArrayList<>();
        for (XUserProfile xUser : xUserProfileService.findByUsername(principal.getName())) {
            posts.addAll(galleryService.getPosts(xUser));
        }
        return posts;
    }

    @GetMapping("/gallery")
    public List<GalleryPost> gallery(@RequestParam String screenName, Principal principal) {
        String normalizedScreenName = normalizeScreenName(screenName);
        XUserProfile selectedUser = xUserProfileService.findByUsername(principal.getName()).stream()
            .filter(xUser -> normalizeScreenName(xUser.getScreenName()).equalsIgnoreCase(normalizedScreenName))
            .findFirst()
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Registered X user not found"));
        return galleryService.getPosts(selectedUser);
    }

    private String normalizeScreenName(String screenName) {
        return screenName == null ? "" : screenName.trim().replaceFirst("^@", "");
    }
}
