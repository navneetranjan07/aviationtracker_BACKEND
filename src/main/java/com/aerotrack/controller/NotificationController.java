package com.aerotrack.controller;

import com.aerotrack.dto.NotificationRequestDto;
import com.aerotrack.service.NotificationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

     // Triggers an automated flight alert dispatch rule via Twilio.
    @PostMapping("/alert")
    public ResponseEntity<Map<String, String>> triggerFlightAlert(@RequestBody NotificationRequestDto requestDto) {
        String statusMessage = notificationService.sendFlightAlert(requestDto);
        return ResponseEntity.ok(Map.of(
                "status", "success",
                "message", statusMessage
        ));
    }
}