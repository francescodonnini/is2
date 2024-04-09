package io.github.francescodonnini.json.issue;

import com.google.gson.annotations.Expose;

import java.util.List;

public class Issues {
    @Expose
    private String expand;
    @Expose
    private int startAt;
    @Expose
    private int maxResults;
    @Expose
    private int total;
    @Expose
    private List<Issue> issues;

    public String getExpand() {
        return expand;
    }

    public void setExpand(String expand) {
        this.expand = expand;
    }

    public int getStartAt() {
        return startAt;
    }

    public void setStartAt(int startAt) {
        this.startAt = startAt;
    }

    public int getMaxResults() {
        return maxResults;
    }

    public void setMaxResults(int maxResults) {
        this.maxResults = maxResults;
    }

    public int getTotal() {
        return total;
    }

    public void setTotal(int total) {
        this.total = total;
    }

    public List<Issue> getIssues() {
        return issues;
    }

    public void setIssues(List<Issue> issues) {
        this.issues = issues;
    }
}
