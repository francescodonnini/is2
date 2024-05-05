package io.github.francescodonnini.api;

import io.github.francescodonnini.csv.CsvVersionApi;
import io.github.francescodonnini.json.JsonVersionApi;
import io.github.francescodonnini.model.Version;

import java.io.FileNotFoundException;
import java.util.List;

public class VersionRepository implements VersionApi {
    private final JsonVersionApi remoteDataSource;
    private final CsvVersionApi localDataSource;

    public VersionRepository(JsonVersionApi remoteDataSource, CsvVersionApi localDataSource) {
        this.remoteDataSource = remoteDataSource;
        this.localDataSource = localDataSource;
    }

    @Override
    public List<Version> getVersions() {
        try {
            return localDataSource.getLocal();
        } catch (FileNotFoundException e) {
            return remoteDataSource.getRemoteVersions();
        }
    }
}
