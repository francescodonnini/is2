package io.github.francescodonnini.csv;

import com.opencsv.bean.CsvBindByName;
import com.opencsv.bean.CsvDate;

import java.time.LocalDate;

public class VersionLocalEntity {
    @CsvBindByName(column = "Archived", required = true)
    private boolean archived;
    @CsvBindByName(column = "Id", required = true)
    private String id;
    @CsvBindByName(column = "Name", required = true)
    private String name;
    @CsvBindByName(column = "Released", required = true)
    private boolean released;
    @CsvDate("yyyy-MM-dd")
    @CsvBindByName(column = "Release Date")
    private LocalDate releaseDate;

    public boolean isArchived() {
        return archived;
    }

    public void setArchived(boolean archived) {
        this.archived = archived;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public LocalDate getReleaseDate() {
        return releaseDate;
    }

    public void setReleaseDate(LocalDate releaseDate) {
        this.releaseDate = releaseDate;
    }

    public boolean isReleased() {
        return released;
    }

    public void setReleased(boolean released) {
        this.released = released;
    }
}
