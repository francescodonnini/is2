package io.github.francescodonnini.json.version;

import com.google.gson.annotations.Expose;

import java.util.List;

public class VersionList {
    @Expose
    private List<Version> versions;

    public List<Version> getVersions() {
        return versions;
    }

    public void setVersions(List<Version> versions) {
        this.versions = versions;
    }
}
