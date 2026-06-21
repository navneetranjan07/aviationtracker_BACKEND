package com.aerotrack.service;


import com.aerotrack.dto.NotificationRequestDto;
import com.twilio.rest.api.v2010.account.Call;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;
import com.twilio.type.Twiml;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class NotificationService {

    @Value("${twilio.from-number}")
    private String twilioFromNumber;

    public String sendFlightAlert(NotificationRequestDto request) {
        String alertMessage = String.format(
                "AeroTrack Alert! Flight %s is estimated to arrive at its destination shortly. Prepare for landing!",
                request.getFlightNumber()
        );

        if ("CALL".equalsIgnoreCase(request.getNotificationType())) {
            // Fire an automated Text-to-Speech phone call routing thread
            String twimlInstructions = String.format(
                    "<Response><Say voice='alice'>Hello! This is a real-time flight update from AeroTrack. Your monitored flight, %s, is approaching its destination. Goodbye!</Say></Response>", 
                    request.getFlightNumber()
            );

            Call call = Call.creator(
                    new PhoneNumber(request.getPhoneNumber()), // To
                    new PhoneNumber(twilioFromNumber),        // From
                    new Twiml(twimlInstructions)
            ).create();

            return "Voice Call triggered successfully. Twilio Tracking SID: " + call.getSid();
        } else {
            // Default: Send standard SMS notification string
            Message message = Message.creator(
                    new PhoneNumber(request.getPhoneNumber()), // To
                    new PhoneNumber(twilioFromNumber),        // From
                    alertMessage
            ).create();

            return "SMS alert dispatched successfully. Twilio Tracking SID: " + message.getSid();
        }
    }
}