package com.aerotrack.client;


import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.UriComponentsBuilder;

@Component
public class WeatherApiClient {

    private final RestClient restClient;

    @Value("${weatherapi.api-key}")
    private String apiKey;

    @Value("${weatherapi.base-url}")
    private String baseUrl;

    public WeatherApiClient(RestClient restClient) {
        this.restClient = restClient;
    }

// Fetches current weather reports using geo-coordinates.
    public JsonNode fetchCurrentWeather(Double latitude, Double longitude) {
        // Query pattern accepts "latitude,longitude" coordinates string
        String queryCoordinates = latitude + "," + longitude;

        String url = UriComponentsBuilder.fromHttpUrl(baseUrl)
                .path("/current.json")
                .queryParam("key", apiKey)
                .queryParam("q", queryCoordinates)
                .toUriString();

        return restClient.get()
                .uri(url)
                .retrieve()
                .body(JsonNode.class);
    }
}