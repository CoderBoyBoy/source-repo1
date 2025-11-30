package com.gitmanager.dto;

import jakarta.validation.constraints.NotBlank;

public class CreateTagRequest {

    @NotBlank(message = "Tag name is required")
    private String name;

    private String message;
    private String commitId;
    private boolean annotated;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getCommitId() {
        return commitId;
    }

    public void setCommitId(String commitId) {
        this.commitId = commitId;
    }

    public boolean isAnnotated() {
        return annotated;
    }

    public void setAnnotated(boolean annotated) {
        this.annotated = annotated;
    }
}
