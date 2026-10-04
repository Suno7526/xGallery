package com.example.xGallery.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.example.xGallery.GalleryPost;

@Mapper
public interface GalleryPostMapper {
    List<GalleryPost> findByXUserProfileId(@Param("xUserProfileId") Long xUserProfileId);
    int insert(@Param("xUserProfileId") Long xUserProfileId, @Param("post") GalleryPost post);
    int updateImageUrl(@Param("xUserProfileId") Long xUserProfileId, @Param("tweetId") String tweetId, @Param("imageUrl") String imageUrl);
    int deleteByXUserProfileId(@Param("xUserProfileId") Long xUserProfileId);
}
