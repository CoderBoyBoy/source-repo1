package com.gitmanager.model;

import java.time.LocalDateTime;

public class TagInfo {

    private String name;
    private String commitId;
    private String message;
    private String tagger;
    private LocalDateTime tagDate;
    private boolean annotated;

    public TagInfo() {
    }

    public TagInfo(String name, String commitId, String message, String tagger,
                   LocalDateTime tagDate, boolean annotated) {
        this.name = name;
        this.commitId = commitId;
        this.message = message;
        this.tagger = tagger;
        this.tagDate = tagDate;
        this.annotated = annotated;
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

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getTagger() {
        return tagger;
    }

    public void setTagger(String tagger) {
        this.tagger = tagger;
    }

    public LocalDateTime getTagDate() {
        return tagDate;
    }

    public void setTagDate(LocalDateTime tagDate) {
        this.tagDate = tagDate;
    }

    public boolean isAnnotated() {
        return annotated;
    }

    public void setAnnotated(boolean annotated) {
        this.annotated = annotated;
    }
}
