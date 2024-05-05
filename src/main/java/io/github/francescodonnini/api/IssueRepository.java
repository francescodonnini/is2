package io.github.francescodonnini.api;

import io.github.francescodonnini.csv.CsvIssueApi;
import io.github.francescodonnini.json.JsonIssueApi;
import io.github.francescodonnini.model.Issue;

import java.io.FileNotFoundException;
import java.util.List;

public class IssueRepository implements IssueApi {
    private final JsonIssueApi remoteDataSource;
    private final CsvIssueApi localDataSource;

    public IssueRepository(JsonIssueApi remoteDataSource, CsvIssueApi localDataSource) {
        this.remoteDataSource = remoteDataSource;
        this.localDataSource = localDataSource;
    }

    @Override
    public List<Issue> getIssues() {
        try {
            return localDataSource.getLocal();
        } catch (FileNotFoundException e) {
            return remoteDataSource.getRemoteIssues();
        }
    }
}
