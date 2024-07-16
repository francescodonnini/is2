package io.github.francescodonnini.metrics;

import io.github.francescodonnini.model.Entry;
import io.github.francescodonnini.model.Release;
import io.github.francescodonnini.utils.FileUtils;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.diff.DiffFormatter;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.util.io.DisabledOutputStream;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.StreamSupport;

public class CalculatorImpl implements Calculator {
    private final Logger logger = Logger.getLogger(CalculatorImpl.class.getName());
    private final Git git;
    private final List<Release> releases;

    public CalculatorImpl(List<Release> releases, Git git) {
        this.releases = new ArrayList<>(releases);
        this.git = git;
    }

    /*
    * (*): è possibile misurare la metrica ispezionando linearmente i commit.
    * Data una entry afferente alla release i, calcola le seguenti metriche:
    * - [x] LOC Touched: somma delle linee aggiunte/eliminate.
    * - [x] Churn: differenza delle linee aggiunte/eliminate
    * - [x] NR: numero di commit che modificano la classe.
    * - [x] Number of Authors: numero di sviluppatori che hanno contribuito alla classe.
    * - [x] LOC Added: somma delle line aggiunte.
    * - [x] Avg LOC Added: numero medio di righe aggiunte per commit.
    * - [x] Change Set Size: numero di file committati insieme.
    * - [x] Max Change Set Size: Numero massimo di file committati insieme.
    * - [x] Average Change Set: numero medio di file committati insieme.
    * - [] Age: età della release.
    * - [] Weighted Age: età della release pesate per il numero di linee toccate.
    */
    @Override
    public List<Entry> calculate(List<Entry> entries) {
        // mapping contiene tutte le entries divise per numero di release.
        var mapping = new HashMap<Integer, List<Entry>>();
        entries.forEach(e -> mapping.computeIfAbsent(e.getRelease().releaseNumber(), k -> new ArrayList<>()).add(e));
        try {
            // Si prende la lista di tutti i commit e si ordinano per data di pubblicazione.
            var commits = StreamSupport.stream(git.log().call().spliterator(), false)
                    .sorted(Comparator.comparing(this::getCommitTime))
                    .toList();
            var changeSet = new HashSet<String>();
            for (var commit : commits) {
                // Si prende la release a cui quel commit appartiene.
                // Se non esiste una release disponibile si scarta il commit.
                var o = getReleaseByCommit(commit);
                if (o.isEmpty()) {
                    continue;
                }
                var release = o.get();
                // Si prende la lista delle entries afferenti a release.
                // Queste sono le entries che possono essere influenzate dal commit che si sta analizzando.
                // Se non ci sono entries afferenti a release allora si scarta il commit.
                var susceptibles = mapping.get(release.releaseNumber());
                if (susceptibles == null || susceptibles.isEmpty()) {
                    continue;
                }
                // Un commit è una lista di modifiche fatte a uno o più file, è necessario iterare
                // tra le modifiche (diff) per raccogliere le metriche.
                RevCommit parent = null;
                try {
                    parent = commit.getParent(0);
                } catch (IndexOutOfBoundsException e) {
                    logger.log(Level.INFO, "Commit %s has no parent".formatted(commit));
                }
                if (parent == null) {
                    continue;
                }
                var df = new DiffFormatter(DisabledOutputStream.INSTANCE);
                df.setRepository(git.getRepository());
                df.setDetectRenames(true);
                var diffs = df.scan(parent.getTree(), commit.getTree());
                for (var diff : diffs) {
                    var path = diff.getNewPath();
                    // Se il percorso del file modificato non è un file .java allora non è necessario analizzare
                    // la modifica.
                    if (!FileUtils.isJavaNonTestFile(path)) {
                        continue;
                    }
                    changeSet.add(path);
                    // I file potrebbero essere stati rinominati ed è necessario quindi aggiornare tutte le entry che hanno
                    // il vecchio percorso. Bisogna controllare se il vecchio path è diverso da /dev/null dato che in tal caso non è necessario fare alcune
                    // operazione perché significa che il file non esisteva prima di quel commit.
                    var oldPath = diff.getOldPath();
                    if (!oldPath.equals("/dev/null") && !oldPath.equals(path)) {
                        renameAllEntries(entries, oldPath, path);
                    }
                    var optionalEntry = susceptibles.stream().filter(it -> it.getPath().contains(path)).findFirst();
                    // Se non esiste alcuna entry con quel percorso allora non è necessario analizzare la modifica.
                    if (optionalEntry.isEmpty()) {
                        continue;
                    }
                    var entry = optionalEntry.get();
                    // A questo punto si può analizzare come il commit in questione ha modificato la entry.
                    // Un commit contiene le seguenti informazioni utili:
                    // - autore (email).
                    // - linee di codice aggiunte/eliminate.
                    var del = 0;
                    var add = 0;
                    for (var edit : df.toFileHeader(diff).toEditList()) {
                        del += edit.getEndA() - edit.getBeginA();
                        add += edit.getEndB() - edit.getBeginB();
                    }
                    entry.addChurn(add - del);
                    entry.addLocTouched(add + del);
                    entry.addLocAdded(add);
                    entry.addLocDeleted(del);
                    entry.incNumOfRevisions();
                    var author = getAuthor(commit);
                    author.ifPresent(entry::addAuthor);
                    entry.setChangeSetSize(changeSet.size());
                    changeSet.clear();
                }
            }
            calculateAverages(mapping);
            return entries;
        } catch (GitAPIException | IOException e) {
            logger.log(Level.SEVERE, e.getMessage(), e);
            return List.of();
        }
    }

