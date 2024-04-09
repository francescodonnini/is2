package io.github.francescodonnini;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class JqlQueryBuilder {
    private static final DateTimeFormatter dateFormat = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm");
    public static String and(String left, String right) {
        return String.format("%s AND %s", left, right);
    }

    public static String concat(String left, String right) {
        return String.format("%s & %s", left, right);
    }

    public static String or(String left, String right) {
        return String.format("%s OR %s", left, right);
    }

    public static String project(String projectName) {
        return String.format("project='%s'", projectName);
    }

    public static String created(String operator, LocalDateTime date) {
        return format("created", operator, date.format(dateFormat));
    }

    public static String resolution(String operator, int resolutionId) {
        return format("resolution", operator, resolutionId);
    }

    public static String resolution(String operator, String resolutionName) {
        return format("resolution", operator, resolutionName);
    }

    public static String status(String operator, int statusId) {
        return format("created", operator, statusId);
    }

    public static String status(String operator, String statusName) {
        return format("status", operator, statusName);
    }

    public static String type(String operator, int typeId) {
        return format("type", operator, typeId);
    }

    public static String type(String operator, String typeName) {
        return format("type", operator, typeName);
    }

    private static String format(String field, String operator, String value) {
        return String.format("%s %s %s", field, operator, value);
    }

    private static String format(String field, String operator, int value) {
        return String.format("%s %s %d", field, operator, value);
    }

    public static String fields(String... fields) {
        return String.format("fields=%s", String.join(",", fields));
    }

    public static String startAt(int i) {
        return String.format("startAt=%d", i);
    }
}
