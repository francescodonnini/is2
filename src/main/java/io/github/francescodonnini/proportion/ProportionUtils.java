package io.github.francescodonnini.proportion;

import io.github.francescodonnini.model.Issue;
import io.github.francescodonnini.model.Release;

import java.util.Comparator;
import java.util.List;

public class ProportionUtils {
    private ProportionUtils() {}

    public static Issue calculateAffectedVersions(Issue issue, double p, List<Release> releases) {
        var fv = issue.fixVersion().releaseNumber();
        var ov = issue.openingVersion().releaseNumber();
        var den = ((fv - ov) == 0) ? 1 : (fv - ov);
        var iv = (int) Math.floor(fv - den*p);
        return issue.withAffectedVersions(getRange(releases, iv, ov));
    }

    private static List<Release> getRange(List<Release> releases, int start, int endInclusive) {
        return releases.stream().filter(r -> r.releaseNumber() >= start && r.releaseNumber() <= endInclusive).toList();
    }

    public static double calculateProportion(List<Issue> batch) {
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

    public static List<Issue> getLabeledIssues(List<Issue> issues) {
        return issues.stream()
                .filter(i -> !i.affectedVersions().isEmpty() && i.fixVersion().releaseNumber() > 0)
                .sorted(Comparator.comparing(Issue::created))
                .toList();
    }

    public static List<Issue> getUnlabeledIssues(List<Issue> issues) {
        return issues.stream()
                .filter(i -> i.affectedVersions().isEmpty() && i.fixVersion().releaseNumber() > 0)
                .sorted(Comparator.comparing(Issue::created))
                .toList();
    }
}
