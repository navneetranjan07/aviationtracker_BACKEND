package com.aerotrack.dto;

public class NotificationRequestDto {
    private String flightNumber;
    private String phoneNumber;
    private Integer minutesBeforeLanding;
    private String notificationType;

    // Default Constructor
    public NotificationRequestDto() {}

    // Getters and Setters
    public String getFlightNumber() { return flightNumber; }
    public void setFlightNumber(String flightNumber) { this.flightNumber = flightNumber; }

    public String getPhoneNumber() { return phoneNumber; }
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }

    public Integer getMinutesBeforeLanding() { return minutesBeforeLanding; }
    public void setMinutesBeforeLanding(Integer minutesBeforeLanding) { this.minutesBeforeLanding = minutesBeforeLanding; }

    public String getNotificationType() { return notificationType; }
    public void setNotificationType(String notificationType) { this.notificationType = notificationType; }
}