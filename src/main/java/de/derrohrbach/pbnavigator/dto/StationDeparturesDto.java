package de.derrohrbach.pbnavigator.dto;

import java.util.List;

public class StationDeparturesDto {
    private String id;
    private String name;
    private String place;
    private List<DepartureDto> departures;

    public StationDeparturesDto() {}

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getPlace() { return place; }
    public void setPlace(String place) { this.place = place; }

    public List<DepartureDto> getDepartures() { return departures; }
    public void setDepartures(List<DepartureDto> departures) { this.departures = departures; }
}