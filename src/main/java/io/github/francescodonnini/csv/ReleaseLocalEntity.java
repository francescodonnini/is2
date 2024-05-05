package io.github.francescodonnini.csv;

import com.opencsv.bean.CsvBindByName;
import com.opencsv.bean.CsvDate;

import java.time.LocalDate;

public class ReleaseLocalEntity {
    @CsvBindByName(column = "Release Number", required = true)
    private int releaseNumber;
    @CsvBindByName(column = "Id", required = true)
    private String id;
    @CsvBindByName(column = "Name", required = true)
    private String name;
    @CsvDate("dd/MM/yyyy")
    @CsvBindByName(column = "Release Date", required = true)
    private LocalDate releaseDate;

    public int getReleaseNumber() {
        return releaseNumber;
    }

    public void setReleaseNumber(int releaseNumber) {
        this.releaseNumber = releaseNumber;
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
}
