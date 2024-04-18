package io.github.francescodonnini;

import io.github.francescodonnini.git.GitLog;
import io.github.francescodonnini.jira.JiraApi;
import io.github.francescodonnini.jira.JiraRestApi;
import io.github.francescodonnini.json.issue.Issue;
import io.github.francescodonnini.json.version.Version;
import io.github.francescodonnini.json.version.VersionList;
import org.eclipse.jgit.revwalk.RevCommit;

import java.io.IOException;
import java.net.URISyntaxException;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class Main {
    public static void main(String[] args) throws URISyntaxException, IOException, ExecutionException, InterruptedException {
        var avIssues = getAllIssues("project='SYNCOPE' AND type=bug AND (status=closed OR status=fixed) AND resolution=fixed".replace(" ", "%20"));
    }

    private static List<Issue> getAllIssues(String jql) {
        var restApi = new JiraRestApi();
        var jiraApi = new JiraApi(restApi);
        var issues = new ArrayList<Issue>();
        var i = 0;
        var opt = jiraApi.getIssues(jql);
        if (opt.isEmpty()) {
            return List.of();
        }
        var result = opt.get();
        while (i < result.getTotal()) {
            issues.addAll(result.getIssues());
            i += result.getIssues().size();
            opt = jiraApi.getIssues("project='SYNCOPE'", i);
            if (opt.isEmpty()) {
                break;
            }
            result = opt.get();
        }
        return issues;
    }
}