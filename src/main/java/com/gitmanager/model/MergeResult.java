package com.gitmanager.model;

public class MergeResult {

    private boolean successful;
    private String mergedCommitId;
    private String message;
    private MergeStatus status;

    public MergeResult() {
    }

    public MergeResult(boolean successful, String mergedCommitId, String message, MergeStatus status) {
        this.successful = successful;
        this.mergedCommitId = mergedCommitId;
        this.message = message;
        this.status = status;
    }

    public boolean isSuccessful() {
        return successful;
    }

    public void setSuccessful(boolean successful) {
        this.successful = successful;
    }

    public String getMergedCommitId() {
        return mergedCommitId;
    }

    public void setMergedCommitId(String mergedCommitId) {
        this.mergedCommitId = mergedCommitId;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public MergeStatus getStatus() {
        return status;
    }

    public void setStatus(MergeStatus status) {
        this.status = status;
    }

    public enum MergeStatus {
        MERGED, FAST_FORWARD, ALREADY_UP_TO_DATE, CONFLICTING, FAILED, ABORTED
    }
}
