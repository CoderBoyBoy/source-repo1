package com.gitmanager.service;

import com.gitmanager.config.GitRepositoryConfig;
import com.gitmanager.dto.CloneRepositoryRequest;
import com.gitmanager.dto.CreateRepositoryRequest;
import com.gitmanager.exception.RepositoryException;
import com.gitmanager.exception.RepositoryException.ErrorCode;
import com.gitmanager.model.RepositoryInfo;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import org.eclipse.jgit.transport.SshTransport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;

@Service
public class RepositoryService {

    private static final Logger logger = LoggerFactory.getLogger(RepositoryService.class);

    private final GitRepositoryConfig config;
    private final SshService sshService;

    public RepositoryService(GitRepositoryConfig config, SshService sshService) {
        this.config = config;
        this.sshService = sshService;
        initializeBasePath();
    }

    private void initializeBasePath() {
        try {
            Path basePath = Path.of(config.getBasePath());
            if (!Files.exists(basePath)) {
                Files.createDirectories(basePath);
                logger.info("Created repository base path: {}", basePath);
            }
        } catch (IOException e) {
            logger.error("Failed to create base path: {}", e.getMessage());
        }
    }

    public RepositoryInfo createRepository(CreateRepositoryRequest request) {
        Path repoPath = Path.of(config.getBasePath(), request.getName());
        
        if (Files.exists(repoPath)) {
            throw new RepositoryException("Repository already exists: " + request.getName(),
                    ErrorCode.REPOSITORY_ALREADY_EXISTS);
        }

        try {
            Git git;
            if (request.isBare()) {
                git = Git.init()
                        .setDirectory(repoPath.toFile())
                        .setBare(true)
                        .call();
            } else {
                git = Git.init()
                        .setDirectory(repoPath.toFile())
                        .call();
            }
            git.close();

            logger.info("Created repository: {}", request.getName());
            return getRepositoryInfo(request.getName());
        } catch (GitAPIException e) {
            throw new RepositoryException("Failed to create repository: " + e.getMessage(), e);
        }
    }

    public RepositoryInfo cloneRepository(CloneRepositoryRequest request) {
        Path repoPath = Path.of(config.getBasePath(), request.getName());

        if (Files.exists(repoPath)) {
            throw new RepositoryException("Repository already exists: " + request.getName(),
                    ErrorCode.REPOSITORY_ALREADY_EXISTS);
        }

        try {
            var cloneCommand = Git.cloneRepository()
                    .setURI(request.getUrl())
                    .setDirectory(repoPath.toFile());

            if (request.getBranch() != null && !request.getBranch().isEmpty()) {
                cloneCommand.setBranch(request.getBranch());
            }

            if (request.isUseSsh()) {
                cloneCommand.setTransportConfigCallback(transport -> {
                    if (transport instanceof SshTransport sshTransport) {
                        sshTransport.setSshSessionFactory(sshService.getSshSessionFactory());
                    }
                });
            }

            Git git = cloneCommand.call();
            git.close();

            logger.info("Cloned repository: {} from {}", request.getName(), request.getUrl());
            return getRepositoryInfo(request.getName());
        } catch (GitAPIException e) {
            throw new RepositoryException("Failed to clone repository: " + e.getMessage(), e, ErrorCode.CLONE_FAILED);
        }
    }

    public List<RepositoryInfo> listRepositories() {
        List<RepositoryInfo> repositories = new ArrayList<>();
        Path basePath = Path.of(config.getBasePath());

        if (!Files.exists(basePath)) {
            return repositories;
        }

        try (Stream<Path> paths = Files.list(basePath)) {
            paths.filter(Files::isDirectory)
                    .forEach(path -> {
                        try {
                            if (isGitRepository(path)) {
                                repositories.add(getRepositoryInfo(path.getFileName().toString()));
                            }
                        } catch (Exception e) {
                            logger.warn("Could not read repository: {}", path.getFileName());
                        }
                    });
        } catch (IOException e) {
            throw new RepositoryException("Failed to list repositories: " + e.getMessage(), e);
        }

        return repositories;
    }

    public RepositoryInfo getRepositoryInfo(String name) {
        Path repoPath = Path.of(config.getBasePath(), name);
        
        if (!isGitRepository(repoPath)) {
            throw new RepositoryException("Repository not found: " + name, ErrorCode.REPOSITORY_NOT_FOUND);
        }

        try (Repository repository = openRepository(name)) {
            File repoDir = repository.getDirectory();
            boolean isBare = repository.isBare();
            String currentBranch = repository.getBranch();

            LocalDateTime createdAt = LocalDateTime.ofInstant(
                    Instant.ofEpochMilli(repoDir.lastModified()),
                    ZoneId.systemDefault());

            return new RepositoryInfo(
                    name,
                    repoPath.toString(),
                    null,
                    currentBranch,
                    createdAt,
                    createdAt,
                    isBare
            );
        } catch (IOException e) {
            throw new RepositoryException("Failed to get repository info: " + e.getMessage(), e);
        }
    }

    public void deleteRepository(String name) {
        Path repoPath = Path.of(config.getBasePath(), name);
        
        if (!isGitRepository(repoPath)) {
            throw new RepositoryException("Repository not found: " + name, ErrorCode.REPOSITORY_NOT_FOUND);
        }

        try (Stream<Path> walk = Files.walk(repoPath)) {
            walk.sorted(Comparator.reverseOrder())
                    .map(Path::toFile)
                    .forEach(File::delete);
            logger.info("Deleted repository: {}", name);
        } catch (IOException e) {
            throw new RepositoryException("Failed to delete repository: " + e.getMessage(), e);
        }
    }

    public Repository openRepository(String name) {
        Path repoPath = Path.of(config.getBasePath(), name);
        
        if (!isGitRepository(repoPath)) {
            throw new RepositoryException("Repository not found: " + name, ErrorCode.REPOSITORY_NOT_FOUND);
        }

        try {
            FileRepositoryBuilder builder = new FileRepositoryBuilder();
            Path gitDir = repoPath.resolve(".git");
            
            if (Files.exists(gitDir)) {
                return builder.setGitDir(gitDir.toFile())
                        .readEnvironment()
                        .findGitDir()
                        .build();
            } else {
                // Bare repository
                return builder.setGitDir(repoPath.toFile())
                        .readEnvironment()
                        .build();
            }
        } catch (IOException e) {
            throw new RepositoryException("Failed to open repository: " + e.getMessage(), e);
        }
    }

    private boolean isGitRepository(Path path) {
        if (!Files.isDirectory(path)) {
            return false;
        }
        Path gitDir = path.resolve(".git");
        Path headFile = path.resolve("HEAD");
        return Files.exists(gitDir) || (Files.exists(headFile) && Files.isDirectory(path.resolve("objects")));
    }
}
