package io.github.francescodonnini.git;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

public class GitLog {
    private final Git git;

    public GitLog(String path) throws IOException {
        var repository = new FileRepositoryBuilder()
                .setGitDir(new File(path))
                .build();
        git = new Git(repository);
    }

    public List<RevCommit> getAll() throws GitAPIException {
        var commits = new ArrayList<RevCommit>();
        git.log().call().forEach(commits::add);
        return commits;
    }


}
