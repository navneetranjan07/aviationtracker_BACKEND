package com.aerotrack.client;

import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.UriComponentsBuilder;

@Component
public class AviationStackClient {

    private final RestClient restClient;
    
    @Value("${aviationstack.api-key}")
    private String apiKey;

    @Value("${aviationstack.base-url}")
    private String baseUrl;

    public AviationStackClient(RestClient restClient) {
        this.restClient = restClient;
    }

     // Fetches live flight positions, telemetry, and airport details by Flight Number.
    public JsonNode fetchLiveFlightData(String flightNumber) {
        String url = UriComponentsBuilder.fromHttpUrl(baseUrl)
                .path("/flights")
                .queryParam("access_key", apiKey)
                .queryParam("flight_iata", flightNumber)
                .toUriString();

        return restClient.get()
                .uri(url)
                .retrieve()
                .body(JsonNode.class);
    }

    /**
     * airportIata = Airport name code, like DEL for Delhi.
     * dep_iata = Departures board
     * arr_iata = Arrivals board
     */
    public JsonNode fetchAirportBoardData(String airportIata, String type) {
        String structuredUrl = UriComponentsBuilder.fromHttpUrl(baseUrl)
                .path("/flights")
                .queryParam("access_key", apiKey)
                .queryParam("limit", 20)
                .queryParam(type, airportIata)
                .toUriString();

        return restClient.get()
                .uri(structuredUrl)
                .retrieve()
                .body(JsonNode.class);
    }
}