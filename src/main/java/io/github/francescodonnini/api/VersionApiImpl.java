package io.github.francescodonnini.api;

import io.github.francescodonnini.jira.JiraRestApi;
import io.github.francescodonnini.json.version.VersionNetworkEntity;
import io.github.francescodonnini.model.Release;
import io.github.francescodonnini.model.Version;

import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class VersionApiImpl implements VersionApi {
    private final JiraRestApi restApi;

    public VersionApiImpl(JiraRestApi restApi) {
        this.restApi = restApi;
    }

    @Override
    public List<Version> getVersions(String projectName) {
        try {
            return restApi.getReleaseInfo(projectName).getVersions().stream()
                    .map(VersionApiImpl::fromVersionNetworkEntity)
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

    @Override
    public List<Release> getReleases(String projectName) {
        var versions = getVersions(projectName).stream().filter(Version::released).filter(v -> v.releaseDate() != null).sorted(Comparator.comparing(Version::releaseDate)).toList();
        var releases = new ArrayList<Release>();
        for (int i = 0; i < versions.size(); i++) {
            var v = versions.get(i);
            releases.add(new Release(i, v.id(), v.name(), v.releaseDate()));
        }
        return releases;
    }
}
