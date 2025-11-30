package com.gitmanager.model;

public class FileContent {

    private String path;
    private String content;
    private String encoding;
    private long size;
    private boolean binary;

    public FileContent() {
    }

    public FileContent(String path, String content, String encoding, long size, boolean binary) {
        this.path = path;
        this.content = content;
        this.encoding = encoding;
        this.size = size;
        this.binary = binary;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getEncoding() {
        return encoding;
    }

    public void setEncoding(String encoding) {
        this.encoding = encoding;
    }

    public long getSize() {
        return size;
    }

    public void setSize(long size) {
        this.size = size;
    }

    public boolean isBinary() {
        return binary;
    }

    public void setBinary(boolean binary) {
        this.binary = binary;
    }
}
