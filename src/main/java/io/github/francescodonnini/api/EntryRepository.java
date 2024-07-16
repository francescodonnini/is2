package io.github.francescodonnini.api;

import com.opencsv.exceptions.CsvDataTypeMismatchException;
import com.opencsv.exceptions.CsvRequiredFieldEmptyException;
import io.github.francescodonnini.csv.CsvEntryApi;
import io.github.francescodonnini.model.Entry;
import io.github.francescodonnini.utils.CreateEntries;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class EntryRepository implements EntryApi {
    private final Logger logger = Logger.getLogger(EntryRepository.class.getName());
    private final CsvEntryApi localSource;
    private final CreateEntries assignBuggyness;

    public EntryRepository(CreateEntries createEntries, CsvEntryApi localSource) throws IOException {
        this.localSource = localSource;
        this.assignBuggyness = createEntries;
    }

    @Override
    public List<Entry> getEntries() {
        try {
            return localSource.getLocal();
        } catch (FileNotFoundException e) {
            var entries = assignBuggyness.getEntries();
            saveLocal(entries);
            return entries;
        }
    }

    private void saveLocal(List<Entry> entries) {
        try {
            localSource.saveLocal(entries);
        } catch (CsvRequiredFieldEmptyException | CsvDataTypeMismatchException | IOException e) {
            logger.log(Level.INFO, e.getMessage());
        }
    }
}
