package io.github.francescodonnini.proportion;

import io.github.francescodonnini.model.Issue;
import io.github.francescodonnini.model.Release;

import java.util.ArrayList;
import java.util.List;

public class NewIncremental implements Proportion {
    private final List<Issue> issues;
    private final List<Release> releases;

    public NewIncremental(List<Issue> issues, List<Release> releases) {
        this.issues = issues;
        this.releases = releases;
    }

    @Override
    public List<Issue> fillOut() {
        // labeled è l'insieme degli issue che hanno affectedVersions != [] e fanno parte di un release superiore alla prima.
        var labeled = ProportionUtils.getLabeledIssues(issues);
        var unlabeled = ProportionUtils.getUnlabeledIssues(issues);
        var p = ProportionUtils.calculateProportion(labeled);
        var issues = new ArrayList<>(labeled);
        for (var issue : unlabeled) {
            issues.add(ProportionUtils.calculateAffectedVersions(issue, p, releases));
        }
        return issues;
    }
}
