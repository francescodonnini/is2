package io.github.francescodonnini;

import io.github.francescodonnini.api.*;
import io.github.francescodonnini.config.IniSettings;
import io.github.francescodonnini.jira.JiraRestApi;
import org.apache.commons.configuration2.ex.ConfigurationException;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.diff.DiffFormatter;
import org.eclipse.jgit.diff.RawTextComparator;
import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import org.eclipse.jgit.treewalk.TreeWalk;
import org.eclipse.jgit.util.io.DisabledOutputStream;

import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Main {
    private static final Logger logger = Logger.getLogger(Main.class.getName());
    private static final String PATTERN = "%s-\\d+";

    public static void main(String[] args) throws IOException, ConfigurationException {
        if (args.length != 1) {
            logger.log(Level.SEVERE, "Expected one argument, but got %d%n".formatted(args.length));
            System.exit(1);
        }
        var settings = new IniSettings(args[0]);
        var restApi = new JiraRestApi();
        VersionApi versionApi = new VersionApiImpl(restApi);
        versionApi = new CsvVersionApi(settings.getString("dataPath"), versionApi);
        IssueApi issueApi = new IssueApiImpl(restApi, versionApi, settings.getString("gitBasePath"));
        issueApi = new CsvIssueApi(settings.getString("dataPath"), settings.getString("gitBasePath"), issueApi, versionApi);
        var gitBasePath = settings.getString("gitBasePath");
        var repository = new FileRepositoryBuilder()
                .setGitDir(new File(gitBasePath.formatted("syncope")))
                .build();
        var issues = issueApi.getIssues("SYNCOPE", PATTERN.formatted("SYNCOPE"));
        for (var issue : issues) {
            for (var commit : issue.commits()) {
                var df = new DiffFormatter(DisabledOutputStream.INSTANCE);
                df.setRepository(repository);
                df.setDiffComparator(RawTextComparator.DEFAULT);
                df.setDetectRenames(true);
                var diffs = df.scan(commit.getParent(0).getId(), commit.getTree());
                logger.log(Level.INFO, commit.getId().getName().substring(0, 6));
                for (var diff : diffs) {
                    logger.log(Level.INFO, "%s %s %s".formatted(diff.getChangeType().name(), diff.getNewMode(), diff.getNewPath()));
                }
            }
        }
    }
}
