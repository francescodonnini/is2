package io.github.francescodonnini.api;

import io.github.francescodonnini.csv.CsvReleaseApi;
import io.github.francescodonnini.json.JsonReleaseApi;
import io.github.francescodonnini.model.Release;

import java.io.FileNotFoundException;
import java.util.List;

public class ReleaseRepository implements ReleaseApi {
    private final JsonReleaseApi remoteDataSource;
    private final CsvReleaseApi localDataSource;

    public ReleaseRepository(JsonReleaseApi remoteDataSource, CsvReleaseApi localDataSource) {
        this.remoteDataSource = remoteDataSource;
        this.localDataSource = localDataSource;
    }

    @Override
    public List<Release> getReleases() {
        try {
            return localDataSource.getLocal();
        } catch (FileNotFoundException e) {
            return remoteDataSource.getReleases();
        }
    }
}
