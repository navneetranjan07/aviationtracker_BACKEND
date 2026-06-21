package com.aerotrack.service;

import com.aerotrack.client.AviationStackClient;
import com.aerotrack.dto.AirportBoardDto;
import com.aerotrack.dto.FlightResponseDto;
import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@Service
public class FlightService {

    private final AviationStackClient aviationStackClient;
    private final Random random = new Random();

    public FlightService(AviationStackClient aviationStackClient) {
        this.aviationStackClient = aviationStackClient;
    }

    @Cacheable(value = "flights", key = "#flightNumber")
    public FlightResponseDto getFlightDetails(String flightNumber) {
        String cleanFlightNum = flightNumber.toUpperCase().trim();
        try {
            JsonNode root = aviationStackClient.fetchLiveFlightData(cleanFlightNum);
            if (root != null && !root.has("error") && !root.path("data").isEmpty()) {
                System.out.println(">>> [SERVICE] Live data found via AviationStack for: " + cleanFlightNum);
                return mapToDto(root.path("data").get(0), cleanFlightNum);
            }
        } catch (Exception e) {
            System.err.println(">>> [SERVICE] Upstream API Error for " + cleanFlightNum + ". Activating Simulator fallback.");
        }

        // Fallback: Return robust simulated flight data instead of breaking the frontend
        return generateMockFlight(cleanFlightNum);
    }

    public AirportBoardDto getAirportBoard(String airportIata) {
        String cleanIata = airportIata.toUpperCase().trim();
        try {
            JsonNode departuresRoot = aviationStackClient.fetchAirportBoardData(cleanIata, "dep_iata");
            JsonNode arrivalsRoot = aviationStackClient.fetchAirportBoardData(cleanIata, "arr_iata");

            if (departuresRoot != null && !departuresRoot.has("error") && arrivalsRoot != null && !arrivalsRoot.has("error")) {
                AirportBoardDto board = new AirportBoardDto();
                board.setAirportCode(cleanIata);
                board.setDepartures(parseScheduleItems(departuresRoot.path("data"), false));
                board.setArrivals(parseScheduleItems(arrivalsRoot.path("data"), true));
                return board;
            }
        } catch (Exception e) {
            System.err.println(">>> [SERVICE] Upstream Board Data Blocked for " + cleanIata + ". Activating Timetable generator.");
        }

        // Fallback: Generate full beautiful dashboard timetables dynamically
        return generateMockBoard(cleanIata);
    }

    private FlightResponseDto mapToDto(JsonNode flightData, String flightNumber) {
        JsonNode departure = flightData.path("departure");
        JsonNode arrival = flightData.path("arrival");
        JsonNode live = flightData.path("live");

        FlightResponseDto dto = new FlightResponseDto();
        dto.setFlightNumber(flightNumber);
        dto.setAirline(flightData.path("airline").path("name").asText("Unknown Airline"));
        dto.setStatus(flightData.path("flight_status").asText("active"));
        dto.setOriginIata(departure.path("iata").asText("DEL"));
        dto.setOriginAirport(departure.path("airport").asText("Indira Gandhi International Airport"));
        dto.setOriginCity(extractCityName(departure, dto.getOriginIata()));
        dto.setDestinationIata(arrival.path("iata").asText("BOM"));
        dto.setDestinationAirport(arrival.path("airport").asText("Chhatrapati Shivaji International Airport"));
        dto.setDestinationCity(extractCityName(arrival, dto.getDestinationIata()));
        dto.setScheduledDeparture(departure.path("scheduled").asText("2026-06-21T12:10:00+05:30"));
        dto.setEstimatedArrival(arrival.path("estimated").asText("2026-06-21T15:15:00+05:30"));
        dto.setDestinationLatitude(arrival.path("latitude").asDouble(19.0896));
        dto.setDestinationLongitude(arrival.path("longitude").asDouble(72.8656));

        if (!live.isMissingNode() && !live.isNull()) {
            dto.setCurrentLatitude(live.path("latitude").asDouble(19.1500));
            dto.setCurrentLongitude(live.path("longitude").asDouble(72.9000));
            dto.setAltitude(live.path("altitude").asDouble(7500.0));
            dto.setSpeed(live.path("speed").asDouble(650.0));
            dto.setHeading(live.path("direction").asDouble(170.0));
        } else {
            dto.setCurrentLatitude(19.0896);
            dto.setCurrentLongitude(72.8656);
            dto.setAltitude(0.0);
            dto.setSpeed(0.0);
            dto.setHeading(0.0);
        }
        return dto;
    }

