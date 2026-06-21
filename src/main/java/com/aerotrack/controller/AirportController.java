package com.aerotrack.controller;

import com.aerotrack.dto.AirportBoardDto;
import com.aerotrack.service.FlightService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/airports")
public class AirportController {

    private final FlightService flightService;

    public AirportController(FlightService flightService) {
        this.flightService = flightService;
    }

     // Fetches the live departure and arrival boards for a given airport code.
    @GetMapping("/{iataCode}/board")
    public ResponseEntity<AirportBoardDto> getAirportBoard(@PathVariable String iataCode) {
        AirportBoardDto response = flightService.getAirportBoard(iataCode.toUpperCase());
        return ResponseEntity.ok(response);
    }
}