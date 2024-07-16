package io.github.francescodonnini.metrics;

public record JavaMethod(String name, long numberOfLines) {
    @Override
    public String toString() {
        return "JavaMethod{" +
                "name='" + name + '\'' +
                ", numberOfLines=" + numberOfLines +
                '}';
    }
}
