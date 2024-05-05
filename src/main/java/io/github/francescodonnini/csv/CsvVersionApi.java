package io.github.francescodonnini.csv;

import com.opencsv.bean.CsvToBeanBuilder;
import com.opencsv.bean.StatefulBeanToCsvBuilder;
import com.opencsv.exceptions.CsvDataTypeMismatchException;
import com.opencsv.exceptions.CsvRequiredFieldEmptyException;
import io.github.francescodonnini.utils.FileUtils;
import io.github.francescodonnini.api.VersionApi;
import io.github.francescodonnini.model.Version;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class CsvVersionApi extends CsvAbstractApi<VersionLocalEntity, Version> implements VersionApi {
    private final Logger logger = Logger.getLogger(CsvVersionApi.class.getName());
    private final VersionApi versionApi;

    public CsvVersionApi(String path, VersionApi versionApi) {
        super(path);
        this.versionApi = versionApi;
    }

    @Override
    protected VersionLocalEntity toCsv(Version model) {
        var bean = new VersionLocalEntity();
        bean.setArchived(model.archived());
        bean.setId(model.id());
        bean.setName(model.name());
        bean.setReleased(model.released());
        bean.setReleaseDate(model.releaseDate());
        return bean;
    }

    @Override
    protected Version fromCsv(VersionLocalEntity bean) {
        return new Version(bean.isArchived(), bean.getId(), bean.getName(), bean.isReleased(), bean.getReleaseDate());
    }

    @Override
    public List<Version> getVersions() {
        try {
            var beans = new CsvToBeanBuilder<VersionLocalEntity>(new FileReader(path))
                .withType(VersionLocalEntity.class)
                .build()
                .parse();
            return beans.stream().map(this::fromCsv).toList();
        } catch (FileNotFoundException e) {
            var versions = versionApi.getVersions();
            fetchAndSave(versions);
            return versions;
        }
    }

    private void fetchAndSave(List<Version> versions) {
        var beans = versions.stream().map(this::toCsv).toList();
        FileUtils.createFileIfNotExists(path);
        try (var writer = new FileWriter(path)) {
            var beanToCsv = new StatefulBeanToCsvBuilder<VersionLocalEntity>(writer).build();
            for (var b : beans) {
                beanToCsv.write(b);
            }
        } catch (IOException | CsvDataTypeMismatchException | CsvRequiredFieldEmptyException e) {
            logger.log(Level.SEVERE, e.getMessage());
        }
    }

    @Override
    public String getProjectName() {
        return versionApi.getProjectName();
    }
}
