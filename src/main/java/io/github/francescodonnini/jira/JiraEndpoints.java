package io.github.francescodonnini.jira;

public class JiraEndpoints {
    public static String Project(String projectName) {
        return String.format("https://issues.apache.org/jira/rest/api/2/project/%s", projectName);
    }

    public static String Search(String jql) {
        return String.format("https://issues.apache.org/jira/rest/api/2/search?jql=%s", jql);
    }
}
