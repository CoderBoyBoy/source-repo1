package com.gitmanager.service;

import com.gitmanager.config.GitRepositoryConfig;
import com.gitmanager.dto.CreateRepositoryRequest;
import com.gitmanager.dto.CreateTagRequest;
import com.gitmanager.exception.RepositoryException;
import com.gitmanager.model.TagInfo;
import org.eclipse.jgit.api.Git;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class TagServiceTest {

    @TempDir
    Path tempDir;

    private RepositoryService repositoryService;
    private TagService tagService;
    private static final String TEST_REPO = "test-repo";

    @BeforeEach
    void setUp() throws Exception {
        GitRepositoryConfig config = new GitRepositoryConfig();
        config.setBasePath(tempDir.toString());

        SshService sshService = new SshService(new com.gitmanager.config.SshConfig());
        repositoryService = new RepositoryService(config, sshService);
        tagService = new TagService(repositoryService);

        // Create a test repository with initial commit
        CreateRepositoryRequest request = new CreateRepositoryRequest();
        request.setName(TEST_REPO);
        repositoryService.createRepository(request);

        // Add an initial commit so we can create tags
        Path repoPath = tempDir.resolve(TEST_REPO);
        try (Git git = Git.open(repoPath.toFile())) {
            Path testFile = repoPath.resolve("README.md");
            Files.writeString(testFile, "# Test Repository");
            git.add().addFilepattern(".").call();
            git.commit().setMessage("Initial commit").call();
        }
    }

    @Test
    void listTags_shouldReturnEmptyListWhenNoTags() {
        List<TagInfo> tags = tagService.listTags(TEST_REPO);
        assertTrue(tags.isEmpty());
    }

    @Test
    void createTag_shouldCreateLightweightTag() {
        CreateTagRequest request = new CreateTagRequest();
        request.setName("v1.0.0");
        request.setAnnotated(false);

        TagInfo result = tagService.createTag(TEST_REPO, request);

        assertNotNull(result);
        assertEquals("v1.0.0", result.getName());
        assertFalse(result.isAnnotated());
    }

    @Test
    void createTag_shouldCreateAnnotatedTag() {
        CreateTagRequest request = new CreateTagRequest();
        request.setName("v2.0.0");
        request.setMessage("Release version 2.0.0");
        request.setAnnotated(true);

        TagInfo result = tagService.createTag(TEST_REPO, request);

        assertNotNull(result);
        assertEquals("v2.0.0", result.getName());
        assertTrue(result.isAnnotated());
    }

    @Test
    void createTag_shouldThrowExceptionForDuplicateTag() {
        CreateTagRequest request = new CreateTagRequest();
        request.setName("duplicate-tag");

        tagService.createTag(TEST_REPO, request);

        RepositoryException exception = assertThrows(RepositoryException.class,
                () -> tagService.createTag(TEST_REPO, request));
        assertEquals(RepositoryException.ErrorCode.TAG_ALREADY_EXISTS, exception.getErrorCode());
    }

    @Test
    void getTag_shouldReturnTagDetails() {
        CreateTagRequest request = new CreateTagRequest();
        request.setName("test-tag");
        tagService.createTag(TEST_REPO, request);

        TagInfo result = tagService.getTag(TEST_REPO, "test-tag");

        assertNotNull(result);
        assertEquals("test-tag", result.getName());
    }

    @Test
    void getTag_shouldThrowExceptionForNonExistentTag() {
        RepositoryException exception = assertThrows(RepositoryException.class,
                () -> tagService.getTag(TEST_REPO, "non-existent"));
        assertEquals(RepositoryException.ErrorCode.TAG_NOT_FOUND, exception.getErrorCode());
    }

    @Test
    void deleteTag_shouldRemoveTag() {
        CreateTagRequest request = new CreateTagRequest();
        request.setName("delete-tag");
        tagService.createTag(TEST_REPO, request);

        tagService.deleteTag(TEST_REPO, "delete-tag");

        List<TagInfo> tags = tagService.listTags(TEST_REPO);
        assertFalse(tags.stream().anyMatch(t -> t.getName().equals("delete-tag")));
    }
}
