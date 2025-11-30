package com.gitmanager.model;

import java.time.LocalDateTime;

public class RepositoryInfo {

    private String name;
    private String path;
    private String description;
    private String currentBranch;
    private LocalDateTime createdAt;
    private LocalDateTime lastModified;
    private boolean bare;

    public RepositoryInfo() {
    }

    public RepositoryInfo(String name, String path, String description, String currentBranch,
                          LocalDateTime createdAt, LocalDateTime lastModified, boolean bare) {
        this.name = name;
        this.path = path;
        this.description = description;
        this.currentBranch = currentBranch;
        this.createdAt = createdAt;
        this.lastModified = lastModified;
        this.bare = bare;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getCurrentBranch() {
        return currentBranch;
    }

    public void setCurrentBranch(String currentBranch) {
        this.currentBranch = currentBranch;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getLastModified() {
        return lastModified;
    }

    public void setLastModified(LocalDateTime lastModified) {
        this.lastModified = lastModified;
    }

    public boolean isBare() {
        return bare;
    }

    public void setBare(boolean bare) {
        this.bare = bare;
    }
}
