package com.aerotrack.dto;

public class WeatherResponseDto {
    private String cityName;
    private Double temperatureC;
    private Double temperatureF;
    private String conditionText;
    private String conditionIconUrl;
    private Double windKph;
    private Integer humidity;

    // Default Constructor
    public WeatherResponseDto() {}

    // All-Args Constructor
    public WeatherResponseDto(String cityName, Double temperatureC, Double temperatureF, 
                              String conditionText, String conditionIconUrl, Double windKph, Integer humidity) {
        this.cityName = cityName;
        this.temperatureC = temperatureC;
        this.temperatureF = temperatureF;
        this.conditionText = conditionText;
        this.conditionIconUrl = conditionIconUrl;
        this.windKph = windKph;
        this.humidity = humidity;
    }

    // Getters and Setters
    public String getCityName() { return cityName; }
    public void setCityName(String cityName) { this.cityName = cityName; }

    public Double getTemperatureC() { return temperatureC; }
    public void setTemperatureC(Double temperatureC) { this.temperatureC = temperatureC; }

    public Double getTemperatureF() { return temperatureF; }
    public void setTemperatureF(Double temperatureF) { this.temperatureF = temperatureF; }

    public String getConditionText() { return conditionText; }
    public void setConditionText(String conditionText) { this.conditionText = conditionText; }

    public String getConditionIconUrl() { return conditionIconUrl; }
    public void setConditionIconUrl(String conditionIconUrl) { this.conditionIconUrl = conditionIconUrl; }

    public Double getWindKph() { return windKph; }
    public void setWindKph(Double windKph) { this.windKph = windKph; }

    public Integer getHumidity() { return humidity; }
    public void setHumidity(Integer humidity) { this.humidity = humidity; }
}