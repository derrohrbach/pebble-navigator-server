package de.derrohrbach.pbnavigator.dto;

import java.util.List;

public class DeparturesResponseDto {
    private String status;
    private long timestamp;
    private String error;
    private List<StationDeparturesDto> stations;
    private int totalDepartures;
    // Nur bei Trip-Abfragen (via) gesetzt:
    private Boolean directOnly;
    private StationDto to;

    public DeparturesResponseDto() {}

    // Getters and Setters
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }

    public String getError() { return error; }
    public void setError(String error) { this.error = error; }

    public List<StationDeparturesDto> getStations() { return stations; }
    public void setStations(List<StationDeparturesDto> stations) { this.stations = stations; }

    public int getTotalDepartures() { return totalDepartures; }
    public void setTotalDepartures(int totalDepartures) { this.totalDepartures = totalDepartures; }

    public Boolean getDirectOnly() { return directOnly; }
    public void setDirectOnly(Boolean directOnly) { this.directOnly = directOnly; }

    public StationDto getTo() { return to; }
    public void setTo(StationDto to) { this.to = to; }
}
