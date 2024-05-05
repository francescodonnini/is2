package io.github.francescodonnini.json;

import io.github.francescodonnini.jira.JiraRestApi;
import io.github.francescodonnini.json.version.VersionNetworkEntity;
import io.github.francescodonnini.model.Version;

import java.net.URISyntaxException;
import java.util.List;

public class JsonVersionApi {
    private final String projectName;
    private final JiraRestApi restApi;

    public JsonVersionApi(String projectName, JiraRestApi restApi) {
        this.projectName = projectName;
        this.restApi = restApi;
    }

    public List<Version> getRemoteVersions() {
        try {
            return restApi.getReleaseInfo(projectName).getVersions().stream()
                    .map(JsonVersionApi::fromVersionNetworkEntity)
                    .toList();
        } catch (URISyntaxException e) {
            return List.of();
        }
    }

    private static Version fromVersionNetworkEntity(VersionNetworkEntity v) {
        return new Version(v.getArchived(),
                v.getId(),
                v.getName(),
                v.getReleased(),
                v.getReleaseDate());
    }
}
