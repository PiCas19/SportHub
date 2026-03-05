package ch.supsi.sporthub.backend.service.impl;

import ch.supsi.sporthub.backend.dto.response.MapDataResponse;
import ch.supsi.sporthub.backend.service.api.IMapCacheService;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Implementation of the IMapCacheService interface that provides a cache for storing and retrieving map data.
 * This service uses an in-memory cache (ConcurrentHashMap) to store the map data and associate it with a unique token.
 */
@Service
public class MapCacheServiceImpl implements IMapCacheService {

    private final Map<String, MapDataResponse> cache = new ConcurrentHashMap<>();

    /**
     * Saves the provided map data into the cache and generates a unique token for it.
     *
     * @param data The map data to store in the cache.
     * @return A unique token that can be used to retrieve the stored map data.
     */
    @Override
    public String saveMapData(MapDataResponse data) {
        String token = UUID.randomUUID().toString().substring(0, 8);
        cache.put(token, data);
        return token;
    }

    /**
     * Retrieves the map data from the cache using the provided token.
     *
     * @param token The unique token associated with the map data.
     * @return The map data associated with the token, or null if the token is not found.
     */
    @Override
    public MapDataResponse getMapData(String token) {
        return cache.get(token);
    }
}