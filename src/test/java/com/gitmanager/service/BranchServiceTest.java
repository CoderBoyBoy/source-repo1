package com.gitmanager.service;

import com.gitmanager.config.GitRepositoryConfig;
import com.gitmanager.dto.CreateBranchRequest;
import com.gitmanager.dto.CreateRepositoryRequest;
import com.gitmanager.exception.RepositoryException;
import com.gitmanager.model.BranchInfo;
import org.eclipse.jgit.api.Git;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class BranchServiceTest {

    @TempDir
    Path tempDir;

    private RepositoryService repositoryService;
    private BranchService branchService;
    private static final String TEST_REPO = "test-repo";

    @BeforeEach
    void setUp() throws Exception {
        GitRepositoryConfig config = new GitRepositoryConfig();
        config.setBasePath(tempDir.toString());
        
        SshService sshService = new SshService(new com.gitmanager.config.SshConfig());
        repositoryService = new RepositoryService(config, sshService);
        branchService = new BranchService(repositoryService);

        // Create a test repository with initial commit
        CreateRepositoryRequest request = new CreateRepositoryRequest();
        request.setName(TEST_REPO);
        repositoryService.createRepository(request);

        // Add an initial commit so we can create branches
        Path repoPath = tempDir.resolve(TEST_REPO);
        try (Git git = Git.open(repoPath.toFile())) {
            Path testFile = repoPath.resolve("README.md");
            Files.writeString(testFile, "# Test Repository");
            git.add().addFilepattern(".").call();
            git.commit().setMessage("Initial commit").call();
        }
    }

    @Test
    void listBranches_shouldReturnAllBranches() {
        List<BranchInfo> branches = branchService.listBranches(TEST_REPO, false);

        assertFalse(branches.isEmpty());
        assertTrue(branches.stream().anyMatch(b -> b.getName().equals("master") || b.getName().equals("main")));
    }

    @Test
    void createBranch_shouldCreateNewBranch() {
        CreateBranchRequest request = new CreateBranchRequest();
        request.setName("feature-branch");

        BranchInfo result = branchService.createBranch(TEST_REPO, request);

        assertNotNull(result);
        assertEquals("feature-branch", result.getName());
    }

    @Test
    void createBranch_shouldThrowExceptionForDuplicateBranch() {
        CreateBranchRequest request = new CreateBranchRequest();
        request.setName("duplicate-branch");

        branchService.createBranch(TEST_REPO, request);

        RepositoryException exception = assertThrows(RepositoryException.class,
                () -> branchService.createBranch(TEST_REPO, request));
        assertEquals(RepositoryException.ErrorCode.BRANCH_ALREADY_EXISTS, exception.getErrorCode());
    }

    @Test
    void checkoutBranch_shouldSwitchToSpecifiedBranch() {
        CreateBranchRequest request = new CreateBranchRequest();
        request.setName("checkout-test");
        branchService.createBranch(TEST_REPO, request);

        BranchInfo result = branchService.checkoutBranch(TEST_REPO, "checkout-test");

        assertNotNull(result);
    }

    @Test
    void deleteBranch_shouldRemoveBranch() {
        CreateBranchRequest request = new CreateBranchRequest();
        request.setName("delete-branch");
        branchService.createBranch(TEST_REPO, request);

        branchService.deleteBranch(TEST_REPO, "delete-branch", true);

        List<BranchInfo> branches = branchService.listBranches(TEST_REPO, false);
        assertFalse(branches.stream().anyMatch(b -> b.getName().equals("delete-branch")));
    }
}
