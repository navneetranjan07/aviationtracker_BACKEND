package com.aerotrack.service;


import com.aerotrack.client.WeatherApiClient;
import com.aerotrack.dto.WeatherResponseDto;
import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

@Service
public class WeatherService {

    private final WeatherApiClient weatherApiClient;

    public WeatherService(WeatherApiClient weatherApiClient) {
        this.weatherApiClient = weatherApiClient;
    }

    /**
     * Fetches and caches weather details for given coordinates.
     * Cache resets or saves under the key "latitude,longitude".
     */
    @Cacheable(value = "weather", key = "#latitude + ',' + #longitude")
    public WeatherResponseDto getWeatherDetails(Double latitude, Double longitude) {
        if (latitude == null || longitude == null) {
            return new WeatherResponseDto("Unknown", 0.0, 32.0, "No Weather Data Available", "", 0.0, 0);
        }

        JsonNode root = weatherApiClient.fetchCurrentWeather(latitude, longitude);
        JsonNode locationNode = root.path("location");
        JsonNode currentNode = root.path("current");
        JsonNode conditionNode = currentNode.path("condition");

        WeatherResponseDto dto = new WeatherResponseDto();
        dto.setCityName(locationNode.path("name").asText("Unknown"));
        dto.setTemperatureC(currentNode.path("temp_c").asDouble(0.0));
        dto.setTemperatureF(currentNode.path("temp_f").asDouble(32.0));
        dto.setConditionText(conditionNode.path("text").asText("Clear"));
        
        // Handle absolute URL schema safely for the frontend
        String iconUrl = conditionNode.path("icon").asText("");
        if (iconUrl.startsWith("//")) {
            iconUrl = "https:" + iconUrl;
        }
        dto.setConditionIconUrl(iconUrl);
        dto.setWindKph(currentNode.path("wind_kph").asDouble(0.0));
        dto.setHumidity(currentNode.path("humidity").asInt(0));

        return dto;
    }
}