package com.aerotrack.service;

import com.aerotrack.client.AviationStackClient;
import com.aerotrack.dto.AirportBoardDto;
import com.aerotrack.dto.FlightResponseDto;
import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Map;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

@Service
public class FlightService {

    private final AviationStackClient aviationStackClient;

    private static final List<MockAirport> AIRPORT_POOL = new ArrayList<>();

    static {
        AIRPORT_POOL.add(new MockAirport("BOM", "Chhatrapati Shivaji International Airport", "Mumbai", 19.0896, 72.8656));
        AIRPORT_POOL.add(new MockAirport("DEL", "Indira Gandhi International Airport", "Delhi", 28.5562, 77.1000));
        AIRPORT_POOL.add(new MockAirport("CCU", "Netaji Subhash Chandra Bose Airport", "Kolkata", 22.6520, 88.4467));
        AIRPORT_POOL.add(new MockAirport("BLR", "Kempegowda International Airport", "Bengaluru", 13.1986, 77.7066));
        AIRPORT_POOL.add(new MockAirport("MAA", "Chennai International Airport", "Chennai", 12.9941, 80.1709));
        AIRPORT_POOL.add(new MockAirport("PNQ", "Pune International Airport", "Pune", 18.5822, 73.9197));
        AIRPORT_POOL.add(new MockAirport("DXB", "Dubai International Airport", "Dubai", 25.2532, 55.3657));
        AIRPORT_POOL.add(new MockAirport("SIN", "Singapore Changi Airport", "Singapore", 1.3644, 103.9915));
        AIRPORT_POOL.add(new MockAirport("LHR", "London Heathrow Airport", "London", 51.4700, -0.4543));
        AIRPORT_POOL.add(new MockAirport("JFK", "John F. Kennedy International Airport", "New York", 40.6413, -73.7781));
        AIRPORT_POOL.add(new MockAirport("HND", "Tokyo Haneda Airport", "Tokyo", 35.5494, 139.7798));
        AIRPORT_POOL.add(new MockAirport("IXB", "Bagdogra Airport", "Siliguri", 26.6812, 88.3286));
    }

    public FlightService(AviationStackClient aviationStackClient) {
        this.aviationStackClient = aviationStackClient;
    }

    @Cacheable(value = "flights", key = "#flightNumber")
    public FlightResponseDto fetchLiveFlightFromApi(String flightNumber) {
        String cleanFlightNum = flightNumber.toUpperCase().trim().replace(" ", "");
        try {
            JsonNode root = aviationStackClient.fetchLiveFlightData(cleanFlightNum);
            if (root != null && !root.has("error") && !root.path("data").isEmpty()) {
                System.out.println(">>> [SERVICE] Live upstream data parsed for: " + cleanFlightNum);
                return mapToDto(root.path("data").get(0), cleanFlightNum);
            }
        } catch (Exception e) {
            System.err.println(">>> [SERVICE] Upstream API error/unreachable for " + cleanFlightNum);
        }

        return null;
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
            System.err.println(">>> [SERVICE] Timetables restricted or unavailable for " + cleanIata);
        }