    private FlightResponseDto generateMockFlight(String flightNumber) {
        FlightResponseDto dto = new FlightResponseDto();
        dto.setFlightNumber(flightNumber);

        // Handle branding based on standard airline prefixes
        if (flightNumber.startsWith("6E")) {
            dto.setAirline("IndiGo");
        } else if (flightNumber.startsWith("QP")) {
            dto.setAirline("Akasa Air");
        } else {
            dto.setAirline("Air India");
        }

        dto.setStatus("active");
        dto.setOriginIata("IXB");
        dto.setOriginAirport("Bagdogra Airport");
        dto.setOriginCity("Siliguri");
        dto.setDestinationIata("BOM");
        dto.setDestinationAirport("Chhatrapati Shivaji International Airport");
        dto.setDestinationCity("Mumbai");

        // Set realistic operational times matching standard layouts
        dto.setScheduledDeparture("2026-06-21T12:10:00+05:30");
        dto.setEstimatedArrival("2026-06-21T15:15:00+05:30");

        // Coordinates for Map rendering components
        dto.setDestinationLatitude(19.0896);
        dto.setDestinationLongitude(72.8656);
        dto.setCurrentLatitude(19.2964);
        dto.setCurrentLongitude(73.0483);
        dto.setAltitude(7450.0);
        dto.setSpeed(680.0);
        dto.setHeading(195.0);

        return dto;
    }

    private AirportBoardDto generateMockBoard(String airportIata) {
        AirportBoardDto board = new AirportBoardDto();
        board.setAirportCode(airportIata);

        List<AirportBoardDto.ScheduleItem> deps = new ArrayList<>();
        List<AirportBoardDto.ScheduleItem> arrs = new ArrayList<>();

        String[] airlines = {"IndiGo", "Akasa Air", "Air India", "SpiceJet"};
        String[] codes = {"6E", "QP", "AI", "SG"};
        String[] cities = {"Mumbai (BOM)", "Delhi (DEL)", "Bengaluru (BLR)", "Pune (PNQ)"};
        String[] statuses = {"scheduled", "boarding", "delayed", "estimated"};

        // Generate 5 dynamic departures
        for (int i = 0; i < 5; i++) {
            int idx = (i + random.nextInt(4)) % 4;
            AirportBoardDto.ScheduleItem item = new AirportBoardDto.ScheduleItem();
            item.setFlightNumber(codes[idx] + (2000 + random.nextInt(1000)));
            item.setAirline(airlines[idx]);
            item.setTerminal("T" + (1 + random.nextInt(2)));
            item.setGate("A" + (1 + random.nextInt(15)));
            item.setScheduledTime("2026-06-21T19:" + (10 + i * 10) + ":00+05:30");
            item.setEstimatedTime("2026-06-21T19:" + (15 + i * 10) + ":00+05:30");
            item.setCounterCity(cities[idx]);
            item.setStatus(statuses[random.nextInt(statuses.length)]);
            deps.add(item);
        }

        // Generate 5 dynamic arrivals
        for (int i = 0; i < 5; i++) {
            int idx = (i + random.nextInt(4)) % 4;
            AirportBoardDto.ScheduleItem item = new AirportBoardDto.ScheduleItem();
            item.setFlightNumber(codes[idx] + (3000 + random.nextInt(1000)));
            item.setAirline(airlines[idx]);
            item.setTerminal("T" + (1 + random.nextInt(2)));
            item.setGate("B" + (1 + random.nextInt(15)));
            item.setScheduledTime("2026-06-21T20:" + (5 + i * 12) + ":00+05:30");
            item.setEstimatedTime("2026-06-21T20:" + (5 + i * 12) + ":00+05:30");
            item.setCounterCity(cities[idx]);
            item.setStatus("estimated");
            arrs.add(item);
        }

        board.setDepartures(deps);
        board.setArrivals(arrs);
        return board;
    }

    private List<AirportBoardDto.ScheduleItem> parseScheduleItems(JsonNode dataArray, boolean isArrival) {
        List<AirportBoardDto.ScheduleItem> items = new ArrayList<>();
        if (dataArray != null && dataArray.isArray()) {
            for (JsonNode node : dataArray) {
                AirportBoardDto.ScheduleItem item = new AirportBoardDto.ScheduleItem();
                String flightIata = node.path("flight").path("iata").asText("");
                item.setFlightNumber(flightIata.isEmpty() ? "N/A" : flightIata);
                item.setAirline(node.path("airline").path("name").asText("Unknown Airline"));

                JsonNode pointNode = isArrival ? node.path("arrival") : node.path("departure");
                item.setTerminal(pointNode.path("terminal").asText("-"));
                item.setGate(pointNode.path("gate").asText("-"));
                item.setScheduledTime(pointNode.path("scheduled").asText(""));
                item.setEstimatedTime(pointNode.path("estimated").asText(""));

                JsonNode counterNode = isArrival ? node.path("departure") : node.path("arrival");
                String counterIata = counterNode.path("iata").asText("N/A");
                item.setCounterCity(extractCityName(counterNode, counterIata) + " (" + counterIata.toUpperCase() + ")");
                item.setStatus(node.path("flight_status").asText("scheduled"));
                items.add(item);
            }
        }
        return items;
    }

    private String extractCityName(JsonNode node, String iataCode) {
        if (iataCode == null || iataCode.isEmpty() || iataCode.equalsIgnoreCase("N/A")) return "Unknown";
        switch (iataCode.toUpperCase()) {
            case "BOM": return "Mumbai";
            case "DEL": return "Delhi";
            case "PNQ": return "Pune";
            case "BLR": return "Bengaluru";
            default:
                String airport = node.path("airport").asText("");
                if (!airport.isEmpty() && !airport.equalsIgnoreCase("Unknown Airport")) {
                    return airport.split(" ")[0];
                }
                return "Unknown City";
        }
    }
}