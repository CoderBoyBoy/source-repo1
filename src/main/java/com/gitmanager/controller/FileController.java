package com.gitmanager.controller;

import com.gitmanager.dto.ApiResponse;
import com.gitmanager.model.FileContent;
import com.gitmanager.model.FileTreeNode;
import com.gitmanager.service.FileService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/repositories/{repoName}/files")
public class FileController {

    private final FileService fileService;

    public FileController(FileService fileService) {
        this.fileService = fileService;
    }

    @GetMapping("/tree")
    public ResponseEntity<ApiResponse<FileTreeNode>> getFileTree(
            @PathVariable String repoName,
            @RequestParam(defaultValue = "") String ref,
            @RequestParam(defaultValue = "") String path) {
        FileTreeNode tree = fileService.getFileTree(repoName, ref, path);
        return ResponseEntity.ok(ApiResponse.success(tree));
    }

    @GetMapping("/content")
    public ResponseEntity<ApiResponse<FileContent>> getFileContent(
            @PathVariable String repoName,
            @RequestParam(defaultValue = "") String ref,
            @RequestParam String path) {
        FileContent content = fileService.getFileContent(repoName, ref, path);
        return ResponseEntity.ok(ApiResponse.success(content));
    }
}