    /*
     * calcola le metriche:
     * - [x] Average Change Set Size
     * - [x] Max Change Set Size
     */
    private void calculateAverages(Map<Integer, List<Entry>> mapping) {
        var averages = new HashMap<String, Integer>();
        var maximums = new HashMap<String, Integer>();
        var numOfSamples = new HashMap<String, Integer>();
        var releases = mapping.keySet().stream().sorted(Integer::compareTo).toList();
        for (var r : releases) {
            var entries = mapping.get(r);
            for (var e : entries) {
                var path = e.getPath();
                var count = numOfSamples.getOrDefault(path, 0) + 1;
                var prevAvg = averages.getOrDefault(path, 0);
                var avg = (prevAvg + e.getChangeSetSize()) / count;
                var prevMax = maximums.getOrDefault(path, 0);
                var max = e.getChangeSetSize() > prevMax ? e.getChangeSetSize() : prevMax;
                e.setAvgChangeSetSize(avg);
                e.setMaxChangeSetSize(max);
                averages.put(path, avg);
                maximums.put(path, max);
                numOfSamples.put(path, count);
            }
        }
    }

    // cambia il path di tutte le entry da oldPath a newPath
    private void renameAllEntries(List<Entry> entries, String oldPath, String newPath) {
        for (var entry : entries) {
            if (entry.getPath().equals(oldPath)) {
                entry.setPath(newPath);
            }
        }
    }

    private Optional<String> getAuthor(RevCommit commit) {
        var author = commit.getAuthorIdent().getEmailAddress();
        if (author == null || author.isEmpty() || author.equalsIgnoreCase("unknown@apache.org")) {
            return Optional.empty();
        }
        return Optional.of(author);
    }

    private Optional<Release> getReleaseByCommit(RevCommit commit) {
        return getReleaseByDate(getCommitTime(commit));
    }

    private Optional<Release> getReleaseByDate(LocalDateTime commitTime) {
        for (Release r : releases) {
            if (r.releaseDate().isAfter(commitTime.toLocalDate())) {
                return Optional.of(r);
            }
        }
        return Optional.empty();
    }

    private LocalDateTime getCommitTime(RevCommit commit) {
        return commit.getAuthorIdent().getWhenAsInstant().atZone(commit.getAuthorIdent().getZoneId()).toLocalDateTime();
    }
}
