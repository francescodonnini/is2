package io.github.francescodonnini.jira;

import io.github.francescodonnini.json.issue.Issues;
import io.github.francescodonnini.json.version.VersionList;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutionException;

public class JiraApi {
    private final JiraRestApi restApi;

    public JiraApi(JiraRestApi restApi) {
        this.restApi = restApi;
    }

    public Issues getIssues(String gql) throws URISyntaxException {
        return getIssues(gql, List.of(), 0, 1000, List.of());
    }

    public Issues getIssues(String gql, int startAt) throws URISyntaxException {
        return getIssues(gql, List.of(), startAt, 50, List.of());
    }
    public Issues getIssues(String jql, List<String> fields, int startAt, int maxResults, List<String> properties) throws URISyntaxException {
        var request = new StringBuilder()
                .append(JiraEndpoints.Search(jql))
                .append(String.format("&startAt=%d", startAt))
                .append(String.format("&maxResults=%d", maxResults));
        if (!fields.isEmpty()) {
            request.append("&fields=").append(String.join(",", fields));
        }
        if (!properties.isEmpty()) {
            request.append("&properties=").append(String.join(",", properties));
        }
        return restApi.get(request.toString(), Issues.class);
    }

    public VersionList getReleaseInfo(String projectName) throws URISyntaxException {
        return restApi.get("https://issues.apache.org/jira/rest/api/2/project/" + projectName, VersionList.class);
    }
}
