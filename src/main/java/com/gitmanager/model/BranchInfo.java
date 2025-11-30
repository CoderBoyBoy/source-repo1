package com.gitmanager.model;

import java.time.LocalDateTime;

public class BranchInfo {

    private String name;
    private String commitId;
    private String commitMessage;
    private String author;
    private LocalDateTime commitDate;
    private boolean isRemote;
    private boolean isCurrent;

    public BranchInfo() {
    }

    public BranchInfo(String name, String commitId, String commitMessage, String author,
                      LocalDateTime commitDate, boolean isRemote, boolean isCurrent) {
        this.name = name;
        this.commitId = commitId;
        this.commitMessage = commitMessage;
        this.author = author;
        this.commitDate = commitDate;
        this.isRemote = isRemote;
        this.isCurrent = isCurrent;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCommitId() {
        return commitId;
    }

    public void setCommitId(String commitId) {
        this.commitId = commitId;
    }

    public String getCommitMessage() {
        return commitMessage;
    }

    public void setCommitMessage(String commitMessage) {
        this.commitMessage = commitMessage;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public LocalDateTime getCommitDate() {
        return commitDate;
    }

    public void setCommitDate(LocalDateTime commitDate) {
        this.commitDate = commitDate;
    }

    public boolean isRemote() {
        return isRemote;
    }

    public void setRemote(boolean remote) {
        isRemote = remote;
    }

    public boolean isCurrent() {
        return isCurrent;
    }

    public void setCurrent(boolean current) {
        isCurrent = current;
    }
}
