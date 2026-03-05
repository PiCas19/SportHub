package ch.supsi.sporthub.backend.controller;

import ch.supsi.sporthub.backend.dto.response.MapDataResponse;
import ch.supsi.sporthub.backend.exception.ResourceNotFoundException;
import ch.supsi.sporthub.backend.service.api.IMapCacheService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * MapController handles API requests related to map data.
 * It allows users to retrieve map data using a specific token to access cached map data.
 */
@RestController
@RequestMapping("/api/map")
public class MapController {

    private final IMapCacheService mapCacheService;

    /**
     * Constructs an instance of MapController with the specified MapCacheService.
     *
     * @param mapCacheService The service responsible for retrieving map data from the cache.
     */
    public MapController(IMapCacheService mapCacheService) {
        this.mapCacheService = mapCacheService;
    }

    /**
     * Retrieves map data based on the provided token.
     * If the map data is not found for the provided token, it throws a ResourceNotFoundException.
     *
     * @param token The token identifying the specific map data to retrieve.
     * @return A ResponseEntity containing the map data if found, or an error if not.
     * @throws ResourceNotFoundException If no map data is found for the given token.
     */
    @GetMapping("/{token}")
    public ResponseEntity<MapDataResponse> getMapData(@PathVariable String token) {
        MapDataResponse data = mapCacheService.getMapData(token);
        if (data == null) {
            throw new ResourceNotFoundException("Map data not found for token: " + token);
        }
        return ResponseEntity.ok(data);
    }
}