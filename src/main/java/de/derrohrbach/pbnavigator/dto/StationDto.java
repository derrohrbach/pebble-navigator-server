package de.derrohrbach.pbnavigator.dto;

public class StationDto {
    private String id;
    private String name;
    private String place;
    private String type;
    private CoordinatesDto coordinates;

    public StationDto() {}

    public StationDto(String id, String name, String place, String type, CoordinatesDto coordinates) {
        this.id = id;
        this.name = name;
        this.place = place;
        this.type = type;
        this.coordinates = coordinates;
    }

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getPlace() { return place; }
    public void setPlace(String place) { this.place = place; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public CoordinatesDto getCoordinates() { return coordinates; }
    public void setCoordinates(CoordinatesDto coordinates) { this.coordinates = coordinates; }
}