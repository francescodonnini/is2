package io.github.francescodonnini.api;

import io.github.francescodonnini.model.Release;

import java.util.List;

public interface ReleaseApi {
    List<Release> getReleases();
}
