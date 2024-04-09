package io.github.francescodonnini;

import com.google.gson.Gson;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.concurrent.ExecutionException;

public class JiraRestApi {
    private final HttpClient client = HttpClient
            .newBuilder()
            .build();

    public <T> T get(String uri, Class<T> clazz) throws URISyntaxException, IOException, InterruptedException, ExecutionException {
        var request = HttpRequest.newBuilder()
                .uri(new URI(uri))
                .header("Accept", "application/json")
                .GET()
                .build();

        return client
                .sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(HttpResponse::body)
                .thenApply((String json) -> {
                    var mapper = new GsonMapper<T>();
                    return mapper.from(json, clazz);
                })
                .join();
    }

    private static class GsonMapper<T> {
        public T from(String json, Class<T> clazz) {
            var gson = new Gson();
            return gson.fromJson(json, clazz);
        }
    }
}
