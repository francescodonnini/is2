package io.github.francescodonnini;

import com.opencsv.exceptions.CsvDataTypeMismatchException;
import com.opencsv.exceptions.CsvRequiredFieldEmptyException;
import io.github.francescodonnini.api.*;
import io.github.francescodonnini.config.IniSettings;
import io.github.francescodonnini.csv.CsvIssueApi;
import io.github.francescodonnini.csv.CsvReleaseApi;
import io.github.francescodonnini.csv.CsvVersionApi;
import io.github.francescodonnini.git.GitLog;
import io.github.francescodonnini.jira.JiraRestApi;
import io.github.francescodonnini.json.JsonIssueApi;
import io.github.francescodonnini.json.JsonReleaseApi;
import io.github.francescodonnini.json.JsonVersionApi;
import io.github.francescodonnini.proportion.Incremental;
import org.apache.commons.configuration2.ex.ConfigurationException;
import org.eclipse.jgit.api.errors.GitAPIException;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Main {
    private static final Logger logger = Logger.getLogger(Main.class.getName());

    public static void main(String[] args) throws IOException, ConfigurationException, GitAPIException, CsvRequiredFieldEmptyException, CsvDataTypeMismatchException {
        if (args.length != 1) {
            logger.log(Level.SEVERE, "Expected one argument, but got %d%n".formatted(args.length));
            System.exit(1);
        }
        var projectName = "SYNCOPE";
        var pattern = "%s-\\d+".formatted(projectName);
        var settings = new IniSettings(args[0]);
        var restApi = new JiraRestApi();
        var git = new GitLog(settings.getString("gitBasePath").formatted(projectName.toLowerCase()));
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
        issueApi.getIssues();
        var proportion = new Incremental(issueApi, releaseApi);
        var issues = proportion.fillOut();
        localIssueApi.saveLocal(issues, path + "/labelled_issues.csv");
    }
}
