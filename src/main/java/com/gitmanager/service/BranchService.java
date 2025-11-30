package com.gitmanager.service;

import com.gitmanager.dto.CreateBranchRequest;
import com.gitmanager.dto.MergeBranchRequest;
import com.gitmanager.exception.RepositoryException;
import com.gitmanager.exception.RepositoryException.ErrorCode;
import com.gitmanager.model.BranchInfo;
import com.gitmanager.model.MergeResult;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.ListBranchCommand;
import org.eclipse.jgit.api.MergeCommand;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
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
public class BranchService {

    private static final Logger logger = LoggerFactory.getLogger(BranchService.class);
    private static final String REFS_HEADS_PREFIX = "refs/heads/";
    private static final String REFS_REMOTES_PREFIX = "refs/remotes/";

    private final RepositoryService repositoryService;

    public BranchService(RepositoryService repositoryService) {
        this.repositoryService = repositoryService;
    }

    public List<BranchInfo> listBranches(String repoName, boolean includeRemote) {
        List<BranchInfo> branches = new ArrayList<>();

        try (Repository repository = repositoryService.openRepository(repoName);
             Git git = new Git(repository)) {

            String currentBranch = repository.getBranch();

            ListBranchCommand.ListMode listMode = includeRemote 
                    ? ListBranchCommand.ListMode.ALL 
                    : null;

            List<Ref> refs = git.branchList()
                    .setListMode(listMode)
                    .call();

            for (Ref ref : refs) {
                BranchInfo branchInfo = createBranchInfo(repository, ref, currentBranch);
                branches.add(branchInfo);
            }

        } catch (GitAPIException | IOException e) {
            throw new RepositoryException("Failed to list branches: " + e.getMessage(), e);
        }

        return branches;
    }

    public BranchInfo createBranch(String repoName, CreateBranchRequest request) {
        try (Repository repository = repositoryService.openRepository(repoName);
             Git git = new Git(repository)) {

            // Check if branch already exists
            List<Ref> existingBranches = git.branchList().call();
            for (Ref ref : existingBranches) {
                if (ref.getName().equals(REFS_HEADS_PREFIX + request.getName())) {
                    throw new RepositoryException("Branch already exists: " + request.getName(),
                            ErrorCode.BRANCH_ALREADY_EXISTS);
                }
            }

            var branchCommand = git.branchCreate()
                    .setName(request.getName());

            if (request.getStartPoint() != null && !request.getStartPoint().isEmpty()) {
                branchCommand.setStartPoint(request.getStartPoint());
            }

            Ref ref = branchCommand.call();

            if (request.isCheckout()) {
                git.checkout().setName(request.getName()).call();
            }

            logger.info("Created branch: {} in repository: {}", request.getName(), repoName);
            return createBranchInfo(repository, ref, repository.getBranch());

        } catch (GitAPIException | IOException e) {
            throw new RepositoryException("Failed to create branch: " + e.getMessage(), e);
        }
    }

    public void deleteBranch(String repoName, String branchName, boolean force) {
        try (Repository repository = repositoryService.openRepository(repoName);
             Git git = new Git(repository)) {

            String currentBranch = repository.getBranch();
            if (currentBranch.equals(branchName)) {
                throw new RepositoryException("Cannot delete the current branch: " + branchName,
                        ErrorCode.INVALID_OPERATION);
            }

            git.branchDelete()
                    .setBranchNames(branchName)
                    .setForce(force)
                    .call();

            logger.info("Deleted branch: {} from repository: {}", branchName, repoName);

        } catch (GitAPIException e) {
            if (e.getMessage().contains("not found")) {
                throw new RepositoryException("Branch not found: " + branchName, ErrorCode.BRANCH_NOT_FOUND);
            }
            throw new RepositoryException("Failed to delete branch: " + e.getMessage(), e);
        } catch (IOException e) {
            throw new RepositoryException("Failed to delete branch: " + e.getMessage(), e);
        }
    }

