package io.github.francescodonnini.model;

import java.util.*;

public class Entry {
    private boolean buggy;
    private String path;
    private final Release release;
    private int churn;
    private int locTouched;
    private int numOfAdditions;
    private int locAdded;
    private int numOfDeletions;
    private int locDeleted;
    private int loc;
    private int numOfRevisions;
    private int changeSetSize;
    private int maxChangeSetSize;
    private int avgChangeSetSize;
    private int numOfMethods;
    private long averageLocOfMethods;
    private int age;
    private final List<String> authors = new ArrayList<>();

    public Entry(boolean buggy, String path, Release release) {
        this.buggy = buggy;
        this.path = path;
        this.release = release;
    }
    public boolean isBuggy() {
        return buggy;
    }

    public void setBuggy(boolean buggy) {
        this.buggy = buggy;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public Release getRelease() {
        return release;
    }

    public Entry withBuggy(boolean buggy) {
        return new Entry(buggy, path, release);
    }

    public int getChurn() {
        return churn;
    }

    public void addChurn(int delta) {
        this.churn = delta;
    }

    public int getLocAdded() {
        return locAdded;
    }

    public void addLocAdded(int locAdded) {
        this.locAdded += locAdded;
        numOfAdditions += 1;
    }

    public int getLocDeleted() {
        return locDeleted;
    }

    public void addLocDeleted(int locDeleted) {
        this.locDeleted += locDeleted;
        numOfDeletions += 1;
    }

    public List<String> getAuthors() {
        return authors;
    }

    public void addAuthor(String author) {
        if (authors.contains(author)) {
            return;
        }
        authors.add(author);
    }

    public int getAvgLocAdded() {
        if (locAdded == 0) {
            return 0;
        }
        return locAdded / numOfAdditions;
    }

    public int getAvgLocDeleted() {
        if (locDeleted == 0) {
            return 0;
        }
        return locDeleted / numOfDeletions;
    }

    public int getNumberOfAuthors() {
        return authors.size();
    }

    public int getLoc() {
        return loc;
    }

    public void setLoc(int loc) {
        this.loc = loc;
    }

    public int getNumOfRevisions() {
        return numOfRevisions;
    }

    public void incNumOfRevisions() {
        numOfRevisions++;
    }

    public int getChangeSetSize() {
        return changeSetSize;
    }

    public void setChangeSetSize(int changeSetSize) {
        this.changeSetSize = changeSetSize;
    }

    public int getMaxChangeSetSize() {
        return maxChangeSetSize;
    }

    public void setMaxChangeSetSize(int maxChangeSetSize) {
        this.maxChangeSetSize = maxChangeSetSize;
    }

    public int getAvgChangeSetSize() {
        return avgChangeSetSize;
    }

    public void setAvgChangeSetSize(int avgChangeSetSize) {
        this.avgChangeSetSize = avgChangeSetSize;
    }

    public int getLocTouched() {
        return locTouched;
    }

    public void addLocTouched(int locTouched) {
        this.locTouched += locTouched;
    }

    public int getNumOfMethods() {
        return numOfMethods;
    }

    public void setNumOfMethods(int numOfMethods) {
        this.numOfMethods = numOfMethods;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }

    public float getWeightedAge() {
        return (float) age / loc;
    }

    public long getAverageLocOfMethods() {
        return averageLocOfMethods;
    }

    public void setAverageLocOfMethods(long averageLocOfMethods) {
        this.averageLocOfMethods = averageLocOfMethods;
    }
}
