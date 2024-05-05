package io.github.francescodonnini.csv;

import com.opencsv.bean.CsvToBeanBuilder;
import com.opencsv.bean.StatefulBeanToCsvBuilder;
import com.opencsv.exceptions.CsvDataTypeMismatchException;
import com.opencsv.exceptions.CsvRequiredFieldEmptyException;
import io.github.francescodonnini.utils.FileUtils;
import io.github.francescodonnini.api.ReleaseApi;
import io.github.francescodonnini.model.Release;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class CsvReleaseApi extends CsvAbstractApi<ReleaseLocalEntity, Release> implements ReleaseApi {
    private final Logger logger = Logger.getLogger(CsvReleaseApi.class.getName());
    private final ReleaseApi releaseApi;

    public CsvReleaseApi(String path, ReleaseApi releaseApi) {
        super(path);
        this.releaseApi = releaseApi;
    }

    @Override
    public List<Release> getReleases() {
        try {
            var beans = new CsvToBeanBuilder<ReleaseLocalEntity>(new FileReader(path))
                    .withType(ReleaseLocalEntity.class)
                    .build()
                    .parse();
            return beans.stream().map(this::fromCsv).toList();
        } catch (FileNotFoundException e) {
            var releases = releaseApi.getReleases();
            fetchAndSave(releases);
            return releases;
        }
    }

    private void fetchAndSave(List<Release> releases) {
        var beans = releases.stream().map(this::toCsv).toList();
        FileUtils.createFileIfNotExists(path);
        try (var writer = new FileWriter(path)) {
            var beanToCsv = new StatefulBeanToCsvBuilder<ReleaseLocalEntity>(writer).build();
            for (var b : beans) {
                beanToCsv.write(b);
            }
        } catch (IOException | CsvDataTypeMismatchException | CsvRequiredFieldEmptyException e) {
            logger.log(Level.SEVERE, e.getMessage());
        }
    }

    @Override
    protected ReleaseLocalEntity toCsv(Release model) {
        var bean = new ReleaseLocalEntity();
        bean.setReleaseNumber(model.releaseNumber());
        bean.setName(model.name());
        bean.setId(model.id());
        bean.setReleaseDate(model.releaseDate());
        return bean;
    }

    @Override
    protected Release fromCsv(ReleaseLocalEntity bean) {
        return new Release(bean.getReleaseNumber(), bean.getId(), bean.getName(), bean.getReleaseDate());
    }
}
