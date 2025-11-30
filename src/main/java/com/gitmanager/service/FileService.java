package com.gitmanager.service;

import com.gitmanager.exception.RepositoryException;
import com.gitmanager.exception.RepositoryException.ErrorCode;
import com.gitmanager.model.FileContent;
import com.gitmanager.model.FileTreeNode;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.ObjectLoader;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevTree;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.treewalk.TreeWalk;
import org.eclipse.jgit.treewalk.filter.PathFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

@Service
public class FileService {

    private static final Logger logger = LoggerFactory.getLogger(FileService.class);
    private static final int MAX_FILE_SIZE = 10 * 1024 * 1024; // 10MB

    private final RepositoryService repositoryService;

    public FileService(RepositoryService repositoryService) {
        this.repositoryService = repositoryService;
    }

    public FileTreeNode getFileTree(String repoName, String ref, String path) {
        try (Repository repository = repositoryService.openRepository(repoName)) {
            ObjectId commitId = resolveRef(repository, ref);
            
            try (RevWalk revWalk = new RevWalk(repository)) {
                RevCommit commit = revWalk.parseCommit(commitId);
                RevTree tree = commit.getTree();

                FileTreeNode root = new FileTreeNode(
                        path.isEmpty() ? repoName : getFileName(path),
                        path,
                        FileTreeNode.FileType.DIRECTORY,
                        0
                );

                try (TreeWalk treeWalk = new TreeWalk(repository)) {
                    treeWalk.addTree(tree);
                    treeWalk.setRecursive(false);

                    if (!path.isEmpty()) {
                        treeWalk.setFilter(PathFilter.create(path));
                    }

                    Map<String, FileTreeNode> nodeMap = new HashMap<>();
                    nodeMap.put("", root);

                    while (treeWalk.next()) {
                        String filePath = treeWalk.getPathString();
                        String fileName = treeWalk.getNameString();
                        boolean isSubtree = treeWalk.isSubtree();

                        FileTreeNode node = new FileTreeNode(
                                fileName,
                                filePath,
                                isSubtree ? FileTreeNode.FileType.DIRECTORY : FileTreeNode.FileType.FILE,
                                0
                        );

                        String parentPath = getParentPath(filePath);
                        FileTreeNode parent = nodeMap.getOrDefault(parentPath, root);
                        parent.addChild(node);

                        if (isSubtree) {
                            nodeMap.put(filePath, node);
                            treeWalk.enterSubtree();
                        }
                    }
                }

                return root;
            }
        } catch (IOException e) {
            throw new RepositoryException("Failed to get file tree: " + e.getMessage(), e);
        }
    }

    public FileContent getFileContent(String repoName, String ref, String filePath) {
        try (Repository repository = repositoryService.openRepository(repoName)) {
            ObjectId commitId = resolveRef(repository, ref);

            try (RevWalk revWalk = new RevWalk(repository)) {
                RevCommit commit = revWalk.parseCommit(commitId);
                RevTree tree = commit.getTree();

                try (TreeWalk treeWalk = TreeWalk.forPath(repository, filePath, tree)) {
                    if (treeWalk == null) {
                        throw new RepositoryException("File not found: " + filePath, ErrorCode.FILE_NOT_FOUND);
                    }

                    ObjectId objectId = treeWalk.getObjectId(0);
                    ObjectLoader loader = repository.open(objectId);

                    long size = loader.getSize();
                    if (size > MAX_FILE_SIZE) {
                        return new FileContent(filePath, null, null, size, false);
                    }

                    byte[] bytes = loader.getBytes();
                    boolean isBinary = isBinaryContent(bytes);

                    String content = null;
                    String encoding = null;
                    if (!isBinary) {
                        content = new String(bytes, StandardCharsets.UTF_8);
                        encoding = "UTF-8";
                    }

                    return new FileContent(filePath, content, encoding, size, isBinary);
                }
            }
        } catch (IOException e) {
            throw new RepositoryException("Failed to get file content: " + e.getMessage(), e);
        }
    }

    private ObjectId resolveRef(Repository repository, String ref) throws IOException {
        if (ref == null || ref.isEmpty()) {
            ref = "HEAD";
        }

        ObjectId objectId = repository.resolve(ref);
        if (objectId == null) {
            throw new RepositoryException("Invalid reference: " + ref, ErrorCode.INVALID_OPERATION);
        }
        return objectId;
    }

    private String getFileName(String path) {
        int lastSlash = path.lastIndexOf('/');
        return lastSlash >= 0 ? path.substring(lastSlash + 1) : path;
    }

    private String getParentPath(String path) {
        int lastSlash = path.lastIndexOf('/');
        return lastSlash >= 0 ? path.substring(0, lastSlash) : "";
    }

    private boolean isBinaryContent(byte[] bytes) {
        if (bytes == null || bytes.length == 0) {
            return false;
        }

        int checkLength = Math.min(bytes.length, 8000);
        for (int i = 0; i < checkLength; i++) {
            if (bytes[i] == 0) {
                return true;
            }
        }
        return false;
    }
}
