package ch.supsi.sporthub.backend.service.api;

import ch.supsi.sporthub.backend.dto.response.MapDataResponse;

/**
 * Service interface for caching map-related data.
 * Used to temporarily store and retrieve map data (e.g., activity segments, coordinates)
 * associated with a specific token or identifier.
 */
public interface IMapCacheService {

    /**
     * Stores the provided map data in the cache and returns a unique token
     * that can be used to retrieve it later.
     *
     * @param data the {@link MapDataResponse} to be cached
     * @return a unique string token associated with the cached map data
     */
    String saveMapData(MapDataResponse data);

    /**
     * Retrieves previously cached map data using the provided token.
     *
     * @param token the unique token associated with the cached data
     * @return the {@link MapDataResponse} if found, or null if the token is invalid or expired
     */
    MapDataResponse getMapData(String token);
}