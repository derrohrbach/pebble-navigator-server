package de.derrohrbach.pbnavigator.service;

import de.schildbach.pte.DbProvider;
import de.schildbach.pte.dto.*;
import de.derrohrbach.pbnavigator.dto.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

public class DepartureService {
    private static final Logger logger = LoggerFactory.getLogger(DepartureService.class);
    private final DbProvider dbProvider;

    public DepartureService() {
        this.dbProvider = new DbProvider();
    }

    /**
     * Sucht nach Stationen basierend auf dem Namen
     */
    public List<StationDto> searchStations(String query) {
        try {
            SuggestLocationsResult result = dbProvider.suggestLocations(query, 
                EnumSet.of(LocationType.STATION), 10);
            
            if (result.status != SuggestLocationsResult.Status.OK) {
                logger.warn("Station search failed with status: {}", result.status);
                return Collections.emptyList();
            }

            return result.getLocations().stream()
                .map(this::mapToStationDto)
                .collect(Collectors.toList());

        } catch (IOException e) {
            logger.error("Error searching for stations: {}", query, e);
            return Collections.emptyList();
        }
    }

    /**
     * Holt Abfahrten für eine Station
     */
    public DeparturesResponseDto getDepartures(String stationId, int maxDepartures) {
        DeparturesResponseDto response = new DeparturesResponseDto();
        response.setTimestamp(System.currentTimeMillis());

        try {
            Date now = new Date();
            QueryDeparturesResult result = dbProvider.queryDepartures(stationId, now,
                Math.min(maxDepartures, 50), false);

            response.setStatus(result.status.toString());

            if (result.status != QueryDeparturesResult.Status.OK) {
                response.setError("Could not fetch departures: " + result.status);
                response.setStations(Collections.emptyList());
                response.setTotalDepartures(0);
                return response;
            }

            List<StationDeparturesDto> stations = result.stationDepartures.stream()
                .map(sd -> mapToStationDeparturesDto(sd, null, maxDepartures))
                .collect(Collectors.toList());

            response.setStations(stations);
            response.setTotalDepartures(
                stations.stream()
                    .mapToInt(station -> station.getDepartures().size())
                    .sum()
            );

            return response;

        } catch (IOException e) {
            logger.error("Error fetching departures for station: {}", stationId, e);
            response.setStatus("ERROR");
            response.setError("Service temporarily unavailable");
            response.setStations(Collections.emptyList());
            response.setTotalDepartures(0);
            return response;
        }
    }

    /**
     * Sucht direkte Verbindungen (ohne Umstieg) von fromId nach toId.
     * Gibt ein DeparturesResponseDto zurück, das mit dem normalen Abfahrts-Format kompatibel ist.
     * Zusätzliche Keys: directOnly=true, to=Zielbahnhof
     */
    public DeparturesResponseDto getDirectTrips(String fromId, String toId, int max) {
        DeparturesResponseDto response = new DeparturesResponseDto();
        response.setTimestamp(System.currentTimeMillis());

        try {
            Location from = new Location(LocationType.STATION, fromId);
            Location to = new Location(LocationType.STATION, toId);
            QueryTripsResult result = dbProvider.queryTrips(from, null, to, new Date(), true, null);

            response.setStatus(result.status.toString());

            if (result.status != QueryTripsResult.Status.OK) {
                logger.warn("Trip query failed with status: {}", result.status);
                response.setError("Could not fetch trips: " + result.status);
                response.setStations(Collections.emptyList());
                response.setTotalDepartures(0);
                return response;
            }

            List<DepartureDto> departures = result.trips.stream()
                .filter(trip -> {
                    Integer changes = trip.getNumChanges();
                    return changes != null && changes == 0;
                })
                .limit(max)
                .map(this::mapTripToDepartureDto)
                .collect(Collectors.toList());

            // Abfahrtsstation aus dem ersten Ergebnis ableiten (oder Fallback per ID)
            StationDto fromStation = (!result.trips.isEmpty() && result.trips.get(0).getFirstPublicLeg() != null)
                ? mapToStationDto(result.trips.get(0).getFirstPublicLeg().departureStop.location)
                : new StationDto(fromId, null, null, "STATION", null);

            StationDto toStation = (!result.trips.isEmpty() && result.trips.get(0).getFirstPublicLeg() != null)
                ? mapToStationDto(result.trips.get(0).getFirstPublicLeg().arrivalStop.location)
                : new StationDto(toId, null, null, "STATION", null);

            StationDeparturesDto stationDto = new StationDeparturesDto();
            stationDto.setId(fromStation.getId());
            stationDto.setName(fromStation.getName());
            stationDto.setPlace(fromStation.getPlace());
            stationDto.setDepartures(departures);

            response.setStations(Collections.singletonList(stationDto));
            response.setTotalDepartures(departures.size());
            response.setDirectOnly(true);
            response.setTo(toStation);
            return response;

        } catch (IOException e) {
            logger.error("Error querying trips from {} to {}", fromId, toId, e);
            response.setStatus("ERROR");
            response.setError("Service temporarily unavailable");
            response.setStations(Collections.emptyList());
            response.setTotalDepartures(0);
            return response;
        }
    }

