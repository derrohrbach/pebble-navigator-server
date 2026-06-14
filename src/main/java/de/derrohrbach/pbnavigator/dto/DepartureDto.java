package de.derrohrbach.pbnavigator.dto;

public class DepartureDto {
    private String plannedTime;
    private String predictedTime;
    private Long delayMinutes;
    private LineDto line;
    private String destination;
    private String platform;
    private String message;

    public DepartureDto() {}

    // Getters and Setters
    public String getPlannedTime() { return plannedTime; }
    public void setPlannedTime(String plannedTime) { this.plannedTime = plannedTime; }

    public String getPredictedTime() { return predictedTime; }
    public void setPredictedTime(String predictedTime) { this.predictedTime = predictedTime; }

    public Long getDelayMinutes() { return delayMinutes; }
    public void setDelayMinutes(Long delayMinutes) { this.delayMinutes = delayMinutes; }

    public LineDto getLine() { return line; }
    public void setLine(LineDto line) { this.line = line; }

    public String getDestination() { return destination; }
    public void setDestination(String destination) { this.destination = destination; }

    public String getPlatform() { return platform; }
    public void setPlatform(String platform) { this.platform = platform; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    // Nur bei Trip-Abfragen (via) gesetzt:
    private String plannedArrival;
    private String predictedArrival;
    private String arrivalPlatform;

    public String getPlannedArrival() { return plannedArrival; }
    public void setPlannedArrival(String plannedArrival) { this.plannedArrival = plannedArrival; }

    public String getPredictedArrival() { return predictedArrival; }
    public void setPredictedArrival(String predictedArrival) { this.predictedArrival = predictedArrival; }

    public String getArrivalPlatform() { return arrivalPlatform; }
    public void setArrivalPlatform(String arrivalPlatform) { this.arrivalPlatform = arrivalPlatform; }
}