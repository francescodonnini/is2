package io.github.francescodonnini;

import io.github.francescodonnini.api.*;
import io.github.francescodonnini.csv.CsvIssueApi;
import io.github.francescodonnini.csv.CsvReleaseApi;
import io.github.francescodonnini.csv.CsvVersionApi;
import io.github.francescodonnini.config.IniSettings;
import io.github.francescodonnini.git.GitLog;
import io.github.francescodonnini.jira.JiraRestApi;
import io.github.francescodonnini.json.JsonIssueApi;
import io.github.francescodonnini.json.JsonReleaseApi;
import io.github.francescodonnini.json.JsonVersionApi;
import org.apache.commons.configuration2.ex.ConfigurationException;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.diff.DiffFormatter;
import org.eclipse.jgit.diff.RawTextComparator;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import org.eclipse.jgit.util.io.DisabledOutputStream;

import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Main {
    private static final Logger logger = Logger.getLogger(Main.class.getName());
    private static final String PATTERN = "%s-\\d+";

    public static void main(String[] args) throws IOException, ConfigurationException, GitAPIException {
        if (args.length != 1) {
            logger.log(Level.SEVERE, "Expected one argument, but got %d%n".formatted(args.length));
            System.exit(1);
        }
        var projectName = "AVRO";
        var pattern = "AVRO-\\d+";
        var settings = new IniSettings(args[0]);
        var restApi = new JiraRestApi();
        var git = new GitLog(settings.getString("gitBasePath").formatted(projectName.toLowerCase()));
        var path = settings.getString("dataPath").formatted(projectName);
        VersionApi versionApi = new JsonVersionApi(projectName, restApi);
        versionApi = new CsvVersionApi(path + "/versions.csv", versionApi);
        ReleaseApi releaseApi = new JsonReleaseApi(versionApi);
        releaseApi = new CsvReleaseApi(path + "/releases.csv", releaseApi);
        IssueApi issueApi = new JsonIssueApi(projectName, pattern, git, restApi, releaseApi);
        issueApi = new CsvIssueApi(path + "/issues.csv", git.getAll(), issueApi, releaseApi);
        var gitBasePath = settings.getString("gitBasePath");
        var repository = new FileRepositoryBuilder()
                .setGitDir(new File(gitBasePath.formatted(projectName.toLowerCase())))
                .build();
        var issues = issueApi.getIssues();
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
