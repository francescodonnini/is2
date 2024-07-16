package io.github.francescodonnini.metrics;

import io.github.francescodonnini.model.Entry;

import java.util.List;

public interface Calculator {
    List<Entry> calculate(List<Entry> entries);
}
