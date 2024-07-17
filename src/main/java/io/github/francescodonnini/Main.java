package io.github.francescodonnini;

import com.opencsv.exceptions.CsvDataTypeMismatchException;
import com.opencsv.exceptions.CsvRequiredFieldEmptyException;
import io.github.francescodonnini.api.*;
import io.github.francescodonnini.config.IniSettings;
import io.github.francescodonnini.csv.CsvEntryApi;
import io.github.francescodonnini.csv.CsvIssueApi;
import io.github.francescodonnini.csv.CsvReleaseApi;
import io.github.francescodonnini.csv.CsvVersionApi;
import io.github.francescodonnini.git.GitLog;
import io.github.francescodonnini.jira.JiraRestApi;
import io.github.francescodonnini.json.JsonIssueApi;
import io.github.francescodonnini.json.JsonReleaseApi;
import io.github.francescodonnini.json.JsonVersionApi;
import io.github.francescodonnini.metrics.CalculatorImpl;
import io.github.francescodonnini.proportion.Incremental;
import io.github.francescodonnini.utils.AssignBugginess;
import io.github.francescodonnini.utils.CreateEntries;
import org.apache.commons.configuration2.ex.ConfigurationException;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;

import java.io.File;
import java.io.IOException;

public class Main {
    public static void main(String[] args) throws IOException, ConfigurationException, GitAPIException, CsvRequiredFieldEmptyException, CsvDataTypeMismatchException {
        if (args.length != 1) {
            System.exit(1);
        }
        var projectName = "SYNCOPE";
        var pattern = "%s-\\d+".formatted(projectName);
        var settings = new IniSettings(args[0]);
        var restApi = new JiraRestApi();
        var repositoryPath = settings.getString("gitBasePath").formatted(projectName.toLowerCase());
        var projectPath = settings.getString("projectBasePath").formatted(projectName.toLowerCase());
        var git = new GitLog(repositoryPath);
        var path = settings.getString("dataPath").formatted(projectName);
        var remoteVersionApi = new JsonVersionApi(projectName, restApi);
        var localVersionApi = new CsvVersionApi(path + "/versions.csv");
        var versionApi = new VersionRepository(remoteVersionApi, localVersionApi);
        var remoteReleaseApi = new JsonReleaseApi(versionApi);
        var localReleaseApi = new CsvReleaseApi(path + "/releases.csv");
        var releaseApi = new ReleaseRepository(remoteReleaseApi, localReleaseApi);
        var localIssueApi = new CsvIssueApi(path + "/issues.csv", releaseApi.getReleases(), git.getAll());
        var remoteIssueApi = new JsonIssueApi(projectName, pattern, git, restApi, releaseApi);
        var issueApi = new IssueRepository(remoteIssueApi, localIssueApi);
        var issues = issueApi.getIssues();
        var releases = releaseApi.getReleases();
        releases = releases.subList(0, releases.size() / 2);
        var proportion = new Incremental(issues, releases);
        issues = proportion.fillOut();
        // localIssueApi.saveLocal(issues, path + "/labelled_issues.csv");
        var localEntriesApi = new CsvEntryApi(path + "/entries.csv", releaseApi);
        var entryUtils = new CreateEntries(projectPath, releases);
        var entries = entryUtils.getEntries();
        var repository = new FileRepositoryBuilder()
                .setGitDir(new File(repositoryPath))
                .build();
        var g = new Git(repository);
        var calculator = new CalculatorImpl(releases.subList(0, releases.size() / 2), g);
        entries = calculator.calculate(entries);
        var assignBugginess = new AssignBugginess(entries, issues);
        assignBugginess.setRepository(repositoryPath);
        var trainingSetPath = "%s/tr/%d.csv";
        for (var release : releases.subList(1, releases.size())) {
            assignBugginess.setEnd(release);
            var data = assignBugginess.fill();
            localEntriesApi.saveLocal(data, trainingSetPath.formatted(path, release.releaseNumber()));
        }
    }
}