    private DepartureDto mapTripToDepartureDto(Trip trip) {
        DepartureDto dto = new DepartureDto();

        Trip.Public leg = trip.getFirstPublicLeg();
        if (leg == null) return dto;

        Stop depStop = leg.departureStop;
        Stop arrStop = leg.arrivalStop;

        // Abfahrt (wie normales DepartureDto)
        if (depStop.plannedDepartureTime != null)
            dto.setPlannedTime(depStop.plannedDepartureTime.toInstant().toString());
        if (depStop.predictedDepartureTime != null) {
            dto.setPredictedTime(depStop.predictedDepartureTime.toInstant().toString());
            if (depStop.plannedDepartureTime != null) {
                long delayMs = depStop.predictedDepartureTime.getTime() - depStop.plannedDepartureTime.getTime();
                dto.setDelayMinutes(delayMs / (1000 * 60));
            }
        }
        if (depStop.predictedDeparturePosition != null)
            dto.setPlatform(depStop.predictedDeparturePosition.name);
        else if (depStop.plannedDeparturePosition != null)
            dto.setPlatform(depStop.plannedDeparturePosition.name);

        // Ziel (Endbahnhof des Zuges)
        if (leg.destination != null)
            dto.setDestination(leg.destination.name);
        else if (arrStop.location != null)
            dto.setDestination(arrStop.location.name);

        // Linie
        if (leg.line != null) {
            StyleDto styleDto = leg.line.style != null ? mapToStyleDto(leg.line.style) : null;
            dto.setLine(new LineDto(leg.line.id, leg.line.name, leg.line.label,
                leg.line.product != null ? leg.line.product.toString() : null, styleDto));
        }

        // Ankunft am Zielbahnhof (neue optionale Keys)
        if (arrStop.plannedArrivalTime != null)
            dto.setPlannedArrival(arrStop.plannedArrivalTime.toInstant().toString());
        if (arrStop.predictedArrivalTime != null)
            dto.setPredictedArrival(arrStop.predictedArrivalTime.toInstant().toString());
        if (arrStop.predictedArrivalPosition != null)
            dto.setArrivalPlatform(arrStop.predictedArrivalPosition.name);
        else if (arrStop.plannedArrivalPosition != null)
            dto.setArrivalPlatform(arrStop.plannedArrivalPosition.name);

        return dto;
    }

    private StationDto mapToStationDto(Location location) {
        CoordinatesDto coordinates = null;
        if (location.hasCoord()) {
            coordinates = new CoordinatesDto(
                location.getLatAsDouble(), 
                location.getLonAsDouble()
            );
        }

        return new StationDto(
            location.id,
            location.name,
            location.place,
            location.type.toString(),
            coordinates
        );
    }

    private StationDeparturesDto mapToStationDeparturesDto(StationDepartures stationDeps,
            String viaLower, int maxDepartures) {
        StationDeparturesDto dto = new StationDeparturesDto();
        dto.setId(stationDeps.location.id);
        dto.setName(stationDeps.location.name);
        dto.setPlace(stationDeps.location.place);

        List<DepartureDto> departures = stationDeps.departures.stream()
            .filter(dep -> {
                if (viaLower == null) return true;
                if (dep.destination == null) return false;
                String dest = "";
                if (dep.destination.name != null) dest += dep.destination.name.toLowerCase();
                if (dep.destination.place != null) dest += " " + dep.destination.place.toLowerCase();
                return dest.contains(viaLower);
            })
            .limit(maxDepartures)
            .map(this::mapToDepartureDto)
            .collect(Collectors.toList());

        dto.setDepartures(departures);
        return dto;
    }

    private DepartureDto mapToDepartureDto(Departure departure) {
        DepartureDto dto = new DepartureDto();
        
        // Zeiten
        if (departure.plannedTime != null) {
            dto.setPlannedTime(departure.plannedTime.toInstant().toString());
        }
        
        if (departure.predictedTime != null) {
            dto.setPredictedTime(departure.predictedTime.toInstant().toString());
            
            // Verspätung berechnen
            if (departure.plannedTime != null) {
                long delayMs = departure.predictedTime.getTime() - departure.plannedTime.getTime();
                dto.setDelayMinutes(delayMs / (1000 * 60));
            }
        }

        // Linie
        if (departure.line != null) {
            StyleDto styleDto = null;
            if (departure.line.style != null) {
                styleDto = mapToStyleDto(departure.line.style);
            }
            
            LineDto lineDto = new LineDto(
                departure.line.id,
                departure.line.name,
                departure.line.label,
                departure.line.product != null ? departure.line.product.toString() : null,
                styleDto
            );
            dto.setLine(lineDto);
        }

        // Richtung
        if (departure.destination != null) {
            dto.setDestination(departure.destination.name);
        }

        // Gleis
        if (departure.position != null) {
            dto.setPlatform(departure.position.name);
        }

        // Nachrichten
        if (departure.message != null && !departure.message.isEmpty()) {
            dto.setMessage(departure.message);
        }

        return dto;
    }

    private StyleDto mapToStyleDto(de.schildbach.pte.dto.Style style) {
        if (style == null) return null;
        
        return new StyleDto(
            style.shape != null ? style.shape.toString() : null,
            String.format("#%06X", style.backgroundColor & 0xFFFFFF),
            style.backgroundColor2 != 0 ? String.format("#%06X", style.backgroundColor2 & 0xFFFFFF) : null,
            String.format("#%06X", style.foregroundColor & 0xFFFFFF),
            style.borderColor != 0 ? String.format("#%06X", style.borderColor & 0xFFFFFF) : null
        );
    }
}