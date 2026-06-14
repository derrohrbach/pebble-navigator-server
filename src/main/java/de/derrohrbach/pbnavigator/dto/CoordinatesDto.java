package de.derrohrbach.pbnavigator.dto;

public class CoordinatesDto {
    private double lat;
    private double lon;

    public CoordinatesDto() {}

    public CoordinatesDto(double lat, double lon) {
        this.lat = lat;
        this.lon = lon;
    }

    // Getters and Setters
    public double getLat() { return lat; }
    public void setLat(double lat) { this.lat = lat; }

    public double getLon() { return lon; }
    public void setLon(double lon) { this.lon = lon; }
}