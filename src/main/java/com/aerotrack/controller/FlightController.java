package com.aerotrack.controller;

import com.aerotrack.dto.AirportBoardDto;
import com.aerotrack.dto.FlightResponseDto;
import com.aerotrack.service.FlightService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/flights")
@CrossOrigin(origins = "*", allowedHeaders = "*")
public class FlightController {

    private final FlightService flightService;

    public FlightController(FlightService flightService) {
        this.flightService = flightService;
    }

    @GetMapping("/{flightNumber}")
    public ResponseEntity<FlightResponseDto> getFlightDetails(@PathVariable String flightNumber) {
        String standardizedFlight = flightNumber.trim().toUpperCase();
        System.out.println(">>> [CONTROLLER] Fetching Flight: " + standardizedFlight);

        try {
            FlightResponseDto response = flightService.getFlightDetails(standardizedFlight);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            System.out.println(">>> [CONTROLLER] Live lookup failed for " + standardizedFlight + ". Engaging fallback simulation matrix...");
            FlightResponseDto fallbackResponse = flightService.generateGlobalAlgorithmicFlight(standardizedFlight);
            return ResponseEntity.ok(fallbackResponse);
        }
    }

    @GetMapping("/board/{airportIata}")
    public ResponseEntity<AirportBoardDto> getAirportBoard(
            @PathVariable String airportIata,
            @RequestParam(value = "type", required = false) String type) {
        System.out.println(">>> [CONTROLLER] Fetching Airport Board for: " + airportIata + " (Type: " + type + ")");
        AirportBoardDto response = flightService.getAirportBoard(airportIata.toUpperCase().trim());
        return ResponseEntity.ok(response);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleAllExceptions(Exception ex) {
        Map<String, Object> errorBody = new HashMap<>();
        errorBody.put("status", HttpStatus.INTERNAL_SERVER_ERROR.value());
        errorBody.put("message", ex.getMessage());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorBody);
    }
}