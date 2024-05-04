package io.github.francescodonnini.model;

import java.time.LocalDateTime;
import java.util.List;

public record Issue(
        List<Release> affectedVersions,
        LocalDateTime created,
        Release fixVersion,
        Release openingVersion,
        String key,
        String project) {
    public Release getInjectedVersion() {
        return affectedVersions.getFirst();
    }
}
