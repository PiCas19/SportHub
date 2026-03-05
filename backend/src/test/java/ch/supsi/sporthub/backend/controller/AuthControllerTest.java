package ch.supsi.sporthub.backend.controller;

import ch.supsi.sporthub.backend.dto.request.auth.*;
import ch.supsi.sporthub.backend.model.User;
import ch.supsi.sporthub.backend.security.JwtUtil;
import ch.supsi.sporthub.backend.service.EmailService;
import ch.supsi.sporthub.backend.service.JwtTokenService;
import ch.supsi.sporthub.backend.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;
import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AuthController.class)
public class AuthControllerTest {


    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @TestConfiguration
    static class MockConfig {
        @Bean AuthenticationManager authenticationManager() {
            return mock(AuthenticationManager.class);
        }

        @Bean JwtTokenService jwtTokenService() {
            return mock(JwtTokenService.class);
        }

        @Bean
        public JwtUtil jwtUtil() {
            return mock(JwtUtil.class);
        }

        @Bean UserService userService() {
            return mock(UserService.class);
        }

        @Bean UserDetailsService userDetailsService() {
            return mock(UserDetailsService.class);
        }

        @Bean
        EmailService emailService() {
            return mock(EmailService.class);
        }

        @Bean
        public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
            return http
                    .csrf(AbstractHttpConfigurer::disable)
                    .authorizeHttpRequests(auth -> auth.anyRequest().permitAll())
                    .build();
        }
    }

    @Autowired private AuthenticationManager authenticationManager;
    @Autowired private JwtTokenService jwtTokenService;
    @Autowired private UserService userService;
    @Autowired private UserDetailsService userDetailsService;
    @Autowired private EmailService emailService;

    @Test
    void login_userNotFound() throws Exception {
        AuthRequest request = new AuthRequest();
        request.setUsername("john");
        request.setPassword("password");
        when(userService.findByUsername("john")).thenReturn(null);
        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("User not found"));
    }

    @Test
    void login_accountNotActive() throws Exception {
        AuthRequest request = new AuthRequest();
        request.setUsername("john");
        request.setPassword("password");
        User user = new User();
        user.setUsername("john");
        user.setAccountStatus(false);

        when(userService.findByUsername("john")).thenReturn(user);
        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").value("Account not active. Please activate it via email."));
    }

    @Test
    void login_successful() throws Exception {
        AuthRequest request = new AuthRequest();
        request.setUsername("john");
        request.setPassword("password");
        User user = new User();
        user.setUsername("john");
        user.setAccountStatus(true);

        UserDetails userDetails = org.springframework.security.core.userdetails.User
                .withUsername("john")
                .password("password")
                .authorities("USER")
                .build();

        when(userService.findByUsername("john")).thenReturn(user);
        when(userDetailsService.loadUserByUsername("john")).thenReturn(userDetails);
        when(jwtTokenService.generateAccessToken(userDetails)).thenReturn("access-token");
        when(jwtTokenService.generateRefreshToken(userDetails)).thenReturn("refresh-token");
        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Login successful"))
                .andExpect(jsonPath("$.accessToken").value("access-token"))
                .andExpect(jsonPath("$.refreshToken").value("refresh-token"));
    }

    @Test
    void refresh_invalidToken() throws Exception {
        String token = "invalid-refresh-token";
        RefreshTokenRequest request = new RefreshTokenRequest(token, "john");

        UserDetails userDetails = org.springframework.security.core.userdetails.User
                .withUsername("john")
                .password("password")
                .authorities("USER")
                .build();
        when(jwtTokenService.extractUsername(token)).thenReturn("john");
        when(userDetailsService.loadUserByUsername("john")).thenReturn(userDetails);
        when(jwtTokenService.isRefreshTokenValid(token, userDetails)).thenReturn(false);
        mockMvc.perform(post("/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Invalid or expired refresh token"));
    }



    @Test
    void refresh_successful() throws Exception {
        RefreshTokenRequest request = new RefreshTokenRequest("refresh-token", "john");

        UserDetails userDetails = org.springframework.security.core.userdetails.User
                .withUsername("john")
                .password("password")
                .authorities("USER")
                .build();

        when(jwtTokenService.extractUsername("refresh-token")).thenReturn("john");
        when(userDetailsService.loadUserByUsername("john")).thenReturn(userDetails);
        when(jwtTokenService.isRefreshTokenValid("refresh-token", userDetails)).thenReturn(true);
        when(jwtTokenService.generateAccessToken(userDetails)).thenReturn("new-access-token");

        mockMvc.perform(post("/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Token refreshed"))
                .andExpect(jsonPath("$.accessToken").value("new-access-token"))
                .andExpect(jsonPath("$.refreshToken").value("refresh-token"));
    }


    @Test
    void register_usernameAlreadyTaken() throws Exception {
        AuthRequest request = new AuthRequest();
        request.setUsername("john");
        request.setPassword("password");
        request.setEmail("john@example.com");
        request.setFirstName("John");
        request.setLastName("Doe");
        when(userService.findByUsername("john")).thenReturn(new User());
        mockMvc.perform(post("/auth/register")
                        .param("role", "USER")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("Username already taken"));
    }


    @Test
    void register_successful() throws Exception {
        AuthRequest request = new AuthRequest();
        request.setUsername("john");
        request.setPassword("password");
        request.setEmail("john@example.com");
        request.setFirstName("John");
        request.setLastName("Doe");

        User mockUser = new User();
        mockUser.setUsername("john");
        mockUser.setEmail("john@example.com");

        UserDetails mockUserDetails = new org.springframework.security.core.userdetails.User(
                "john", "password", Collections.emptyList());

        when(userService.findByUsername("john")).thenReturn(null);
        when(userService.registerUser(eq("john"), eq("password"), eq("USER"),
                eq("john@example.com"), eq("John"), eq("Doe"))).thenReturn(mockUser);
        when(userDetailsService.loadUserByUsername("john")).thenReturn(mockUserDetails);
        when(jwtTokenService.generateActivationToken(mockUserDetails)).thenReturn("activation-token");
        when(jwtTokenService.generateAccessToken(mockUserDetails)).thenReturn("access-token");
        when(jwtTokenService.generateRefreshToken(mockUserDetails)).thenReturn("refresh-token");
        mockMvc.perform(post("/auth/register")
                        .param("role", "USER")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.message").value("User registered. Activation email sent."))
                .andExpect(jsonPath("$.accessToken").value("access-token"))
                .andExpect(jsonPath("$.refreshToken").value("refresh-token"));

        verify(emailService).sendActivationCode(eq("john@example.com"), contains("activation-token"));
    }

    @Test
    void activateAccount_invalidToken() throws Exception {
        String invalidToken = "invalid-token";
        ActivateTokenRequest request = new ActivateTokenRequest(invalidToken);
        when(jwtTokenService.validateActivationToken(invalidToken)).thenReturn(false);
        mockMvc.perform(post("/auth/activate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Invalid or expired activation token"));
    }

    @Test
    void activateAccount_accountAlreadyActivated() throws Exception {
        String validToken = "valid-token";
        String username = "john";
        User user = new User();
        user.setUsername(username);
        user.setAccountStatus(true);

        ActivateTokenRequest request = new ActivateTokenRequest(validToken);
        when(jwtTokenService.validateActivationToken(validToken)).thenReturn(true);
        when(jwtTokenService.extractUsername(validToken)).thenReturn(username);
        when(userService.findByUsername(username)).thenReturn(user);
        mockMvc.perform(post("/auth/activate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("Account already activated"));
    }

    @Test
    void activateAccount_userNotFound() throws Exception {
        String validToken = "valid-token";
        String username = "john";
        ActivateTokenRequest request = new ActivateTokenRequest(validToken);
        when(jwtTokenService.validateActivationToken(validToken)).thenReturn(true);
        when(jwtTokenService.extractUsername(validToken)).thenReturn(username);
        when(userService.findByUsername(username)).thenReturn(null);
        mockMvc.perform(post("/auth/activate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("User not found"));
    }


    @Test
    void activateAccount_success() throws Exception {
        String token = "activation-token";
        String username = "john";
        User user = new User();
        user.setUsername(username);
        user.setAccountStatus(false);

        when(jwtTokenService.validateActivationToken(token)).thenReturn(true);
        when(jwtTokenService.extractUsername(token)).thenReturn(username);
        when(userService.findByUsername(username)).thenReturn(user);

        ActivateTokenRequest request = new ActivateTokenRequest(token);

        mockMvc.perform(post("/auth/activate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNoContent());

        verify(userService).activateUser(user);
    }

    @Test
    void validateToken_invalidToken() throws Exception {
        String invalidToken = "invalid-token";
        String username = "john";
        String purpose = "ACCESS";

        UserDetails userDetails = org.springframework.security.core.userdetails.User
                .withUsername(username)
                .password("password")
                .authorities("USER")
                .build();

        when(jwtTokenService.getTokenPurpose(invalidToken)).thenReturn(purpose);
        when(jwtTokenService.extractUsername(invalidToken)).thenReturn(username);
        when(userDetailsService.loadUserByUsername(username)).thenReturn(userDetails);
        when(jwtTokenService.isTokenValid(invalidToken, userDetails)).thenReturn(false);

        mockMvc.perform(get("/auth/validate-token")
                        .param("token", invalidToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value(purpose + " token is invalid or expired for user: " + username));
    }

    @Test
    void validateToken_success() throws Exception {
        String token = "valid-token";
        String username = "john";
        String purpose = "ACCESS";

        UserDetails userDetails = new org.springframework.security.core.userdetails.User(
                username, "", Collections.emptyList());

        when(jwtTokenService.getTokenPurpose(token)).thenReturn(purpose);
        when(jwtTokenService.extractUsername(token)).thenReturn(username);
        when(userDetailsService.loadUserByUsername(username)).thenReturn(userDetails);
        when(jwtTokenService.isTokenValid(token, userDetails)).thenReturn(true);

        mockMvc.perform(get("/auth/validate-token")
                        .param("token", token)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value(purpose + " token valid for " + username))
                .andExpect(jsonPath("$.valid").value(true));
    }

    @Test
    void logout_success() throws Exception {
        mockMvc.perform(post("/auth/logout"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Logout successful"));
    }


    @Test
    void forgotPassword_userNotFound() throws Exception {
        String email = "john@example.com";
        ForgotPasswordRequest request = new ForgotPasswordRequest(email);
        when(userService.findByEmail(email)).thenReturn(null);
        mockMvc.perform(post("/auth/forgot-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("User not found"));
    }

    @Test
    void forgotPassword_success() throws Exception {
        String email = "john@example.com";
        String username = "john";
        String resetToken = "reset-token";

        User user = new User();
        user.setUsername(username);
        user.setEmail(email);

        UserDetails userDetails = new org.springframework.security.core.userdetails.User(username, "", List.of());

        when(userService.findByEmail(email)).thenReturn(user);
        when(userDetailsService.loadUserByUsername(username)).thenReturn(userDetails);
        when(jwtTokenService.generatePasswordResetToken(userDetails)).thenReturn(resetToken);

        ForgotPasswordRequest request = new ForgotPasswordRequest(email);

        mockMvc.perform(post("/auth/forgot-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$.message").value("Reset password email sent"));

        verify(emailService).sendPasswordResetEmail(eq(email), contains(resetToken));
    }

    @Test
    void resetPassword_success() throws Exception {
        String token = "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJqb2huIn0.signature";
        String username = "john";
        String newPassword = "newPass";

        User user = new User();
        user.setUsername(username);

        when(jwtTokenService.validatePasswordResetToken(token)).thenReturn(true);
        when(jwtTokenService.extractUsername(token)).thenReturn(username);
        when(userService.findByUsername(username)).thenReturn(user);

        ResetPasswordRequest request = new ResetPasswordRequest();
        request.setToken(token);
        request.setNewPassword(newPassword);

        mockMvc.perform(put("/auth/reset-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNoContent());

        verify(userService).updateUserPassword(user, newPassword);
    }


    @Test
    void changePassword_success() throws Exception {
        String username = "john";
        String token = "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJqb2huIn0.signature";

        User user = new User();
        user.setUsername(username);

        ChangePasswordRequest request = new ChangePasswordRequest("oldPass", "newPass");
        when(jwtTokenService.extractUsername(token)).thenReturn(username);
        when(userService.findByUsername(username)).thenReturn(user);
        Authentication mockAuth = mock(Authentication.class);
        when(authenticationManager.authenticate(any())).thenReturn(mockAuth);
        mockMvc.perform(put("/auth/change-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + token)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNoContent());
        verify(userService, times(2)).updateUserPassword(eq(user), eq("newPass"));
    }


    @Test
    void deleteAccount_success() throws Exception {
        String username = "john";
        String token = "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJqb2huIn0.signature";

        User user = new User();
        user.setUsername(username);

        when(jwtTokenService.extractUsername(token)).thenReturn(username);
        when(userService.findByUsername(username)).thenReturn(user);

        mockMvc.perform(delete("/auth/delete")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isNoContent());

        verify(userService).deleteUser(user);
    }


    @Test
    void resetPassword_tokenInvalid() throws Exception {
        String token = "invalid-token";
        String newPassword = "newPass";
        ResetPasswordRequest request = new ResetPasswordRequest();
        request.setToken(token);
        request.setNewPassword(newPassword);
        when(jwtTokenService.validatePasswordResetToken(token)).thenReturn(false);
        mockMvc.perform(put("/auth/reset-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Invalid or expired token"));
    }

    @Test
    void resetPassword_userNotFound() throws Exception {
        String token = "valid-token";
        String newPassword = "newPass";
        String username = "john";
        ResetPasswordRequest request = new ResetPasswordRequest();
        request.setToken(token);
        request.setNewPassword(newPassword);
        when(jwtTokenService.validatePasswordResetToken(token)).thenReturn(true);
        when(jwtTokenService.extractUsername(token)).thenReturn(username);
        when(userService.findByUsername(username)).thenReturn(null);
        mockMvc.perform(put("/auth/reset-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("User not found"));
    }



}
