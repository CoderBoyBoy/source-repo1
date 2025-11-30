package com.gitmanager.service;

import com.gitmanager.dto.CreateTagRequest;
import com.gitmanager.exception.RepositoryException;
import com.gitmanager.exception.RepositoryException.ErrorCode;
import com.gitmanager.model.TagInfo;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevObject;
import org.eclipse.jgit.revwalk.RevTag;
import org.eclipse.jgit.revwalk.RevWalk;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

@Service
public class TagService {

    private static final Logger logger = LoggerFactory.getLogger(TagService.class);
    private static final String REFS_TAGS_PREFIX = "refs/tags/";

    private final RepositoryService repositoryService;

    public TagService(RepositoryService repositoryService) {
        this.repositoryService = repositoryService;
    }

    public List<TagInfo> listTags(String repoName) {
        List<TagInfo> tags = new ArrayList<>();

        try (Repository repository = repositoryService.openRepository(repoName);
             Git git = new Git(repository)) {

            List<Ref> refs = git.tagList().call();

            for (Ref ref : refs) {
                TagInfo tagInfo = createTagInfo(repository, ref);
                tags.add(tagInfo);
            }

        } catch (GitAPIException | IOException e) {
            throw new RepositoryException("Failed to list tags: " + e.getMessage(), e);
        }

        return tags;
    }

    public TagInfo createTag(String repoName, CreateTagRequest request) {
        try (Repository repository = repositoryService.openRepository(repoName);
             Git git = new Git(repository)) {

            // Check if tag already exists
            List<Ref> existingTags = git.tagList().call();
            for (Ref ref : existingTags) {
                if (ref.getName().equals(REFS_TAGS_PREFIX + request.getName())) {
                    throw new RepositoryException("Tag already exists: " + request.getName(),
                            ErrorCode.TAG_ALREADY_EXISTS);
                }
            }

            var tagCommand = git.tag()
                    .setName(request.getName())
                    .setAnnotated(request.isAnnotated());

            if (request.getMessage() != null && !request.getMessage().isEmpty()) {
                tagCommand.setMessage(request.getMessage());
            }

            if (request.getCommitId() != null && !request.getCommitId().isEmpty()) {
                ObjectId objectId = repository.resolve(request.getCommitId());
                if (objectId == null) {
                    throw new RepositoryException("Commit not found: " + request.getCommitId(),
                            ErrorCode.INVALID_OPERATION);
                }
                try (RevWalk revWalk = new RevWalk(repository)) {
                    RevCommit commit = revWalk.parseCommit(objectId);
                    tagCommand.setObjectId(commit);
                }
            }

            Ref ref = tagCommand.call();

            logger.info("Created tag: {} in repository: {}", request.getName(), repoName);
            return createTagInfo(repository, ref);

        } catch (GitAPIException | IOException e) {
            throw new RepositoryException("Failed to create tag: " + e.getMessage(), e);
        }
    }

    public TagInfo getTag(String repoName, String tagName) {
        try (Repository repository = repositoryService.openRepository(repoName);
             Git git = new Git(repository)) {

            List<Ref> refs = git.tagList().call();
            for (Ref ref : refs) {
                String name = ref.getName();
                if (name.startsWith(REFS_TAGS_PREFIX)) {
                    name = name.substring(REFS_TAGS_PREFIX.length());
                }
                if (name.equals(tagName)) {
                    return createTagInfo(repository, ref);
                }
            }

            throw new RepositoryException("Tag not found: " + tagName, ErrorCode.TAG_NOT_FOUND);

        } catch (GitAPIException | IOException e) {
            throw new RepositoryException("Failed to get tag: " + e.getMessage(), e);
        }
    }

    public void deleteTag(String repoName, String tagName) {
        try (Repository repository = repositoryService.openRepository(repoName);
             Git git = new Git(repository)) {

            git.tagDelete()
                    .setTags(tagName)
                    .call();

            logger.info("Deleted tag: {} from repository: {}", tagName, repoName);

        } catch (GitAPIException e) {
            if (e.getMessage().contains("not found") || e.getMessage().contains("Ref")) {
                throw new RepositoryException("Tag not found: " + tagName, ErrorCode.TAG_NOT_FOUND);
            }
            throw new RepositoryException("Failed to delete tag: " + e.getMessage(), e);
        }
    }

    private TagInfo createTagInfo(Repository repository, Ref ref) throws IOException {
        String tagName = ref.getName();
        if (tagName.startsWith(REFS_TAGS_PREFIX)) {
            tagName = tagName.substring(REFS_TAGS_PREFIX.length());
        }

        ObjectId objectId = ref.getPeeledObjectId();
        if (objectId == null) {
            objectId = ref.getObjectId();
        }

        try (RevWalk revWalk = new RevWalk(repository)) {
            RevObject revObject = revWalk.parseAny(ref.getObjectId());

            if (revObject instanceof RevTag revTag) {
                // Annotated tag
                LocalDateTime tagDate = LocalDateTime.ofInstant(
                        revTag.getTaggerIdent().getWhen().toInstant(),
                        ZoneId.systemDefault());

                RevCommit commit = revWalk.parseCommit(revTag.getObject());

                return new TagInfo(
                        tagName,
                        commit.getName(),
                        revTag.getFullMessage(),
                        revTag.getTaggerIdent().getName(),
                        tagDate,
                        true
                );
            } else if (revObject instanceof RevCommit revCommit) {
                // Lightweight tag
                LocalDateTime commitDate = LocalDateTime.ofInstant(
                        Instant.ofEpochSecond(revCommit.getCommitTime()),
                        ZoneId.systemDefault());

                return new TagInfo(
                        tagName,
                        revCommit.getName(),
                        null,
                        revCommit.getAuthorIdent().getName(),
                        commitDate,
                        false
                );
            }
        }

        return new TagInfo(tagName, objectId != null ? objectId.getName() : null, 
                null, null, null, false);
    }
}
