package com.gitmanager.model;

import java.util.ArrayList;
import java.util.List;

public class FileTreeNode {

    private String name;
    private String path;
    private FileType type;
    private long size;
    private List<FileTreeNode> children;

    public FileTreeNode() {
        this.children = new ArrayList<>();
    }

    public FileTreeNode(String name, String path, FileType type, long size) {
        this.name = name;
        this.path = path;
        this.type = type;
        this.size = size;
        this.children = new ArrayList<>();
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

    public FileType getType() {
        return type;
    }

    public void setType(FileType type) {
        this.type = type;
    }

    public long getSize() {
        return size;
    }

    public void setSize(long size) {
        this.size = size;
    }

    public List<FileTreeNode> getChildren() {
        return children;
    }

    public void setChildren(List<FileTreeNode> children) {
        this.children = children;
    }

    public void addChild(FileTreeNode child) {
        this.children.add(child);
    }

    public enum FileType {
        FILE, DIRECTORY, SYMLINK
    }
}
