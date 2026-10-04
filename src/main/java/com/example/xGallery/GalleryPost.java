package com.example.xGallery;

public record GalleryPost(String tweetId, String author, String text, String imageUrl, String createdAt) {
    public GalleryPost(String author, String text, String imageUrl) {
        this(null, author, text, imageUrl, null);
    }
}
