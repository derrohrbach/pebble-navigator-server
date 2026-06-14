package de.derrohrbach.pbnavigator;

import io.javalin.Javalin;
import io.javalin.http.Context;
import de.derrohrbach.pbnavigator.service.DepartureService;
import de.derrohrbach.pbnavigator.dto.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;

public class Main {
    private static final Logger logger = LoggerFactory.getLogger(Main.class);
    private static final DepartureService departureService = new DepartureService();

    public static void main(String[] args) {
        String apiKey = System.getenv("API_KEY");
        if (apiKey == null || apiKey.isBlank()) {
            logger.error("Umgebungsvariable API_KEY ist nicht gesetzt. Server startet nicht.");
            System.exit(1);
        }

        Javalin app = Javalin.create(config -> {
            // Optional: Weitere Konfiguration hier
        }).start(8080);

        // API-Key-Authentifizierung (außer Health-Check)
        app.before(ctx -> {
            if ("/api/health".equals(ctx.path())) return;
            String provided = ctx.header("X-API-Key");
            if (!apiKey.equals(provided)) {
                throw new io.javalin.http.UnauthorizedResponse("Ungültiger oder fehlender API-Key");
            }
        });

        // GET / - Basis-Endpoint
        app.get("/", ctx -> ctx.json(Map.of("message", "Pebble Navigator Server läuft!")));

        // GET /api/health - Health Check
        app.get("/api/health", ctx -> ctx.json(Map.of(
            "status", "healthy",
            "service", "pebble-navigator-server",
            "timestamp", System.currentTimeMillis()
        )));

        // GET /api/stations - Stationen suchen
        app.get("/api/stations", ctx -> {
            String query = ctx.queryParam("q");
            if (query == null || query.trim().isEmpty()) {
                ctx.status(400).json(Map.of(
                    "error", "Query parameter 'q' is required",
                    "example", "/api/stations?q=Dortmund"
                ));
                return;
            }
            
            logger.info("Searching stations for: {}", query);
            List<StationDto> stations = departureService.searchStations(query);
            
            ctx.json(Map.of(
                "query", query,
                "stations", stations,
                "count", stations.size()
            ));
        });

        // GET /api/departures/{stationId} - Abfahrten für eine Station-ID
        // Optionaler via-Parameter (Station-ID): direkte Verbindungen per Trip-API
        app.get("/api/departures/{stationId}", ctx -> {
            String stationId = ctx.pathParam("stationId");
            int maxDepartures = parseIntParam(ctx, "max", 20);
            String viaId = ctx.queryParam("via");

            if (viaId != null && !viaId.isBlank()) {
                logger.info("Querying direct trips from {} to {} (max: {})", stationId, viaId, maxDepartures);
                ctx.json(departureService.getDirectTrips(stationId, viaId, maxDepartures));
            } else {
                logger.info("Fetching departures for station: {} (max: {})", stationId, maxDepartures);
                ctx.json(departureService.getDepartures(stationId, maxDepartures));
            }
        });

        // GET /api/departures - Abfahrten mit Station-Suche (convenience endpoint)
        app.get("/api/departures", ctx -> {
            String query = ctx.queryParam("station");
            if (query == null || query.trim().isEmpty()) {
                ctx.status(400).json(Map.of(
                    "error", "Query parameter 'station' is required",
                    "examples", Map.of(
                        "by_name", "/api/departures?station=Dortmund Hbf",
                        "by_id", "/api/departures/8000080"
                    )
                ));
                return;
            }

            // Erst Station suchen
            List<StationDto> stations = departureService.searchStations(query);
            
            if (stations.isEmpty()) {
                ctx.json(Map.of(
                    "query", query,
                    "error", "No stations found",
                    "stations", stations
                ));
                return;
            }

            // Erste Station verwenden
            StationDto fromStation = stations.get(0);
            String stationId = fromStation.getId();
            int maxDepartures = parseIntParam(ctx, "max", 20);
            String viaQuery = ctx.queryParam("via");

            if (viaQuery != null && !viaQuery.isBlank()) {
                // via angegeben: direkte Verbindungen per Trip-API suchen
                List<StationDto> viaStations = departureService.searchStations(viaQuery);
                if (viaStations.isEmpty()) {
                    ctx.status(404).json(Map.of(
                        "error", "Via station not found",
                        "viaQuery", viaQuery
                    ));
                    return;
                }
                StationDto toStation = viaStations.get(0);
                logger.info("Querying direct trips from {} to {} (max: {})",
                    fromStation.getName(), toStation.getName(), maxDepartures);
                ctx.json(departureService.getDirectTrips(stationId, toStation.getId(), maxDepartures));
            } else {
                logger.info("Using first station found: {} ({})", stationId, fromStation.getName());
                DeparturesResponseDto result = departureService.getDepartures(stationId, maxDepartures);
                Map<String, Object> response = new java.util.HashMap<>();
                response.put("searchQuery", query);
                response.put("foundStations", stations);
                response.put("status", result.getStatus());
                response.put("timestamp", result.getTimestamp());
                response.put("stations", result.getStations());
                response.put("totalDepartures", result.getTotalDepartures());
                response.put("error", result.getError() != null ? result.getError() : "");
                ctx.json(response);
            }
        });

        // Legacy endpoints (für Kompatibilität)
        app.get("/api/locations", ctx -> {
            String query = ctx.queryParam("q");
            ctx.json(Map.of(
                "message", "This endpoint is deprecated. Use /api/stations?q=" + (query != null ? query : "QUERY"),
                "redirect", "/api/stations" + (query != null ? "?q=" + query : "")
            ));
        });

        app.post("/api/connections", ctx -> {
            ctx.json(Map.of(
                "message", "Connections endpoint not yet implemented",
                "availableEndpoints", Map.of(
                    "stations", "/api/stations?q=QUERY",
                    "departures", "/api/departures?station=STATION_NAME",
                    "departuresById", "/api/departures/STATION_ID"
                )
            ));
        });

        System.out.println("🚀 Javalin Server gestartet auf http://localhost:8080");
        System.out.println("📍 Health Check: http://localhost:8080/api/health");
        System.out.println("🔍 Station Search: http://localhost:8080/api/stations?q=Dortmund");
        System.out.println("🚆 Departures: http://localhost:8080/api/departures?station=Dortmund Hbf");
        System.out.println("🕐 Departures by ID: http://localhost:8080/api/departures/8000080");
    }

    private static int parseIntParam(Context ctx, String paramName, int defaultValue) {
        String value = ctx.queryParam(paramName);
        if (value == null) return defaultValue;
        
        try {
            int parsed = Integer.parseInt(value);
            return Math.max(1, Math.min(parsed, 100)); // Zwischen 1 und 100
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }
}