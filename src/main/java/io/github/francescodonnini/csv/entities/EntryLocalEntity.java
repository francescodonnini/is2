package io.github.francescodonnini.csv.entities;

import com.opencsv.bean.CsvBindAndSplitByName;
import com.opencsv.bean.CsvBindByName;

import java.util.List;

public class EntryLocalEntity {
    @CsvBindByName(column = "Path", required = true)
    private String path;
    @CsvBindByName(column = "Release", required = true)
    private int releaseNumber;
    @CsvBindByName(column = "Buggy", required = true)
    private boolean buggy;
    @CsvBindByName(column = "LOC Touched")
    private int locTouched;
    @CsvBindByName(column = "Churn")
    private int churn;
    @CsvBindByName(column = "LOC Added")
    private int locAdded;
    @CsvBindByName(column = "Avg LOC Added")
    private int avgLocAdded;
    @CsvBindByName(column = "LOC Deleted")
    private int locDeleted;
    @CsvBindByName(column = "Avg LOC Deleted")
    private int avgLocDeleted;
    @CsvBindAndSplitByName(column = "Authors", elementType = String.class, splitOn = ",", writeDelimiter = ",", collectionType = List.class)
    private List<String> authors;
    @CsvBindByName(column = "NR")
    private int numOfRevisions;
    @CsvBindByName(column = "Change Set Size")
    private int changeSetSize;
    @CsvBindByName(column = "Avg Change Set Size")
    private int avgChangeSetSize;
    @CsvBindByName(column = "Max Change Set Size")
    private int maxChangeSetSize;
    @CsvBindByName(column = "Number of methods")
    private int numOfMethods;
    @CsvBindByName(column = "Avg LOC of methods")
    private long avgLocOfMethods;

    public boolean isBuggy() {
        return buggy;
    }

    public void setBuggy(boolean buggy) {
        this.buggy = buggy;
    }

    public int getReleaseNumber() {
        return releaseNumber;
    }

    public void setReleaseNumber(int releaseNumber) {
        this.releaseNumber = releaseNumber;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public int getLocTouched() {
        return locTouched;
    }

    public void setLocTouched(int locTouched) {
        this.locTouched = locTouched;
    }

    public int getLocAdded() {
        return locAdded;
    }

    public void setLocAdded(int locAdded) {
        this.locAdded = locAdded;
    }

    public int getLocDeleted() {
        return locDeleted;
    }

    public void setLocDeleted(int locDeleted) {
        this.locDeleted = locDeleted;
    }

    public List<String> getAuthors() {
        return authors;
    }

    public void setAuthors(List<String> authors) {
        this.authors = authors;
    }

    public int getNumOfRevisions() {
        return numOfRevisions;
    }

    public void setNumOfRevisions(int numOfRevisions) {
        this.numOfRevisions = numOfRevisions;
    }

    public int getChangeSetSize() {
        return changeSetSize;
    }

    public void setChangeSetSize(int changeSetSize) {
        this.changeSetSize = changeSetSize;
    }

    public int getAvgLocAdded() {
        return avgLocAdded;
    }

    public void setAvgLocAdded(int avgLocAdded) {
        this.avgLocAdded = avgLocAdded;
    }

    public int getAvgLocDeleted() {
        return avgLocDeleted;
    }

    public void setAvgLocDeleted(int avgLocDeleted) {
        this.avgLocDeleted = avgLocDeleted;
    }

    public int getChurn() {
        return churn;
    }

    public void setChurn(int churn) {
        this.churn = churn;
    }

    public int getAvgChangeSetSize() {
        return avgChangeSetSize;
    }

    public void setAvgChangeSetSize(int avgChangeSetSize) {
        this.avgChangeSetSize = avgChangeSetSize;
    }

    public int getMaxChangeSetSize() {
        return maxChangeSetSize;
    }

    public void setMaxChangeSetSize(int maxChangeSetSize) {
        this.maxChangeSetSize = maxChangeSetSize;
    }

    public int getNumOfMethods() {
        return numOfMethods;
    }

    public void setNumOfMethods(int numOfMethods) {
        this.numOfMethods = numOfMethods;
    }

    public long getAvgLocOfMethods() {
        return avgLocOfMethods;
    }

    public void setAvgLocOfMethods(long avgLocOfMethods) {
        this.avgLocOfMethods = avgLocOfMethods;
    }
}
