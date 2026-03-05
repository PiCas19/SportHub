package ch.supsi.sporthub.backend.controller;

import ch.supsi.sporthub.backend.dto.request.UserMetricsRequest;
import ch.supsi.sporthub.backend.dto.response.Response;
import ch.supsi.sporthub.backend.model.User;
import ch.supsi.sporthub.backend.model.UserImage;
import ch.supsi.sporthub.backend.security.JwtUtil;
import ch.supsi.sporthub.backend.service.JwtTokenService;
import ch.supsi.sporthub.backend.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.io.File;
import java.io.IOException;
import java.util.Optional;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UserController.class)
public class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @TestConfiguration
    static class MockConfig {
        @Bean
        public JwtTokenService jwtTokenService() {
            return mock(JwtTokenService.class);
        }

        @Bean
        public UserService userService() {
            return mock(UserService.class);
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
    private JwtTokenService jwtTokenService;

    @Autowired
    private UserService userService;

    @Test
    void getUserImage() throws Exception {
        String token = "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJqb2huIn0.signature";
        String username = "john";
        Long userId = 123L;
        User user = new User();
        user.setUsername(username);
        user.setId(userId);
        UserImage userImage = new UserImage();
        userImage.setFileType("image/jpeg");
        userImage.setImageData("image data".getBytes());
        when(jwtTokenService.extractUsername(token)).thenReturn(username);
        when(userService.findByUsername(username)).thenReturn(user);
        when(userService.getUserImage(userId)).thenReturn(Optional.of(userImage));
        mockMvc.perform(get("/api/user/profile-image")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Type", "image/jpeg"))
                .andExpect(content().bytes(userImage.getImageData()));
        verify(userService).getUserImage(userId);
    }


    @Test
    void updateUserMetrics() throws Exception {
        String token = "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJqb2huIn0.signature";
        String username = "john";
        User user = new User();
        user.setUsername(username);

        UserMetricsRequest metricsRequest = new UserMetricsRequest();
        metricsRequest.setWeight(75.0);
        metricsRequest.setHeight(180.0);

        when(jwtTokenService.extractUsername(token)).thenReturn(username);
        when(userService.findByUsername(username)).thenReturn(user);

        mockMvc.perform(put("/api/user/update-metrics")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(metricsRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("User metrics updated successfully"));

        verify(userService).updateUserMetrics(eq(user), eq(metricsRequest.getWeight()), eq(metricsRequest.getHeight()));
    }

    @Test
    void updateProfileImage_success() throws Exception {
        String token = "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJqb2huIn0.signature";
        String username = "john";
        Long userId = 123L;
        User user = new User();
        user.setUsername(username);
        user.setId(userId);

        MockMultipartFile file = new MockMultipartFile(
                "file",
                "test.jpg",
                MediaType.IMAGE_JPEG_VALUE,
                "some image data".getBytes()
        );

        File savedFile = new File("temp/test.jpg");
        when(jwtTokenService.extractUsername(token)).thenReturn(username);
        when(userService.findByUsername(username)).thenReturn(user);
        when(userService.saveMultipartFile(eq(file))).thenReturn(savedFile);
        when(userService.validateProfileImage(eq(savedFile))).thenReturn(true);

        mockMvc.perform(multipart("/api/user/update-profile-image")
                        .file(file)
                        .header("Authorization", "Bearer " + token)
                        .with(request -> {
                            request.setMethod("PUT");
                            return request;
                        }))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Profile image updated successfully"));

        verify(userService).saveMultipartFile(eq(file));
        verify(userService).validateProfileImage(eq(savedFile));
        verify(userService).updateUserImage(eq(userId), eq(savedFile));
    }

    @Test
    void updateProfileImage_invalidImage() throws Exception {
        String token = "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJqb2huIn0.signature";
        String username = "john";
        User user = new User();
        user.setUsername(username);

        MockMultipartFile file = new MockMultipartFile(
                "file",
                "test.txt",
                MediaType.TEXT_PLAIN_VALUE,
                "some text data".getBytes()
        );

        File savedFile = new File("temp/test.txt");
        when(jwtTokenService.extractUsername(token)).thenReturn(username);
        when(userService.findByUsername(username)).thenReturn(user);
        when(userService.saveMultipartFile(eq(file))).thenReturn(savedFile);
        when(userService.validateProfileImage(eq(savedFile))).thenReturn(false);

        mockMvc.perform(multipart("/api/user/update-profile-image")
                        .file(file)
                        .header("Authorization", "Bearer " + token)
                        .with(request -> {
                            request.setMethod("PUT");
                            return request;
                        }))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Invalid profile image"));

        verify(userService).saveMultipartFile(eq(file));
        verify(userService).validateProfileImage(eq(savedFile));
    }

    @Test
    void getUserProfileTest_actualImplementation() throws Exception {
        String token = "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJqb2huIn0.signature";
        String username = "john";
        User user = new User();
        user.setWeight(75.0);
        user.setHeight(180.0);
        user.setEmail("john.doe@example.com");
        user.setFirstName("John");
        user.setLastName("Doe");
        user.setUsername(username);
        user.setId(1L);
        when(jwtTokenService.extractUsername(anyString())).thenReturn(username);
        when(userService.findByUsername(username)).thenReturn(user);

        mockMvc.perform(get("/api/user/profile")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.weight").value(75.0))
                .andExpect(jsonPath("$.height").value(180.0))
                .andExpect(jsonPath("$.email").value("john.doe@example.com"))
                .andExpect(jsonPath("$.firstName").value("John"))
                .andExpect(jsonPath("$.lastName").value("Doe"))
                .andExpect(jsonPath("$.username").value("john"));
    }


}
