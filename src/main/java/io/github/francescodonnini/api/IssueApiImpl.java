package io.github.francescodonnini.api;

import io.github.francescodonnini.git.GitLog;
import io.github.francescodonnini.jira.JiraRestApi;
import io.github.francescodonnini.json.issue.IssueNetworkEntity;
import io.github.francescodonnini.model.Issue;
import io.github.francescodonnini.model.Release;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.revwalk.RevCommit;

import java.io.IOException;
import java.net.URISyntaxException;
import java.time.LocalDateTime;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;

public class IssueApiImpl implements IssueApi {
    private final Logger logger = Logger.getLogger(IssueApiImpl.class.getName());
    private final JiraRestApi restApi;
    private final VersionApi versionApi;
    private final String gitBasePath;

    public IssueApiImpl(JiraRestApi restApi, VersionApi versionApi, String gitBasePath) throws IOException {
        this.restApi = restApi;
        this.versionApi = versionApi;
        this.gitBasePath = gitBasePath;
    }

    /**
     * Prende gli issues da Jira relativi al progetto @projectName che hanno un commit col pattern @pattern.
     * @param projectName nome del progetto di cui si vogliono prendere gli issues.
     * @param pattern pattern dell'identificativo del ticket di cui deve essere trovato riscontro su un commit.
     * @return la lista degli issue che soddisfano le seguenti proprietà:
     * - sono stati recuperati dalla query: "project='<Project Name>' AND type=bug AND (status=closed OR status=resolved) AND resolution=fixed"
     * - ogni ticket viene citato (tramite identificativo) in almeno un commit della relativa repository.
     * - IV < FV
     * - IV <= OV
     * - OV <= FV
     */
    @Override
    public List<Issue> getIssues(String projectName, String pattern) {
        try {
            var versions = versionApi.getReleases(projectName);
            var mapping = getTicketCommitMapping(projectName, pattern);
            var issueNetworkEntities = restApi.getIssues("project='%s' AND type=bug AND (status=closed OR status=resolved) AND resolution=fixed".formatted(projectName))
                    .getIssueList().stream()
                    // Prendo solamente i ticket di Jira che hanno un commit che ne cita la chiave
                    .filter(i -> mapping.containsKey(i.getKey()))
                    .toList();
            var issues = new ArrayList<Issue>();
            for (var i : issueNetworkEntities) {
                var affectedVersions = getAffectedVersions(versions, i);
                // Al momento sto prendendo come fixVersion la prima release (ordine cronologico) nel campo fixVersions
                // Un'alternativa potrebbe essere quella di prendere come fixVersion la prima utile che ha data di rilascio
                // >= alla data di risoluzione.
                var o1 = getFixVersion(versions, i.getFields().getResolutionDate());
                var o2 = getOpeningVersion(versions, i.getFields().getCreated());
                if (o1.isEmpty() || o2.isEmpty()) continue;
                // Arrivati a questo punto si sta leggendo un ticket che possiede il campo fixVersions non vuoto.
                // Si seleziona la release con getFixVersion (attualmente prende la prima release nella lista).
                // Bisogno controllare che:
                // 1. IV < FV (cioè il bug non viene fixato nella stessa release in cui è stato trovato).
                // 2. IV <= OV
                // 3. OV <= FV (il fix del bug deve avvenire almeno nella stessa release (o comunque successiva) alla release in cui è stato scoperto)
                // Tutti e 3 i vincoli devono essere verificati solamente negli issue che hanno il campo `affectedVersion` non vuoto.
                var fixVersion = o1.get();
                var openingVersion = o2.get();
                // Non è presente una release con data di pubblicazione >= alla data di creazione del ticket
                var issue = getIssue(i, mapping, affectedVersions, openingVersion, fixVersion);
                issue.ifPresent(issues::add);
            }
            return issues;
        } catch (IOException | URISyntaxException | GitAPIException e) {
            logger.log(Level.SEVERE, e.getMessage());
            return List.of();
        }
    }

    private Optional<Release> getOpeningVersion(List<Release> releases, LocalDateTime created) {
        return releases.stream().filter(r -> r.releaseDate().isAfter(created.toLocalDate())).findFirst();
    }

    private Optional<Issue> getIssue(IssueNetworkEntity i, Map<String, List<RevCommit>> commits, List<Release> affectedVersions, Release openingVersion, Release fixVersion) {
        if (!affectedVersions.isEmpty() && !checkForConsistency(affectedVersions, openingVersion, fixVersion)) {
            return Optional.empty();
        }
        var issue = new Issue(
                affectedVersions,
                i.getFields().getCreated(),
                fixVersion,
                openingVersion,
                commits.get(i.getKey()),
                i.getKey(),
                i.getFields().getProject().getName()
        );
        return Optional.of(issue);
    }

    private boolean checkForConsistency(List<Release> affectedVersions, Release openingVersion, Release fix) {
        var injected = affectedVersions.getFirst();
        if (!injected.isBefore(fix)) {
            return false;
        }
        if (injected.isAfter(openingVersion)) {
            return false;
        }
        return !openingVersion.isAfter(fix);
    }

    /**
     * getFixVersion assegna al ticket la release in cui il relativo bug è stato risolto.
     * @param releases lista delle release, si utilizza per selezionare un eventuale fixVersion.
     * @param resolutionDate data in cui il ticket è stato risolto,
     * @return la fixVersion se possibile altrimenti Optional.empty()
     */
    private Optional<Release> getFixVersion(List<Release> releases, LocalDateTime resolutionDate) {
        return releases.stream().filter(r -> r.releaseDate().isAfter(resolutionDate.toLocalDate())).findFirst();
    }

    /**
     * getTicketCommitMapping costruisce una mappa la cui chiave è l'identificativo del ticket che viene citato
     * nel messaggio di un commit (quest'ultimo è il valore della chiave).
     * @param pattern espressione regolare che descrive come è fatta la chiave del ticket che si cerca nei messaggi dei
     *                commit.
     * @return una mappa chiave ticket, commit il cui messaggio contiene la chiave del ticket.
     */
    private Map<String, List<RevCommit>> getTicketCommitMapping(String projectName, String pattern) throws GitAPIException, IOException {
        var logs = new GitLog(gitBasePath.formatted(projectName.toLowerCase()));
        var p = Pattern.compile(pattern);
        var mapping = new HashMap<String, List<RevCommit>>();
        for (var commit : logs.getAll()) {
            var matcher = p.matcher(commit.getFullMessage());
            if (matcher.find()) {
                mapping.computeIfAbsent(matcher.group(), v -> new ArrayList<>()).add(commit);
            }
        }
        return mapping;
    }

    private List<Release> getAffectedVersions(List<Release> versions, IssueNetworkEntity issue) {
        var affectedVersions = new ArrayList<Release>();
        for (var v : versions) {
            for (var affectedVersion : issue.getFields().getAffectedVersions()) {
                if (v.id().equals(affectedVersion.getId())) {
                    affectedVersions.add(v);
                }
            }
        }
        return affectedVersions;
    }
}
