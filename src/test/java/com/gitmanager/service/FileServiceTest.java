package com.gitmanager.service;

import com.gitmanager.config.GitRepositoryConfig;
import com.gitmanager.dto.CreateRepositoryRequest;
import com.gitmanager.exception.RepositoryException;
import com.gitmanager.model.FileContent;
import com.gitmanager.model.FileTreeNode;
import org.eclipse.jgit.api.Git;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class FileServiceTest {

    @TempDir
    Path tempDir;

    private RepositoryService repositoryService;
    private FileService fileService;
    private static final String TEST_REPO = "test-repo";

    @BeforeEach
    void setUp() throws Exception {
        GitRepositoryConfig config = new GitRepositoryConfig();
        config.setBasePath(tempDir.toString());

        SshService sshService = new SshService(new com.gitmanager.config.SshConfig());
        repositoryService = new RepositoryService(config, sshService);
        fileService = new FileService(repositoryService);

        // Create a test repository with files
        CreateRepositoryRequest request = new CreateRepositoryRequest();
        request.setName(TEST_REPO);
        repositoryService.createRepository(request);

        Path repoPath = tempDir.resolve(TEST_REPO);
        try (Git git = Git.open(repoPath.toFile())) {
            // Create directory structure
            Files.createDirectories(repoPath.resolve("src/main/java"));
            Files.writeString(repoPath.resolve("README.md"), "# Test Repository");
            Files.writeString(repoPath.resolve("src/main/java/App.java"), "public class App {}");
            
            git.add().addFilepattern(".").call();
            git.commit().setMessage("Initial commit").call();
        }
    }

    @Test
    void getFileTree_shouldReturnRootTree() {
        FileTreeNode tree = fileService.getFileTree(TEST_REPO, "HEAD", "");

        assertNotNull(tree);
        assertEquals(FileTreeNode.FileType.DIRECTORY, tree.getType());
        assertFalse(tree.getChildren().isEmpty());
    }

    @Test
    void getFileTree_shouldIncludeFilesAndDirectories() {
        FileTreeNode tree = fileService.getFileTree(TEST_REPO, "HEAD", "");

        boolean hasReadme = tree.getChildren().stream()
                .anyMatch(node -> node.getName().equals("README.md") && 
                         node.getType() == FileTreeNode.FileType.FILE);
        boolean hasSrcDir = tree.getChildren().stream()
                .anyMatch(node -> node.getName().equals("src") && 
                         node.getType() == FileTreeNode.FileType.DIRECTORY);

        assertTrue(hasReadme, "Should have README.md file");
        assertTrue(hasSrcDir, "Should have src directory");
    }

    @Test
    void getFileContent_shouldReturnFileContents() {
        FileContent content = fileService.getFileContent(TEST_REPO, "HEAD", "README.md");

        assertNotNull(content);
        assertEquals("README.md", content.getPath());
        assertEquals("# Test Repository", content.getContent());
        assertFalse(content.isBinary());
    }

    @Test
    void getFileContent_shouldThrowExceptionForNonExistentFile() {
        RepositoryException exception = assertThrows(RepositoryException.class,
                () -> fileService.getFileContent(TEST_REPO, "HEAD", "non-existent.txt"));
        assertEquals(RepositoryException.ErrorCode.FILE_NOT_FOUND, exception.getErrorCode());
    }

    @Test
    void getFileContent_shouldHandleNestedFiles() {
        FileContent content = fileService.getFileContent(TEST_REPO, "HEAD", "src/main/java/App.java");

        assertNotNull(content);
        assertEquals("src/main/java/App.java", content.getPath());
        assertEquals("public class App {}", content.getContent());
    }
}
