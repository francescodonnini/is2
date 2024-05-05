package io.github.francescodonnini.csv;


public abstract class CsvAbstractApi<B, T> {
    // path to save .csv file
    protected final String path;

    protected CsvAbstractApi(String path) {
        this.path = path;
    }

    protected abstract B toCsv(T model);

    protected abstract T fromCsv(B bean);
}
