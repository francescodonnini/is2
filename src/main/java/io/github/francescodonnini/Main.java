package io.github.francescodonnini;

import io.github.francescodonnini.json.issue.Issue;
import io.github.francescodonnini.json.issue.Issues;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.concurrent.ExecutionException;

public class Main {
    public static void main(String[] args) throws URISyntaxException, IOException, ExecutionException, InterruptedException {
        var jiraApi = new JiraRestApi();
        var issues = new ArrayList<Issue>();
        var i = 0;
        var result = jiraApi.get(JiraEndpoints.Search(String.format("project='SYNCOPE'%%20&%%20startAt=%d", i)), Issues.class);
        while (i < result.getTotal()) {
            issues.addAll(result.getIssues());
            i += result.getIssues().size();
            result = jiraApi.get(JiraEndpoints.Search(String.format("project='SYNCOPE'%%20&%%20startAt=%d", i)), Issues.class);
        }
        for (var issue : issues) {
            System.out.println(issue.getId());
        }
    }
}