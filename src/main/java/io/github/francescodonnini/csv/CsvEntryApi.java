package io.github.francescodonnini.csv;

import com.opencsv.bean.CsvToBeanBuilder;
import com.opencsv.bean.StatefulBeanToCsvBuilder;
import com.opencsv.exceptions.CsvDataTypeMismatchException;
import com.opencsv.exceptions.CsvRequiredFieldEmptyException;
import io.github.francescodonnini.api.ReleaseApi;
import io.github.francescodonnini.csv.entities.EntryLocalEntity;
import io.github.francescodonnini.model.Entry;
import io.github.francescodonnini.model.Release;
import io.github.francescodonnini.utils.FileUtils;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.Optional;

public class CsvEntryApi {
    private final String defaultPath;
    private final List<Release> releases;

    public CsvEntryApi(String defaultPath, ReleaseApi releaseApi) {
        this.defaultPath = defaultPath;
        this.releases = releaseApi.getReleases();
    }

    public List<Entry> getLocal() throws FileNotFoundException {
        return getEntries(defaultPath);
    }

    public List<Entry> getEntries(String path) throws FileNotFoundException {
        var beans = new CsvToBeanBuilder<EntryLocalEntity>(new FileReader(path))
                .withType(EntryLocalEntity.class)
                .build()
                .parse();
        return beans.stream().map(this::fromCsv).filter(Optional::isPresent).map(Optional::get).toList();
    }

    private Optional<Entry> fromCsv(EntryLocalEntity e) {
        var release = releases.stream().filter(r -> r.releaseNumber() == e.getReleaseNumber()).findFirst();
        return release.map(value -> new Entry(e.isBuggy(), e.getPath(), value));
    }

    public void saveLocal(List<Entry> entries) throws CsvRequiredFieldEmptyException, CsvDataTypeMismatchException, IOException {
        save(entries, defaultPath);
    }

    public void saveLocal(List<Entry> entries, String path) throws CsvRequiredFieldEmptyException, CsvDataTypeMismatchException, IOException {
        save(entries, path);
    }

    private void save(List<Entry> entries, String path) throws IOException, CsvRequiredFieldEmptyException, CsvDataTypeMismatchException {
        var beans = entries.stream().map(this::toCsv).toList();
        FileUtils.createFileIfNotExists(path);
        try (var writer = new FileWriter(path)) {
            var beanToCsv = new StatefulBeanToCsvBuilder<EntryLocalEntity>(writer).build();
            for (var b : beans) {
                beanToCsv.write(b);
            }
        }
    }

    private EntryLocalEntity toCsv(Entry e) {
        var bean = new EntryLocalEntity();
        bean.setBuggy(e.isBuggy());
        bean.setPath(e.getPath());
        bean.setReleaseNumber(e.getRelease().releaseNumber());
        bean.setLocTouched(e.getLoc());
        bean.setAvgLocAdded(e.getAvgLocAdded());
        bean.setLocAdded(e.getLocAdded());
        bean.setLocDeleted(e.getLocDeleted());
        bean.setAvgLocDeleted(e.getLocDeleted());
        bean.setNumOfRevisions(e.getNumOfRevisions());
        bean.setAuthors(e.getAuthors());
        bean.setChangeSetSize(e.getChangeSetSize());
        bean.setChurn(e.getChurn());
        bean.setAvgChangeSetSize(e.getAvgChangeSetSize());
        bean.setMaxChangeSetSize(e.getMaxChangeSetSize());
        bean.setNumOfMethods(e.getNumOfMethods());
        bean.setAvgLocOfMethods(e.getAverageLocOfMethods());
        return bean;
    }
}
