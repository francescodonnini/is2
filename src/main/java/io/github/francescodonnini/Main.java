package io.github.francescodonnini;

import io.github.francescodonnini.json.issue.Issue;
import io.github.francescodonnini.json.issue.Issues;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

public class Main {
    public static void main(String[] args) throws URISyntaxException, IOException, ExecutionException, InterruptedException {
        var restApi = new JiraRestApi();
        var jiraApi = new JiraApi(restApi);
        var issues = new ArrayList<Issue>();
        var i = 0;
        var opt = jiraApi.getIssues("project='SYNCOPE'");
        if (opt.isEmpty()) {
            return;
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
        for (var issue : issues) {
            System.out.println(issue.getId());
        }
    }
}