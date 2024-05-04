package io.github.francescodonnini.api;

import io.github.francescodonnini.model.Release;
import io.github.francescodonnini.model.Version;

import java.util.List;

public interface VersionApi {
    List<Version> getVersions(String projectName);
    List<Release> getReleases(String projectName);
}
