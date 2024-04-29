package io.github.francescodonnini.jira;

public class JiraEndpoints {
    private JiraEndpoints() {}

    public static String createProjectQuery(String projectName) {
        return String.format("https://issues.apache.org/jira/rest/api/2/project/%s", projectName);
    }

    public static String createSearchQuery(String jql) {
        return String.format("https://issues.apache.org/jira/rest/api/2/search?jql=%s", jql);
    }
}
