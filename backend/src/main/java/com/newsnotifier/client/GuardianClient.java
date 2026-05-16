package com.newsnotifier.client;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.newsnotifier.dto.GuardianArticle;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@Component
@Slf4j
public class GuardianClient {

    private final String apiKey;
    private final RestClient restClient;

    public GuardianClient(@Value("${guardian.api.key}") String apiKey) {
        this.apiKey = apiKey;
        this.restClient = RestClient.create();
    }

    public List<GuardianArticle> fetchArticles(String guardianKey, LocalDate date) {
        try {
            GuardianApiResponse response = restClient.get()
                    .uri("https://content.guardianapis.com/search?section={section}&from-date={date}&to-date={date}&page-size=30&show-fields=bodyText&api-key={apiKey}",
                            Map.of("section", guardianKey, "date", date, "apiKey", apiKey))
                    .retrieve()
                    .body(GuardianApiResponse.class);

            if (response == null || response.response() == null || response.response().results() == null) {
                return Collections.emptyList();
            }

            return response.response().results().stream()
                    .map(r -> new GuardianArticle(
                            r.webTitle(),
                            r.webUrl(),
                            r.fields() != null && r.fields().bodyText() != null ? r.fields().bodyText() : ""))
                    .toList();
        } catch (Exception e) {
            log.error("GuardianClient failed for category '{}': {}", guardianKey, e.getMessage());
            return Collections.emptyList();
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private record GuardianApiResponse(GuardianResponseBody response) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    private record GuardianResponseBody(List<GuardianResult> results) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    private record GuardianResult(String webTitle, String webUrl, GuardianFields fields) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    private record GuardianFields(String bodyText) {}
}
