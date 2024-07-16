package io.github.francescodonnini.utils;

import io.github.francescodonnini.metrics.JavaClassUtils;
import io.github.francescodonnini.metrics.JavaMethod;
import io.github.francescodonnini.model.Entry;
import io.github.francescodonnini.model.Issue;
import io.github.francescodonnini.model.Release;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;

import java.io.*;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

public class CreateEntries {
    private final Logger logger = Logger.getLogger(CreateEntries.class.getName());
    // repositoryPath è il percorso delle repository dove leggere i file da cui creare le entry per il dataset.
    private final String repositoryPath;
    // releases è la lista delle release da cui selezionare i file per le entry.
    private final List<Release> releases;

    public CreateEntries(String repositoryPath, List<Issue> issues, List<Release> releases) {
        this.repositoryPath = repositoryPath;
        this.releases = releases;
    }

    public List<Entry> getEntries() {
        try {
            return createEntries();
        } catch (IOException | GitAPIException e) {
            logger.log(Level.SEVERE, e.getMessage());
            return List.of();
        }
    }

    // createEntries crea una lista di entry partendo da repositoryPath e da una lista di release selezionate da Jira.
    // Ci si aspetta che il naming delle release scelte da Jira sia consistente con il naming dei tag su github.
    private List<Entry> createEntries() throws IOException, GitAPIException {
        var repository = new FileRepositoryBuilder()
                .setGitDir(new File(repositoryPath + "/.git"))
                .build();
        var git = new Git(repository);
        var current = repository.getBranch();
        var entries = new ArrayList<Entry>();
        var tags = git.tagList().call().stream().map(Ref::getName).toList();
        for (var tag : tags) {
            var o = releases.stream().filter(r -> tag.endsWith(r.name())).findFirst();
            // Non è stata trovata la release indicata nei tag di github tra le release selezionate da Jira quindi
            // si scarta il tag.
            if (o.isEmpty()) {
                continue;
            }
            var release = o.get();
            git.checkout().setName(tag).call();
            FileUtils.listAllFiles(Path.of(repositoryPath)).stream()
                    .filter(this::isValidPath).forEach(f -> {
                        var optionalEntry = createEntry(f, release);
                        optionalEntry.ifPresent(entries::add);
                    });
        }
        // Si reimposta la repository locale di git a quella iniziale.
        git.checkout().setName(current).call();
        return entries;
    }

    // createEntry legge un file in path afferente a release (si assume che quando invocato il metodo è stato fatto
    // checkout allo snapshot della repository indicato da release).
    private Optional<Entry> createEntry(Path path, Release release) {
        var realPath = repositoryPath + "/" + path;
        try (var in = new LineNumberReader(new FileReader(realPath))) {
            // Non sempre ci si sposta alla fine del file invocando skip con il valore dell'intero massimo, potrebbe
            // essere necessario effettuare più invocazioni.
            while (in.skip(Long.MAX_VALUE) > 0);
            // buggy inizialmente viene impostato come false, perché l'etichettatura viene fatta in un secondo momento.
            var entry = new Entry(false, path.toString(), release);
            entry.setLoc(in.getLineNumber());
            entry.setAge(release.releaseNumber());
            var methods = JavaClassUtils.getMethods(Path.of(realPath));
            entry.setNumOfMethods(methods.size());
            entry.setAverageLocOfMethods(getAvgLocOfMethods(methods));
            return Optional.of(entry);
        } catch (IOException e) {
            logger.log(Level.INFO, "Release = %s, file not found: %s".formatted(release.name(), path));
            return Optional.empty();
        }
    }

    private long getAvgLocOfMethods(List<JavaMethod> methods) {
        if (methods.isEmpty()) {
            return 0;
        }
        var avg = 0L;
        for (var m : methods) {
            avg += m.numberOfLines();
        }
        return avg / methods.size();
    }

    // Si vogliono selezionare solamente i file .java che non sono file di test.
    private boolean isValidPath(Path path) {
        return FileUtils.isJavaNonTestFile(path.toString());
    }
}
