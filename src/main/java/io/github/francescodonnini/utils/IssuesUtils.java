package io.github.francescodonnini.utils;

import io.github.francescodonnini.jira.JiraRestApi;
import io.github.francescodonnini.json.issue.Issue;

import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

public class IssuesUtils {
    private IssuesUtils() {}
    
    public static List<Issue> getAllIssues(String jql) throws URISyntaxException {
        var jiraApi = new JiraRestApi();
        var issues = new ArrayList<Issue>();
        var i = 0;
        var result = jiraApi.getIssues(jql);
        while (i < result.getTotal()) {
            issues.addAll(result.getIssues());
            i += result.getIssues().size();
            result = jiraApi.getIssues(jql, i);
        }
        return issues;
    }
}
