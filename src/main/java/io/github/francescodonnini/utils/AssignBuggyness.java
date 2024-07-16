package io.github.francescodonnini.utils;

import io.github.francescodonnini.model.Entry;
import io.github.francescodonnini.model.Issue;
import io.github.francescodonnini.model.Release;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.diff.DiffFormatter;
import org.eclipse.jgit.diff.RawTextComparator;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import org.eclipse.jgit.util.io.DisabledOutputStream;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.function.Predicate;
import java.util.logging.Level;
import java.util.logging.Logger;

// Utility class che etichetta le entry del dataset come buggy o non buggy. L'etichettatura viene effettuata a partire dai
// ticket, in particolare ogni ticket ha una lista di commit associati. Ogni commit modifica un insieme di file in un certo
// istante temporale. Un'istanza è stata pensata per poter essere riutilizzata su più sottoinsiemi diversi di release.
public class AssignBuggyness {
    private final Logger logger = Logger.getLogger(AssignBuggyness.class.getName());
    private final List<Entry> entries;
    private final List<Issue> issues;
    private Release end;
    private Release start;
    private String path;

    public AssignBuggyness(List<Entry> entries, List<Issue> issues) {
        this.entries = entries;
        this.issues = issues;
    }

    public void setEnd(Release endInclusive) {
        end = endInclusive;
    }

    public void setStart(Release start) {
        this.start = start;
    }

    public void setRepository(String path) {
        this.path = path;
    }

    public List<Entry> fill() {
        var list = entries.stream()
                .filter(e -> !e.getRelease().isAfter(end))
                .toList();
        try {
            return fillBuggyness(list);
        } catch (IOException | GitAPIException e) {
            logger.log(Level.INFO, e.getMessage());
            return List.of();
        }
    }

    private List<Entry> fillBuggyness(List<Entry> entries) throws IOException, GitAPIException {
        var repository = new FileRepositoryBuilder()
                .setGitDir(new File(path))
                .build();
        // Lista di issues afferenti alle release nell'intervallo [start, end]. Se start non viene specificato si
        // parte dalla prima release, se non viene specificato end si considera come ultima quella finale. Almeno
        // una delle due release deve essere specificata.
        var list = selectIssues();
        // mapping divide le entry per identificativo di release per facilitare il labelling.
        var mapping = new HashMap<Integer, List<Entry>>();
        entries.forEach(e -> mapping.computeIfAbsent(e.getRelease().releaseNumber(), k -> new ArrayList<>()).add(e));
        // lista di entry che devono essere etichettate come buggy.
        var buggyClasses = new ArrayList<Entry>();
        for (var issue : list) {
            for (var commit : issue.commits()) {
                var df = new DiffFormatter(DisabledOutputStream.INSTANCE);
                df.setRepository(repository);
                df.setDiffComparator(RawTextComparator.DEFAULT);
                df.setDetectRenames(true);
                var diffs = df.scan(commit.getParent(0).getId(), commit.getTree());
                // classes sono le entry potenzialmente interessate dal ticket
                var classes = new ArrayList<Entry>();
                issue.affectedVersions().forEach(v -> {
                    if (mapping.containsKey(v.releaseNumber())) {
                        classes.addAll(mapping.get(v.releaseNumber()));
                    }
                });
                for (var diff : diffs) {
                    // Percorso del file modificato da un commit afferente a issue.
                    var file = diff.getNewPath();
                    if (FileUtils.isJavaNonTestFile(file)) {
                        continue;
                    }
                    var targets = classes.stream()
                            .filter(c -> file.contains(c.getPath()))
                            .map(e -> e.withBuggy(true))
                            .toList();
                    buggyClasses.addAll(targets);
                }
            }
        }
        // Si scorrono tutte le entry da entries e si aggiornano quelle che si è scoperto essere buggy
        var all = new ArrayList<Entry>();
        for (Entry e : entries) {
            var isBuggy = buggyClasses.stream()
                    .anyMatch(c -> c.getPath().equals(e.getPath()) && e.getRelease().releaseNumber() == c.getRelease().releaseNumber());
            if (isBuggy) {
                all.add(e.withBuggy(true));
            } else {
                all.add(e);
            }
        }
        return all;
    }

    // Seleziona un sottoinsieme di issues in funzione dell'intervallo delle release selezionato.
    private List<Issue> selectIssues() {
        if (start == null && end == null) {
            throw new IllegalArgumentException("selezionare almeno un parametro tra start e end.");
        } else if (start == null) {
            return getIssuesBeforeRelease(end);
        } else if (end == null) {
            return getIssuesAfterRelease(start);
        } else {
            return getIssuesBetween(start, end);
        }
    }

    private Predicate<Issue> after(Release start) {
        return issue -> !issue.created().toLocalDate().isAfter(start.releaseDate());
    }

    private Predicate<Issue> before(Release end) {
        return issue -> !issue.created().toLocalDate().isAfter(end.releaseDate());
    }

    private List<Issue> getIssuesAfterRelease(Release start) {
        return issues.stream()
                .filter(i -> after(start).test(i))
                .toList();
    }

    private List<Issue> getIssuesBeforeRelease(Release endInclusive) {
        return issues.stream()
                .filter(i -> before(endInclusive).test(i))
                .toList();
    }

    private List<Issue> getIssuesBetween(Release start, Release endInclusive) {
        return issues.stream()
                .filter(i -> after(start).test(i))
                .filter(i -> before(endInclusive).test(i))
                .toList();

    }
}
