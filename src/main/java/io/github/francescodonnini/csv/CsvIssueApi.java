package io.github.francescodonnini.csv;

import com.opencsv.bean.CsvToBeanBuilder;
import com.opencsv.bean.StatefulBeanToCsvBuilder;
import com.opencsv.enums.CSVReaderNullFieldIndicator;
import com.opencsv.exceptions.CsvDataTypeMismatchException;
import com.opencsv.exceptions.CsvRequiredFieldEmptyException;
import io.github.francescodonnini.utils.FileUtils;
import io.github.francescodonnini.api.IssueApi;
import io.github.francescodonnini.api.ReleaseApi;
import io.github.francescodonnini.model.Issue;
import io.github.francescodonnini.model.Release;
import io.github.francescodonnini.utils.CollectionUtils;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevObject;

import java.io.*;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class CsvIssueApi extends CsvAbstractApi<IssueLocalEntity, Issue> implements IssueApi {
    private final Logger logger = Logger.getLogger(CsvIssueApi.class.getName());
    private final IssueApi issueApi;
    private final List<Release> releases;
    private final List<RevCommit> commits;

    public CsvIssueApi(String path, List<RevCommit> commits, IssueApi issueApi, ReleaseApi releaseApi) {
        super(path);
        this.issueApi = issueApi;
        this.commits = commits.stream().sorted(Comparator.comparing(RevObject::getId)).toList();
        releases = releaseApi.getReleases().stream().sorted(Comparator.comparing(Release::releaseNumber)).toList();
    }

    @Override
    protected IssueLocalEntity toCsv(Issue model) {
        var bean = new IssueLocalEntity();
        bean.setAffectedVersions(model.affectedVersions().stream().map(Release::releaseNumber).toList());
        bean.setCommits(model.commits().stream().map(c -> c.getId().getName()).toList());
        bean.setCreated(model.created());
        bean.setFixVersion(model.fixVersion().releaseNumber());
        bean.setOpeningVersion(model.openingVersion().releaseNumber());
        bean.setKey(model.key());
        bean.setProject(model.project());
        return bean;
    }

    @Override
    protected Issue fromCsv(IssueLocalEntity bean) {
        List<Release> affectedVersions = new ArrayList<>();
        if (bean.getAffectedVersions() != null) {
            affectedVersions.addAll(bean.getAffectedVersions().stream().map(this::getByReleaseNumber).toList());
        }
        var fixVersion = getByReleaseNumber(bean.getFixVersion());
        var openingVersion = getByReleaseNumber(bean.getOpeningVersion());
        var commitList = new ArrayList<RevCommit>();
        if (bean.getCommits() != null) {
            commitList.addAll(bean.getCommits().stream().map(this::getByObjectId).toList());
        }
        return new Issue(
                affectedVersions,
                bean.getCreated(),
                fixVersion,
                openingVersion,
                commitList,
                bean.getKey(),
                bean.getProject()
        );
    }

    private RevCommit getByObjectId(String id) {
        var objectId = ObjectId.fromString(id);
        return commits.stream().filter(c -> c.getId().equals(objectId)).findFirst().orElse(null);
    }

    private Release getByReleaseNumber(int releaseNumber) {
        return CollectionUtils.binarySearch(releases, release -> Integer.compare(release.releaseNumber(), releaseNumber));
    }

    @Override
    public List<Issue> getIssues() {
        try {
            var beans = new CsvToBeanBuilder<IssueLocalEntity>(new FileReader(path))
                    .withType(IssueLocalEntity.class)
                    .withFieldAsNull(CSVReaderNullFieldIndicator.EMPTY_SEPARATORS)
                    .build()
                    .parse();
            return beans.stream().map(this::fromCsv).toList();
        } catch (FileNotFoundException e) {
            var issues = issueApi.getIssues();
            fetchAndSave(issues);
            return issues;
        }
    }

    private void fetchAndSave(List<Issue> issues) {
        var beans = issues.stream().map(this::toCsv).toList();
        FileUtils.createFileIfNotExists(path);
        try (var writer = new FileWriter(path)) {
            var beanToCsv = new StatefulBeanToCsvBuilder<IssueLocalEntity>(writer).build();
            for (var b : beans) {
                beanToCsv.write(b);
            }
        } catch (IOException | CsvDataTypeMismatchException | CsvRequiredFieldEmptyException e) {
            logger.log(Level.SEVERE, e.getMessage());
        }
    }

    @Override
    public String getProjectName() {
        return issueApi.getProjectName();
    }
}
