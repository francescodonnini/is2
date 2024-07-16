package io.github.francescodonnini.proportion;

import io.github.francescodonnini.api.IssueApi;
import io.github.francescodonnini.api.ReleaseApi;
import io.github.francescodonnini.model.Issue;
import io.github.francescodonnini.model.Release;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class Incremental implements Proportion {
    private final List<Issue> issues;
    private final List<Release> releases;

    public Incremental(List<Issue> issues, List<Release> releases) {
        this.issues = issues;
        this.releases = releases;
    }

    @Override
    public List<Issue> fillOut() {
        // labeled è l'insieme degli issue che hanno affectedVersions != [] e fanno parte di un release superiore alla prima.
        var labeled = ProportionUtils.getLabeledIssues(issues);
        // unlabeled è l'insieme degli issue che hanno affectedVersions = [] e fanno parte di un release superiore alla prima.
        var unlabeled = ProportionUtils.getUnlabeledIssues(issues);
        // issues è l'insieme degli issues risultante: tutti gli elementi devono avere il campo affectedVersions non vuoto, che
        // sia già presente o calcolato col proportion.
        var issues = new ArrayList<>(labeled);
        for (var issue : unlabeled) {
            var fixVersion = issue.fixVersion();
            // batch è l'insieme degli issue che si utilizza per calcolare P
            // contiene tutti gli issue che hanno fixVersion <= di fixVersion di issue
            var batch = labeled.stream().filter(i -> i.fixVersion().releaseNumber() < fixVersion.releaseNumber()).toList();
            var p = ProportionUtils.calculateProportion(batch);
            issues.add(ProportionUtils.calculateAffectedVersions(issue, p, releases));
        }
        return issues;
    }
}
