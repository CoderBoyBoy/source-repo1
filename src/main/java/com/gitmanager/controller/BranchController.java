package com.gitmanager.controller;

import com.gitmanager.dto.ApiResponse;
import com.gitmanager.dto.CreateBranchRequest;
import com.gitmanager.dto.MergeBranchRequest;
import com.gitmanager.model.BranchInfo;
import com.gitmanager.model.MergeResult;
import com.gitmanager.service.BranchService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/repositories/{repoName}/branches")
public class BranchController {

    private final BranchService branchService;

    public BranchController(BranchService branchService) {
        this.branchService = branchService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<BranchInfo>>> listBranches(
            @PathVariable String repoName,
            @RequestParam(defaultValue = "false") boolean includeRemote) {
        List<BranchInfo> branches = branchService.listBranches(repoName, includeRemote);
        return ResponseEntity.ok(ApiResponse.success(branches));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<BranchInfo>> createBranch(
            @PathVariable String repoName,
            @Valid @RequestBody CreateBranchRequest request) {
        BranchInfo branch = branchService.createBranch(repoName, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Branch created successfully", branch));
    }

    @PostMapping("/{branchName}/checkout")
    public ResponseEntity<ApiResponse<BranchInfo>> checkoutBranch(
            @PathVariable String repoName,
            @PathVariable String branchName) {
        BranchInfo branch = branchService.checkoutBranch(repoName, branchName);
        return ResponseEntity.ok(ApiResponse.success("Checked out branch successfully", branch));
    }

    @PostMapping("/merge")
    public ResponseEntity<ApiResponse<MergeResult>> mergeBranch(
            @PathVariable String repoName,
            @Valid @RequestBody MergeBranchRequest request) {
        MergeResult result = branchService.mergeBranch(repoName, request);
        String message = result.isSuccessful() ? "Merge completed successfully" : "Merge failed";
        return ResponseEntity.ok(ApiResponse.success(message, result));
    }

    @DeleteMapping("/{branchName}")
    public ResponseEntity<ApiResponse<Void>> deleteBranch(
            @PathVariable String repoName,
            @PathVariable String branchName,
            @RequestParam(defaultValue = "false") boolean force) {
        branchService.deleteBranch(repoName, branchName, force);
        return ResponseEntity.ok(ApiResponse.success("Branch deleted successfully", null));
    }
}