    public BranchInfo checkoutBranch(String repoName, String branchName) {
        try (Repository repository = repositoryService.openRepository(repoName);
             Git git = new Git(repository)) {

            Ref ref = git.checkout()
                    .setName(branchName)
                    .call();

            logger.info("Checked out branch: {} in repository: {}", branchName, repoName);
            return createBranchInfo(repository, ref, branchName);

        } catch (GitAPIException | IOException e) {
            if (e.getMessage().contains("not found") || e.getMessage().contains("Ref")) {
                throw new RepositoryException("Branch not found: " + branchName, ErrorCode.BRANCH_NOT_FOUND);
            }
            throw new RepositoryException("Failed to checkout branch: " + e.getMessage(), e);
        }
    }

    public MergeResult mergeBranch(String repoName, MergeBranchRequest request) {
        try (Repository repository = repositoryService.openRepository(repoName);
             Git git = new Git(repository)) {

            ObjectId objectId = repository.resolve(request.getSourceBranch());
            if (objectId == null) {
                throw new RepositoryException("Branch not found: " + request.getSourceBranch(),
                        ErrorCode.BRANCH_NOT_FOUND);
            }

            MergeCommand mergeCommand = git.merge()
                    .include(objectId)
                    .setSquash(request.isSquash());

            if (request.getMessage() != null && !request.getMessage().isEmpty()) {
                mergeCommand.setMessage(request.getMessage());
            }

            org.eclipse.jgit.api.MergeResult result = mergeCommand.call();

            MergeResult.MergeStatus status = mapMergeStatus(result.getMergeStatus());
            boolean successful = result.getMergeStatus().isSuccessful();
            String commitId = result.getNewHead() != null ? result.getNewHead().getName() : null;

            logger.info("Merged branch: {} in repository: {} with status: {}",
                    request.getSourceBranch(), repoName, status);

            return new MergeResult(successful, commitId, result.getMergeStatus().toString(), status);

        } catch (GitAPIException | IOException e) {
            throw new RepositoryException("Failed to merge branch: " + e.getMessage(), e);
        }
    }

    private BranchInfo createBranchInfo(Repository repository, Ref ref, String currentBranch) 
            throws IOException {
        String branchName = ref.getName();
        boolean isRemote = branchName.startsWith(REFS_REMOTES_PREFIX);
        
        if (branchName.startsWith(REFS_HEADS_PREFIX)) {
            branchName = branchName.substring(REFS_HEADS_PREFIX.length());
        } else if (isRemote) {
            branchName = branchName.substring(REFS_REMOTES_PREFIX.length());
        }

        ObjectId objectId = ref.getObjectId();
        if (objectId == null) {
            return new BranchInfo(branchName, null, null, null, null, isRemote, 
                    branchName.equals(currentBranch));
        }

        try (RevWalk revWalk = new RevWalk(repository)) {
            RevCommit commit = revWalk.parseCommit(objectId);
            LocalDateTime commitDate = LocalDateTime.ofInstant(
                    Instant.ofEpochSecond(commit.getCommitTime()),
                    ZoneId.systemDefault());

            return new BranchInfo(
                    branchName,
                    objectId.getName(),
                    commit.getShortMessage(),
                    commit.getAuthorIdent().getName(),
                    commitDate,
                    isRemote,
                    branchName.equals(currentBranch)
            );
        }
    }

    private MergeResult.MergeStatus mapMergeStatus(org.eclipse.jgit.api.MergeResult.MergeStatus status) {
        return switch (status) {
            case FAST_FORWARD, FAST_FORWARD_SQUASHED -> MergeResult.MergeStatus.FAST_FORWARD;
            case MERGED, MERGED_SQUASHED, MERGED_NOT_COMMITTED, MERGED_SQUASHED_NOT_COMMITTED -> 
                    MergeResult.MergeStatus.MERGED;
            case ALREADY_UP_TO_DATE -> MergeResult.MergeStatus.ALREADY_UP_TO_DATE;
            case CONFLICTING -> MergeResult.MergeStatus.CONFLICTING;
            case ABORTED -> MergeResult.MergeStatus.ABORTED;
            default -> MergeResult.MergeStatus.FAILED;
        };
    }
}
