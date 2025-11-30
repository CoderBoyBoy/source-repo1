package com.gitmanager.controller;

import com.gitmanager.dto.ApiResponse;
import com.gitmanager.dto.CloneRepositoryRequest;
import com.gitmanager.dto.CreateRepositoryRequest;
import com.gitmanager.model.RepositoryInfo;
import com.gitmanager.service.RepositoryService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/repositories")
public class RepositoryController {

    private final RepositoryService repositoryService;

    public RepositoryController(RepositoryService repositoryService) {
        this.repositoryService = repositoryService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<RepositoryInfo>>> listRepositories() {
        List<RepositoryInfo> repositories = repositoryService.listRepositories();
        return ResponseEntity.ok(ApiResponse.success(repositories));
    }

    @GetMapping("/{name}")
    public ResponseEntity<ApiResponse<RepositoryInfo>> getRepository(@PathVariable String name) {
        RepositoryInfo repository = repositoryService.getRepositoryInfo(name);
        return ResponseEntity.ok(ApiResponse.success(repository));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<RepositoryInfo>> createRepository(
            @Valid @RequestBody CreateRepositoryRequest request) {
        RepositoryInfo repository = repositoryService.createRepository(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Repository created successfully", repository));
    }

    @PostMapping("/clone")
    public ResponseEntity<ApiResponse<RepositoryInfo>> cloneRepository(
            @Valid @RequestBody CloneRepositoryRequest request) {
        RepositoryInfo repository = repositoryService.cloneRepository(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Repository cloned successfully", repository));
    }

    @DeleteMapping("/{name}")
    public ResponseEntity<ApiResponse<Void>> deleteRepository(@PathVariable String name) {
        repositoryService.deleteRepository(name);
        return ResponseEntity.ok(ApiResponse.success("Repository deleted successfully", null));
    }
}
