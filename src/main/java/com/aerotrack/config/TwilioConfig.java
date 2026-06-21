package com.aerotrack.config;

import com.twilio.Twilio;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
public class TwilioConfig {

    @Value("${twilio.account-sid}")
    private String accountSid;

    @Value("${twilio.auth-token}")
    private String authToken;

    @PostConstruct
    public void initTwilio() {
        // Standard structural validation before calling initialization routine
        if (accountSid == null || accountSid.isBlank() || "YOUR_TWILIO_SID".equals(accountSid)) {
            System.err.println("WARNING: Twilio Account SID is missing or unconfigured. SMS alerts will fail.");
            return;
        }
        
        // Connect automatically to the Twilio network gateway
        Twilio.init(accountSid, authToken);
        System.out.println("SUCCESS: Twilio SDK successfully initialized.");
    }
}