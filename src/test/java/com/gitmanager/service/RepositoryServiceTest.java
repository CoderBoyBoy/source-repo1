package com.gitmanager.service;

import com.gitmanager.config.GitRepositoryConfig;
import com.gitmanager.dto.CreateRepositoryRequest;
import com.gitmanager.exception.RepositoryException;
import com.gitmanager.model.RepositoryInfo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class RepositoryServiceTest {

    @TempDir
    Path tempDir;

    private RepositoryService repositoryService;
    private GitRepositoryConfig config;
    private SshService sshService;

    @BeforeEach
    void setUp() {
        config = new GitRepositoryConfig();
        config.setBasePath(tempDir.toString());
        
        sshService = new SshService(new com.gitmanager.config.SshConfig());
        repositoryService = new RepositoryService(config, sshService);
    }

    @Test
    void createRepository_shouldCreateNewRepository() {
        CreateRepositoryRequest request = new CreateRepositoryRequest();
        request.setName("test-repo");
        request.setDescription("Test repository");
        request.setBare(false);

        RepositoryInfo result = repositoryService.createRepository(request);

        assertNotNull(result);
        assertEquals("test-repo", result.getName());
        assertFalse(result.isBare());
    }

    @Test
    void createRepository_shouldCreateBareRepository() {
        CreateRepositoryRequest request = new CreateRepositoryRequest();
        request.setName("bare-repo");
        request.setBare(true);

        RepositoryInfo result = repositoryService.createRepository(request);

        assertNotNull(result);
        assertEquals("bare-repo", result.getName());
        assertTrue(result.isBare());
    }

    @Test
    void createRepository_shouldThrowExceptionForDuplicateName() {
        CreateRepositoryRequest request = new CreateRepositoryRequest();
        request.setName("duplicate-repo");

        repositoryService.createRepository(request);

        RepositoryException exception = assertThrows(RepositoryException.class,
                () -> repositoryService.createRepository(request));
        assertEquals(RepositoryException.ErrorCode.REPOSITORY_ALREADY_EXISTS, exception.getErrorCode());
    }

    @Test
    void listRepositories_shouldReturnAllRepositories() {
        CreateRepositoryRequest request1 = new CreateRepositoryRequest();
        request1.setName("repo1");
        repositoryService.createRepository(request1);

        CreateRepositoryRequest request2 = new CreateRepositoryRequest();
        request2.setName("repo2");
        repositoryService.createRepository(request2);

        List<RepositoryInfo> repositories = repositoryService.listRepositories();

        assertEquals(2, repositories.size());
    }

    @Test
    void getRepositoryInfo_shouldReturnRepositoryDetails() {
        CreateRepositoryRequest request = new CreateRepositoryRequest();
        request.setName("info-repo");
        repositoryService.createRepository(request);

        RepositoryInfo result = repositoryService.getRepositoryInfo("info-repo");

        assertNotNull(result);
        assertEquals("info-repo", result.getName());
    }

    @Test
    void getRepositoryInfo_shouldThrowExceptionForNonExistentRepo() {
        RepositoryException exception = assertThrows(RepositoryException.class,
                () -> repositoryService.getRepositoryInfo("non-existent"));
        assertEquals(RepositoryException.ErrorCode.REPOSITORY_NOT_FOUND, exception.getErrorCode());
    }

    @Test
    void deleteRepository_shouldRemoveRepository() {
        CreateRepositoryRequest request = new CreateRepositoryRequest();
        request.setName("delete-repo");
        repositoryService.createRepository(request);

        repositoryService.deleteRepository("delete-repo");

        RepositoryException exception = assertThrows(RepositoryException.class,
                () -> repositoryService.getRepositoryInfo("delete-repo"));
        assertEquals(RepositoryException.ErrorCode.REPOSITORY_NOT_FOUND, exception.getErrorCode());
    }

    @Test
    void deleteRepository_shouldThrowExceptionForNonExistentRepo() {
        RepositoryException exception = assertThrows(RepositoryException.class,
                () -> repositoryService.deleteRepository("non-existent"));
        assertEquals(RepositoryException.ErrorCode.REPOSITORY_NOT_FOUND, exception.getErrorCode());
    }
}
