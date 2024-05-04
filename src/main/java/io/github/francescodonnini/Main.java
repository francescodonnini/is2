package io.github.francescodonnini;

import io.github.francescodonnini.api.*;
import io.github.francescodonnini.jira.JiraRestApi;
import io.github.francescodonnini.model.Version;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Main {
    private static final String DATA_PATH = "/home/francesco/.is2";
    private static final String PROJECT = "OPENJPA";
    private static final String GIT_PATH = "/home/francesco/Documents/%s/.git";
    private static final String PATTERN = "%s-\\d+".formatted(PROJECT);

    public static void main(String[] args) throws IOException {
        var logger = Logger.getLogger(Main.class.getName());
        var restApi = new JiraRestApi();
        var versionApi = new VersionApiImpl(restApi);
        IssueApi issueApi = new IssueApiImpl(restApi, versionApi, GIT_PATH.formatted(PROJECT.toLowerCase()));
        issueApi = new CsvIssueApi(DATA_PATH, issueApi, versionApi);
        var issues = issueApi.getIssues(PROJECT, PATTERN);
    }
}
