package io.github.francescodonnini;

import io.github.francescodonnini.json.issue.Issues;
import io.github.francescodonnini.json.version.Version;
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

    public Optional<Issues> getIssues(String gql) {
        return getIssues(gql, List.of(), 0, 50, List.of());
    }

    public Optional<Issues> getIssues(String gql, int startAt) {
        return getIssues(gql, List.of(), startAt, 50, List.of());
    }
    public Optional<Issues> getIssues(String gql, List<String> fields, int startAt, int maxResults, List<String> properties) {
        var request = new StringBuilder()
                .append(JiraEndpoints.Search(gql))
                .append(String.format("&startAt=%d", startAt))
                .append(String.format("&maxResults=%d", maxResults));
        if (!fields.isEmpty()) {
            request.append("&fields=").append(String.join(",", fields));
        }
        if (!properties.isEmpty()) {
            request.append("&properties=").append(String.join(",", properties));
        }
        try {
            return Optional.ofNullable(restApi.get(request.toString(), Issues.class));
        } catch (URISyntaxException | IOException | ExecutionException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    public Optional<VersionList> getReleaseInfo(String projectName) {
        try {
            return Optional.ofNullable(restApi.get("https://issues.apache.org/jira/rest/api/2/project/" + projectName, VersionList.class));
        } catch (URISyntaxException | IOException | InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }
    }
}
