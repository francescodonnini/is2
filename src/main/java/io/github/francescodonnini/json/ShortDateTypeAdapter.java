package io.github.francescodonnini.json;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;

public class ShortDateTypeAdapter extends TypeAdapter<LocalDate> {
    @Override
    public void write(JsonWriter jsonWriter, LocalDate localDate) {
        throw new UnsupportedOperationException();
    }

    @Override
    public LocalDate read(JsonReader jsonReader) throws IOException {
        return LocalDate.parse(jsonReader.nextString(), DateTimeFormatter.ISO_LOCAL_DATE);
    }
}
