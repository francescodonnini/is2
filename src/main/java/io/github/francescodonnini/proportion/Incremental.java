package io.github.francescodonnini.proportion;

import io.github.francescodonnini.api.IssueApi;
import io.github.francescodonnini.api.ReleaseApi;
import io.github.francescodonnini.model.Issue;
import io.github.francescodonnini.model.Release;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class Incremental implements Proportion {
    private final IssueApi issueApi;
    private final ReleaseApi releaseApi;

    public Incremental(IssueApi issueApi, ReleaseApi releaseApi) {
        this.issueApi = issueApi;
        this.releaseApi = releaseApi;
    }

    @Override
    public List<Issue> fillOut() {
        var releases = releaseApi.getReleases();
        var l = issueApi.getIssues();
        // labeled è l'insieme degli issue che hanno affectedVersions != [] e fanno parte di un release superiore alla prima.
        var labeled = l.stream()
                .filter(i -> !i.affectedVersions().isEmpty() && i.fixVersion().releaseNumber() > 0)
                .sorted(Comparator.comparing(Issue::created))
                .toList();
        // unlabeled è l'insieme degli issue che hanno affectedVersions = [] e fanno parte di un release superiore alla prima.
        var unlabeled = l.stream()
                .filter(i -> i.affectedVersions().isEmpty() && i.fixVersion().releaseNumber() > 0)
                .sorted(Comparator.comparing(Issue::created))
                .toList();
        // issues è l'insieme degli issues risultante: tutti gli elementi devono avere il campo affectedVersions non vuoto, che
        // sia già presente o calcolato col proportion.
        var issues = new ArrayList<>(labeled);
        for (var issue : unlabeled) {
            var fixVersion = issue.fixVersion();
            // batch è l'insieme degli issue che si utilizza per calcolare P
            // contiene tutti gli issue che hanno fixVersion <= di fixVersion di issue
            var batch = labeled.stream().filter(i -> i.fixVersion().releaseNumber() < fixVersion.releaseNumber()).toList();
            var p = calculateProportion(batch);
            var fv = issue.fixVersion().releaseNumber();
            var ov = issue.openingVersion().releaseNumber();
            var den = fv - ov == 0 ? 1 : fv - ov;
            var iv = (int) Math.floor(fv - (fv - ov)*p);
            issues.add(issue.withAffectedVersions(getRange(releases, iv, ov)));
        }
        return issues;
    }

    private List<Release> getRange(List<Release> releases, int start, int endInclusive) {
        return releases.stream().filter(r -> r.releaseNumber() >= start && r.releaseNumber() <= endInclusive).toList();
    }

    private double calculateProportion(List<Issue> batch) {
        var sum = 0.0;
        for (var i : batch) {
            var fv = (double) i.fixVersion().releaseNumber();
            var ov = (double) i.openingVersion().releaseNumber();
            var den = fv - ov == 0 ? 1 : fv - ov;
            var iv = (double) i.getInjectedVersion().releaseNumber();
            sum += (fv - iv)/(den);
        }
        return sum / batch.size();
    }
}
