package io.github.francescodonnini.api;

import com.opencsv.CSVReader;
import com.opencsv.CSVWriter;
import com.opencsv.exceptions.CsvValidationException;
import io.github.francescodonnini.git.GitLog;
import io.github.francescodonnini.model.Issue;
import io.github.francescodonnini.model.Release;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.revwalk.RevCommit;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

public class CsvIssueApi implements IssueApi {
    private static final int AFFECTED_VERSIONS_FIELD = 0;
    private static final int CREATED_FIELD = 1;
    private static final int OPENING_FIELD = 2;
    private static final int FIX_VERSION_FIELD = 3;
    private static final int COMMITS_FIELD = 4;
    private static final int KEY_FIELD = 5;
    private static final int PROJECT_FIELD = 6;
    private static final String[] HEADER = {
            "Affected Versions",
            "Created",
            "Opening Version",
            "Fixed Version",
            "Commits",
            "Key",
            "Project"
    };
    private final Logger logger;
    private final IssueApi issueApi;
    private final VersionApi versionApi;
    private final String path;
    private final String gitBasePath;

    public CsvIssueApi(String path, String gitBasePath, IssueApi issueApi, VersionApi versionApi) {
        this.path = path;
        this.gitBasePath = gitBasePath;
        this.issueApi = issueApi;
        this.versionApi = versionApi;
        logger = Logger.getLogger(CsvIssueApi.class.getName());
    }

    @Override
    public List<Issue> getIssues(String projectName, String pattern) {
        var issuePath = "%s/%s/issues.csv".formatted(path, projectName);
        var issues = new ArrayList<Issue>();
        try (var r = new CSVReader(new FileReader(issuePath))) {
            issues.addAll(fromCsv(r, projectName));
        } catch (IOException | CsvValidationException e) {
            logger.log(Level.INFO, e.getMessage());
            issues.addAll(issueApi.getIssues(projectName, pattern));
            toCsv(issues, issuePath);
        } catch (GitAPIException e) {
            logger.log(Level.SEVERE, e.getMessage());
        }
        return issues;
    }

    private List<Issue> fromCsv(CSVReader reader, String projectName) throws CsvValidationException, IOException, GitAPIException {
        /*
        * 1. affected-versions  []int
        * 2. created            date
        * 3. fix-version        int
        * 4. key                string
        * 5. project            string
        * */
        reader.readNext();
        List<Issue> issues = new ArrayList<>();
        var releases = versionApi.getReleases(projectName);
        var git = new GitLog(gitBasePath.formatted(projectName.toLowerCase()));
        var commits = git.getAll();
        String[] line;
        while ((line = reader.readNext()) != null) {
            var avField = line[AFFECTED_VERSIONS_FIELD];
            var av = new ArrayList<Release>();
            if (!avField.isEmpty()) {
                av.addAll(parseAffectedVersionList(Arrays.stream(line[AFFECTED_VERSIONS_FIELD].split(",")).mapToInt(Integer::parseInt).toArray(), releases));
            }
            var created = LocalDateTime.parse(line[CREATED_FIELD]);
            var openingVersion = parseVersion(Integer.parseInt(line[OPENING_FIELD]), releases);
            var fixVersion = parseVersion(Integer.parseInt(line[FIX_VERSION_FIELD]), releases);
            var commitList = parseCommit(commits, line[COMMITS_FIELD]);
            if (fixVersion.isEmpty() || openingVersion.isEmpty() || commitList.isEmpty()) continue;
            var key = line[KEY_FIELD];
            var project = line[PROJECT_FIELD];
            issues.add(new Issue(av, created, openingVersion.get(), fixVersion.get(), commitList, key, project));
        }
        return issues;
    }

    private List<RevCommit> parseCommit(List<RevCommit> commits, String s) {
        var identifiers = s.split(",");
        var commitList = new ArrayList<RevCommit>();
        var objectIds = Arrays.stream(identifiers).map(ObjectId::fromString).toList();
        for (var commit : commits) {
            for (var id : objectIds) {
                if (id.equals(commit.getId())) {
                    commitList.add(commit);
                }
            }
        }
        return commitList;
    }

    private List<Release> parseAffectedVersionList(int[] list, List<Release> versionList) {
        return versionList.stream().filter(v -> Arrays.stream(list).anyMatch(i -> i == v.releaseNumber())).toList();
    }

    private Optional<Release> parseVersion(int version, List<Release> versionList) {
        return versionList.stream().filter(v -> v.releaseNumber() == version).findFirst();
    }

    private void toCsv(List<Issue> issues, String issuePath) {
        try {
            var file = new File(issuePath);
            if (file.getParentFile().mkdirs()) {
                var message = "directory %s has been created.".formatted(file.getParentFile());
                logger.log(Level.INFO, message);
            }
            var w = new CSVWriter(new FileWriter(file));
            w.writeNext(HEADER);
            for (var issue : issues) {
                var affectedVersions = String.join(",", issue.affectedVersions().stream().map(v -> String.valueOf(v.releaseNumber())).toList());
                var commits = String.join(",", issue.commits().stream().map(c -> c.getId().getName()).toList());
                w.writeNext(new String[] {
                        affectedVersions,
                        issue.created().toString(),
                        String.valueOf(issue.openingVersion().releaseNumber()),
                        String.valueOf(issue.fixVersion().releaseNumber()),
                        commits,
                        issue.key(),
                        issue.project()
                });
            }
            w.close();
        } catch (IOException e) {
            logger.log(Level.SEVERE, e.getMessage());
        }
    }
}
