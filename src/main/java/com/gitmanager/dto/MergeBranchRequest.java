package com.gitmanager.dto;

import jakarta.validation.constraints.NotBlank;

public class MergeBranchRequest {

    @NotBlank(message = "Source branch name is required")
    private String sourceBranch;

    private String message;
    private boolean squash;

    public String getSourceBranch() {
        return sourceBranch;
    }

    public void setSourceBranch(String sourceBranch) {
        this.sourceBranch = sourceBranch;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public boolean isSquash() {
        return squash;
    }

    public void setSquash(boolean squash) {
        this.squash = squash;
    }
}
