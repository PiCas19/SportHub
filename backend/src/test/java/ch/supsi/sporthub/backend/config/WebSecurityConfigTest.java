package ch.supsi.sporthub.backend.config;

import ch.supsi.sporthub.backend.security.JwtAuthenticationFilter;
import ch.supsi.sporthub.backend.service.CustomUserDetailService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;

import static org.assertj.core.api.Assertions.assertThat;

@WebMvcTest(WebSecurityConfig.class)
@Import({ WebSecurityConfig.class, WebSecurityConfigTest.TestConfig.class })
class WebSecurityConfigTest {

    @Autowired
    private ApplicationContext context;

    @Autowired
    @Qualifier("corsConfigurationSource")
    private CorsConfigurationSource corsConfigurationSource;

    @Test
    @DisplayName("SecurityFilterChain bean is present")
    void securityFilterChainBeanExists() {
        assertThat(context.containsBean("securityFilterChain")).isTrue();
        SecurityFilterChain filterChain = context.getBean(SecurityFilterChain.class);
        assertThat(filterChain).isNotNull();
    }

    @Test
    @DisplayName("AuthenticationProvider bean is a DaoAuthenticationProvider")
    void authenticationProviderBean() {
        assertThat(context.containsBean("authenticationProvider")).isTrue();
        AuthenticationProvider provider = context.getBean(AuthenticationProvider.class);
        assertThat(provider)
                .isInstanceOfAny(org.springframework.security.authentication.dao.DaoAuthenticationProvider.class);
    }

    @Test
    @DisplayName("PasswordEncoder bean is BCryptPasswordEncoder")
    void passwordEncoderBean() {
        assertThat(context.containsBean("passwordEncoder")).isTrue();
        PasswordEncoder encoder = context.getBean(PasswordEncoder.class);
        assertThat(encoder)
                .isInstanceOf(org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder.class);
    }

    @Test
    @DisplayName("AuthenticationManager bean is provided")
    void authenticationManagerBean() {
        assertThat(context.containsBean("authenticationManager")).isTrue();
        AuthenticationManager manager = context.getBean(AuthenticationManager.class);
        assertThat(manager).isNotNull();
    }

    @Test
    @DisplayName("CorsConfigurationSource has expected origins, methods and headers")
    void corsConfigurationSource() {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/any/path");

        CorsConfiguration config = corsConfigurationSource.getCorsConfiguration(request);
        assertThat(config).isNotNull();
        assertThat(config.getAllowedOrigins())
                .containsExactlyInAnyOrder("http://localhost:5173", "http://192.168.1.112:5173");
        assertThat(config.getAllowedMethods())
                .containsExactlyInAnyOrder("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS");
        assertThat(config.getAllowedHeaders())
                .contains("Authorization", "Content-Type", "X-Requested-With");
        assertThat(config.getExposedHeaders())
                .contains("Authorization", "Set-Cookie");
        assertThat(config.getAllowCredentials()).isTrue();
    }

    @Configuration
    static class TestConfig {
        @Bean
        CustomUserDetailService customUserDetailService() {
            return Mockito.mock(CustomUserDetailService.class);
        }

        @Bean
        JwtAuthenticationFilter jwtAuthenticationFilter() {
            return Mockito.mock(JwtAuthenticationFilter.class);
        }
    }
}