        return null;
    }

    private FlightResponseDto mapToDto(JsonNode flightData, String flightNumber) {
        JsonNode departure = flightData.path("departure");
        JsonNode arrival = flightData.path("arrival");
        JsonNode live = flightData.path("live");

        FlightResponseDto dto = new FlightResponseDto();
        dto.setFlightNumber(flightNumber);
        dto.setAirline(flightData.path("airline").path("name").asText(parseAirlinePrefix(flightNumber)));
        dto.setStatus(flightData.path("flight_status").asText("active"));

        dto.setOriginIata(departure.path("iata").asText("DEL"));
        dto.setOriginAirport(departure.path("airport").asText("Indira Gandhi International Airport"));
        dto.setOriginCity(extractCityName(dto.getOriginIata(), dto.getOriginAirport()));

        dto.setDestinationIata(arrival.path("iata").asText("BOM"));
        dto.setDestinationAirport(arrival.path("airport").asText("Chhatrapati Shivaji International Airport"));
        dto.setDestinationCity(extractCityName(dto.getDestinationIata(), dto.getDestinationAirport()));

        String depTime = departure.path("actual").asText(departure.path("scheduled").asText(""));
        String arrTime = arrival.path("estimated").asText(arrival.path("scheduled").asText(""));

        if (depTime.isEmpty()) depTime = ZonedDateTime.now().minusMinutes(30).format(DateTimeFormatter.ISO_OFFSET_DATE_TIME);
        if (arrTime.isEmpty()) arrTime = ZonedDateTime.parse(depTime).plusHours(2).plusMinutes(15).format(DateTimeFormatter.ISO_OFFSET_DATE_TIME);

        dto.setScheduledDeparture(depTime);
        dto.setEstimatedArrival(arrTime);
        dto.setDestinationLatitude(arrival.path("latitude").asDouble(19.0896));
        dto.setDestinationLongitude(arrival.path("longitude").asDouble(72.8656));

        if (!live.isMissingNode() && !live.isNull() && live.path("latitude").asDouble(0.0) != 0.0) {
            dto.setCurrentLatitude(live.path("latitude").asDouble());
            dto.setCurrentLongitude(live.path("longitude").asDouble());
            dto.setAltitude(live.path("altitude").asDouble(8000.0));
            dto.setSpeed(live.path("speed").asDouble(720.0));
            dto.setHeading(live.path("direction").asDouble(180.0));
        } else {
            dto.setCurrentLatitude(dto.getDestinationLatitude());
            dto.setCurrentLongitude(dto.getDestinationLongitude());
            dto.setAltitude(0.0);
            dto.setSpeed(0.0);
            dto.setHeading(0.0);
        }
        return dto;
    }

    private List<AirportBoardDto.ScheduleItem> parseScheduleItems(JsonNode dataArray, boolean isArrival) {
        List<AirportBoardDto.ScheduleItem> items = new ArrayList<>();
        if (dataArray != null && dataArray.isArray()) {
            for (JsonNode node : dataArray) {
                AirportBoardDto.ScheduleItem item = new AirportBoardDto.ScheduleItem();
                String flightIata = node.path("flight").path("iata").asText("");
                item.setFlightNumber(flightIata.isEmpty() ? "N/A" : flightIata);
                item.setAirline(node.path("airline").path("name").asText(parseAirlinePrefix(flightIata)));

                JsonNode pointNode = isArrival ? node.path("arrival") : node.path("departure");
                item.setTerminal(pointNode.path("terminal").asText("-"));
                item.setGate(pointNode.path("gate").asText("-"));
                item.setScheduledTime(pointNode.path("scheduled").asText(""));
                item.setEstimatedTime(pointNode.path("estimated").asText(""));

                JsonNode counterNode = isArrival ? node.path("departure") : node.path("arrival");
                String counterIata = counterNode.path("iata").asText("N/A");
                item.setCounterCity(extractCityName(counterIata, counterNode.path("airport").asText()) + " (" + counterIata.toUpperCase() + ")");
                item.setStatus(node.path("flight_status").asText("scheduled"));
                items.add(item);
            }
        }
        return items;
    }

    private String parseAirlinePrefix(String flightNumber) {
        if (flightNumber == null || flightNumber.length() < 2) return "Global Carrier";
        String code = flightNumber.substring(0, 2).toUpperCase();
        switch (code) {
            case "6E": return "IndiGo";
            case "AI": return "Air India";
            case "QP": return "Akasa Air";
            case "SG": return "SpiceJet";
            case "AA": return "American Airlines";
            case "DL": return "Delta Air Lines";
            case "UA": return "United Airlines";
            case "LH": return "Lufthansa";
            case "BA": return "British Airways";
            case "EK": return "Emirates";
            case "SQ": return "Singapore Airlines";
            case "O3": return "SF Airlines";
            default:
                String alpha = flightNumber.replaceAll("[^A-Za-z]", "");
                return alpha.isEmpty() ? "International Airways" : alpha + " Air";
        }
    }

    private String extractCityName(String iataCode, String airportName) {
        if (iataCode == null || iataCode.isEmpty()) return "Unknown City";
        for (MockAirport airport : AIRPORT_POOL) {
            if (airport.iata.equalsIgnoreCase(iataCode)) return airport.city;
        }
        if (airportName != null && !airportName.isEmpty() && !airportName.equalsIgnoreCase("Unknown Airport")) {
            return airportName.split(" ")[0];
        }
        return "City (" + iataCode.toUpperCase() + ")";
    }

    private static class MockAirport {
        String iata;
        String name;
        String city;
        double lat;
        double lon;

        MockAirport(String iata, String name, String city, double lat, double lon) {
            this.iata = iata;
            this.name = name;
            this.city = city;
            this.lat = lat;
            this.lon = lon;
        }
    }

    public List<FlightResponseDto> fetchActualLiveAirspace() {
        List<FlightResponseDto> realPlanes = new ArrayList<>();
        RestTemplate restTemplate = new RestTemplate();

        double minLat = 8.0;
        double maxLat = 30.0;
        double minLon = 70.0;
        double maxLon = 90.0;

        String url = String.format(
                "https://opensky-network.org/api/states/all?lamin=%f&lomin=%f&lamax=%f&lomax=%f",
                minLat, minLon, maxLat, maxLon
        );

        try {
            String username = "navnit07";
            String password = "OpenSky@07";

            String authStr = username + ":" + password;
            String base64Auth = Base64.getEncoder().encodeToString(authStr.getBytes());

            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Basic " + base64Auth);
            HttpEntity<String> requestEntity = new HttpEntity<>(headers);

            ResponseEntity<Map> responseEntity = restTemplate.exchange(url, HttpMethod.GET, requestEntity, Map.class);
            Map<String, Object> response = responseEntity.getBody();

            if (response != null && response.containsKey("states") && response.get("states") != null) {
                List<List<Object>> states = (List<List<Object>>) response.get("states");

                for (List<Object> flightState : states) {
                    if (flightState.get(5) != null && flightState.get(6) != null) {

                        if (flightState.get(8) != null && Boolean.parseBoolean(flightState.get(8).toString())) {
                            continue;
                        }

                        String callsign = flightState.get(1) != null ? flightState.get(1).toString().trim() : "";

                        if (callsign.isEmpty() || callsign.equalsIgnoreCase("UNK")) {
                            continue;
                        }

                        FlightResponseDto plane = new FlightResponseDto();
                        plane.setFlightNumber(callsign);
                        plane.setAirline("Original ADS-B Feed");
                        plane.setCurrentLongitude(Double.parseDouble(flightState.get(5).toString()));
                        plane.setCurrentLatitude(Double.parseDouble(flightState.get(6).toString()));

                        double heading = flightState.get(10) != null ? Double.parseDouble(flightState.get(10).toString()) : 0.0;
                        plane.setHeading(heading);

                        double altMeters = flightState.get(7) != null ? Double.parseDouble(flightState.get(7).toString()) : 0.0;
                        plane.setAltitude(altMeters * 3.28084);

                        double speedMs = flightState.get(9) != null ? Double.parseDouble(flightState.get(9).toString()) : 0.0;
                        plane.setSpeed(speedMs * 1.94384);

                        plane.setStatus("en-route");
                        realPlanes.add(plane);
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("📡 OpenSky live connection trace error: " + e.getMessage());
        }

        return realPlanes;
    }
}