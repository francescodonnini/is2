package io.github.francescodonnini.utils;

import io.github.francescodonnini.jira.JiraRestApi;
import io.github.francescodonnini.json.issue.IssueNetworkEntity;

import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

public class IssuesUtils {
    private IssuesUtils() {}

    public static List<IssueNetworkEntity> getAllIssues(String jql) throws URISyntaxException {
        var jiraApi = new JiraRestApi();
        var issues = new ArrayList<IssueNetworkEntity>();
        var i = 0;
        var result = jiraApi.getIssues(jql);
        while (i < result.getTotal()) {
            issues.addAll(result.getIssueList());
            i += result.getIssueList().size();
            result = jiraApi.getIssues(jql, i);
        }
        return issues;
    }
}
