package com.gitmanager.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public class CreateRepositoryRequest {

    @NotBlank(message = "Repository name is required")
    @Pattern(regexp = "^[a-zA-Z0-9_-]+$", message = "Repository name can only contain alphanumeric characters, hyphens, and underscores")
    private String name;

    private String description;
    private boolean bare;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public boolean isBare() {
        return bare;
    }

    public void setBare(boolean bare) {
        this.bare = bare;
    }
}
