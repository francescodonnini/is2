package io.github.francescodonnini.proportion;

import io.github.francescodonnini.model.Issue;
import io.github.francescodonnini.model.Version;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.IntStream;

public class Incremental implements Proportion {
    private final List<Issue> issues;
    private final List<Version> releases;

    public Incremental(List<Issue> issues, List<Version> releases) {
        this.issues = issues;
        this.releases = releases;
    }

    @Override
    public List<Issue> fillOut() {
        return List.of();
    }
}
