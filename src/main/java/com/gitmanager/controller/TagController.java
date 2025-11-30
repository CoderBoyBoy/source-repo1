package com.gitmanager.controller;

import com.gitmanager.dto.ApiResponse;
import com.gitmanager.dto.CreateTagRequest;
import com.gitmanager.model.TagInfo;
import com.gitmanager.service.TagService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/repositories/{repoName}/tags")
public class TagController {

    private final TagService tagService;

    public TagController(TagService tagService) {
        this.tagService = tagService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<TagInfo>>> listTags(@PathVariable String repoName) {
        List<TagInfo> tags = tagService.listTags(repoName);
        return ResponseEntity.ok(ApiResponse.success(tags));
    }

    @GetMapping("/{tagName}")
    public ResponseEntity<ApiResponse<TagInfo>> getTag(
            @PathVariable String repoName,
            @PathVariable String tagName) {
        TagInfo tag = tagService.getTag(repoName, tagName);
        return ResponseEntity.ok(ApiResponse.success(tag));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<TagInfo>> createTag(
            @PathVariable String repoName,
            @Valid @RequestBody CreateTagRequest request) {
        TagInfo tag = tagService.createTag(repoName, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Tag created successfully", tag));
    }

    @DeleteMapping("/{tagName}")
    public ResponseEntity<ApiResponse<Void>> deleteTag(
            @PathVariable String repoName,
            @PathVariable String tagName) {
        tagService.deleteTag(repoName, tagName);
        return ResponseEntity.ok(ApiResponse.success("Tag deleted successfully", null));
    }
}
