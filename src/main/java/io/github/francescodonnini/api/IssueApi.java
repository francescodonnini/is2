package io.github.francescodonnini.api;

import io.github.francescodonnini.model.Issue;

import java.util.List;

public interface IssueApi {
    List<Issue> getIssues();
    String getProjectName();
}
