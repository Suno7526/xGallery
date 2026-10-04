package com.example.xGallery.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.xGallery.GalleryPost;
import com.example.xGallery.service.GalleryService;

@RestController
@RequestMapping("/api")
public class GalleryController {

    private final GalleryService galleryService;

    public GalleryController(GalleryService galleryService) {
        this.galleryService = galleryService;
    }

    @GetMapping("/gallery")
    public List<GalleryPost> gallery() {
        return galleryService.getPosts();
    }
}
