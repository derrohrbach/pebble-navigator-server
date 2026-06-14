package de.derrohrbach.pbnavigator.dto;

public class StopDto {
    private String id;
    private String name;
    private String place;
    private String arrivalTime;
    private String departureTime;

    public StopDto() {}

    public StopDto(String id, String name, String place, String arrivalTime, String departureTime) {
        this.id = id;
        this.name = name;
        this.place = place;
        this.arrivalTime = arrivalTime;
        this.departureTime = departureTime;
    }

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getPlace() { return place; }
    public void setPlace(String place) { this.place = place; }

    public String getArrivalTime() { return arrivalTime; }
    public void setArrivalTime(String arrivalTime) { this.arrivalTime = arrivalTime; }

    public String getDepartureTime() { return departureTime; }
    public void setDepartureTime(String departureTime) { this.departureTime = departureTime; }
}