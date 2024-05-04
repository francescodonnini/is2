package io.github.francescodonnini.api;

import com.opencsv.CSVReader;
import com.opencsv.CSVWriter;
import com.opencsv.exceptions.CsvValidationException;
import io.github.francescodonnini.model.Release;
import io.github.francescodonnini.model.Version;

import java.io.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class CsvVersionApi implements VersionApi {
    private static final int VERSION_ARCHIVED_FIELD = 0;
    private static final int VERSION_ID_FIELD = 1;
    private static final int VERSION_NAME_FIELD = 2;
    private static final int VERSION_RELEASED_FIELD = 3;
    private static final int VERSION_RELEASE_DATE_FIELD = 4;
    private static final String[] VERSION_HEADER = new String[]{
            "Archived",
            "Id",
            "Name",
            "Released",
            "Release Date"
    };

    private static final int RELEASE_NUMBER_FIELD = 0;
    private static final int RELEASE_ID_FIELD = 1;
    private static final int RELEASE_NAME_FIELD = 2;
    private static final int RELEASE_DATE_FIELD = 3;
    private static final String[] RELEASE_HEADER = {
            "Release Number",
            "Id",
            "Name",
            "Release Date"
    };
    private final Logger logger;
    private final VersionApi versionApi;
    private final String path;

    public CsvVersionApi(String path, VersionApi versionApi) {
        this.path = path;
        this.versionApi = versionApi;
        logger = Logger.getLogger(CsvVersionApi.class.getName());
    }

    @Override
    public List<Version> getVersions(String projectName) {
        var versionPath = "%s/%s/versions.csv".formatted(path, projectName);
        var versions = new ArrayList<Version>();
        try (var r = new CSVReader(new FileReader(versionPath))) {
            versions.addAll(readVersionsFromCsv(r));
        } catch (IOException | CsvValidationException e) {
            logger.log(Level.INFO, e.getMessage());
            versions.addAll(versionApi.getVersions(projectName));
            versionsToCsv(versions, versionPath);
        }
        return versions;
    }

    private List<Version> readVersionsFromCsv(CSVReader reader) throws IOException, CsvValidationException {
        reader.readNext();
        var versions = new ArrayList<Version>();
        String[] line;
        while ((line = reader.readNext()) != null) {
            var archived = Boolean.parseBoolean(line[VERSION_ARCHIVED_FIELD]);
            var id = line[VERSION_ID_FIELD];
            var name = line[VERSION_NAME_FIELD];
            var released = Boolean.parseBoolean(line[VERSION_RELEASED_FIELD]);
            var releaseDate = LocalDate.parse(line[VERSION_RELEASE_DATE_FIELD], DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            versions.add(new Version(archived, id, name, released, releaseDate));
        }
        return versions;
    }

    private void versionsToCsv(List<Version> versions, String path) {
        try {
            var file = new File(path);
            if (file.getParentFile().mkdirs()) {
                var message = "directory %s has been created.".formatted(file.getAbsolutePath());
                logger.log(Level.INFO, message);
            }
            var w = new CSVWriter(new FileWriter(file));
            w.writeNext(VERSION_HEADER);
            for (var v : versions) {
                w.writeNext(new String[]{
                        String.valueOf(v.archived()),
                        v.id(),
                        v.name(),
                        String.valueOf(v.released()),
                        v.releaseDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
                });
            }
            w.close();
        } catch (IOException e) {
            logger.log(Level.SEVERE, e.getMessage());
        }
    }

    @Override
    public List<Release> getReleases(String projectName) {
        var releasePath = "%s/%s/releases.csv".formatted(path, projectName);
        var releases = new ArrayList<Release>();
        try (var r = new CSVReader(new FileReader(releasePath))) {
            releases.addAll(readReleasesFromCsv(r));
        } catch (IOException | CsvValidationException e) {
            logger.log(Level.INFO, e.getMessage());
            releases.addAll(versionApi.getReleases(projectName));
            releasesToCsv(versionApi.getReleases(projectName), releasePath);
        }
        return releases;
    }

    private List<Release> readReleasesFromCsv(CSVReader reader) throws IOException, CsvValidationException {
        reader.readNext();
        var releases = new ArrayList<Release>();
        String[] line;
        while ((line = reader.readNext()) != null) {
            var number = Integer.parseInt(line[RELEASE_NUMBER_FIELD]);
            var id = line[RELEASE_ID_FIELD];
            var name = line[RELEASE_NAME_FIELD];
            var releaseDate = LocalDate.parse(line[RELEASE_DATE_FIELD], DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            releases.add(new Release(number, id, name, releaseDate));
        }
        return releases;
    }

    private void releasesToCsv(List<Release> releases, String path) {
        try {
            var file = new File(path);
            if (file.getParentFile().mkdirs()) {
                var message = "directory %s has been created.".formatted(file.getAbsolutePath());
                logger.log(Level.INFO, message);
            }
            var w = new CSVWriter(new FileWriter(file));
            w.writeNext(RELEASE_HEADER);
            for (var r : releases) {
                w.writeNext(new String[]{
                        String.valueOf(r.releaseNumber()),
                        String.valueOf(r.id()),
                        r.name(),
                        r.releaseDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
                });
            }
            w.close();
        } catch (IOException e) {
            logger.log(Level.SEVERE, e.getMessage());
        }
    }
}
