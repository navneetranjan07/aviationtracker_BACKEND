package com.aerotrack.dto;

import java.util.List;

public class AirportBoardDto {
    private String airportCode;
    private List<ScheduleItem> departures;
    private List<ScheduleItem> arrivals;

    // Getters and Setters
    public String getAirportCode() { return airportCode; }
    public void setAirportCode(String airportCode) { this.airportCode = airportCode; }

    public List<ScheduleItem> getDepartures() { return departures; }
    public void setDepartures(List<ScheduleItem> departures) { this.departures = departures; }

    public List<ScheduleItem> getArrivals() { return arrivals; }
    public void setArrivals(List<ScheduleItem> arrivals) { this.arrivals = arrivals; }

    // Static Inner Class
    public static class ScheduleItem {
        private String flightNumber;
        private String airline;
        private String terminal;
        private String gate;
        private String scheduledTime;
        private String estimatedTime;
        private String counterCity;
        private String status;

        // Getters and Setters
        public String getFlightNumber() { return flightNumber; }
        public void setFlightNumber(String flightNumber) { this.flightNumber = flightNumber; }

        public String getAirline() { return airline; }
        public void setAirline(String airline) { this.airline = airline; }

        public String getTerminal() { return terminal; }
        public void setTerminal(String terminal) { this.terminal = terminal; }

        public String getGate() { return gate; }
        public void setGate(String gate) { this.gate = gate; }

        public String getScheduledTime() { return scheduledTime; }
        public void setScheduledTime(String scheduledTime) { this.scheduledTime = scheduledTime; }

        public String getEstimatedTime() { return estimatedTime; }
        public void setEstimatedTime(String estimatedTime) { this.estimatedTime = estimatedTime; }

        public String getCounterCity() { return counterCity; }
        public void setCounterCity(String counterCity) { this.counterCity = counterCity; }

        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
    }
}