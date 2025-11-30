package com.gitmanager.dto;

import jakarta.validation.constraints.NotBlank;

public class CloneRepositoryRequest {

    @NotBlank(message = "Repository URL is required")
    private String url;

    @NotBlank(message = "Repository name is required")
    private String name;

    private String branch;
    private boolean useSsh;

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getBranch() {
        return branch;
    }

    public void setBranch(String branch) {
        this.branch = branch;
    }

    public boolean isUseSsh() {
        return useSsh;
    }

    public void setUseSsh(boolean useSsh) {
        this.useSsh = useSsh;
    }
}
