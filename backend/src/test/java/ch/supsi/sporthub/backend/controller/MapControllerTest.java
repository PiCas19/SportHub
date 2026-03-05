package ch.supsi.sporthub.backend.controller;

import ch.supsi.sporthub.backend.dto.response.MapDataResponse;
import ch.supsi.sporthub.backend.security.JwtUtil;
import ch.supsi.sporthub.backend.service.api.IMapCacheService;
import ch.supsi.sporthub.backend.service.domain.strava.ActivitySegment;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(MapController.class)
public class MapControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @TestConfiguration
    static class MockConfig {

        @Bean
        IMapCacheService mapCacheService() {
            return mock(IMapCacheService.class);
        }

        @Bean
        public JwtUtil jwtUtil() {
            return mock(JwtUtil.class);
        }

        @Bean
        public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
            return http
                    .csrf(AbstractHttpConfigurer::disable)
                    .authorizeHttpRequests(auth -> auth.anyRequest().permitAll())
                    .build();
        }
    }

    @Autowired
    private IMapCacheService mapCacheService;

    @Test
    void getMapData_Success() throws Exception {
        String token = "valid-token";

        ActivitySegment segment = new ActivitySegment(45.0, 7.0, 45.5, 7.5, "Segment 1");
        segment.addPoint(45.1, 7.1);
        segment.addPoint(45.2, 7.2);

        List<ActivitySegment> segments = List.of(segment);

        MapDataResponse mapDataResponse = new MapDataResponse(45.0, 7.0, segments);

        when(mapCacheService.getMapData(token)).thenReturn(mapDataResponse);

        mockMvc.perform(get("/api/map/{token}", token)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.targetLat").value(45.0))
                .andExpect(jsonPath("$.targetLon").value(7.0))
                .andExpect(jsonPath("$.segments[0].startLat").value(45.0))
                .andExpect(jsonPath("$.segments[0].startLon").value(7.0))
                .andExpect(jsonPath("$.segments[0].endLat").value(45.5))
                .andExpect(jsonPath("$.segments[0].endLon").value(7.5))
                .andExpect(jsonPath("$.segments[0].label").value("Segment 1"))
                .andExpect(jsonPath("$.segments[0].polyline[0][0]").value(45.1))
                .andExpect(jsonPath("$.segments[0].polyline[0][1]").value(7.1))
                .andExpect(jsonPath("$.segments[0].polyline[1][0]").value(45.2))
                .andExpect(jsonPath("$.segments[0].polyline[1][1]").value(7.2));
        verify(mapCacheService).getMapData(token);
    }

    @Test
    void getMapData_NotFound() throws Exception {
        String token = "invalid-token";
        when(mapCacheService.getMapData(token)).thenReturn(null);
        mockMvc.perform(get("/api/map/{token}", token)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Map data not found for token: " + token));
        verify(mapCacheService).getMapData(token);
    }
}
