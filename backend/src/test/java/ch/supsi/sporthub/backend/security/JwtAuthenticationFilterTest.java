package ch.supsi.sporthub.backend.security;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;

import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.Method;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

public class JwtAuthenticationFilterTest {

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private UserDetailsService userDetailsService;

    @InjectMocks
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private FilterChain filterChain;

    @Mock
    private UserDetails userDetails;

    private static final String VALID_TOKEN = "validToken";
    private static final String EXPIRED_TOKEN = "expiredToken";
    private static final String USERNAME = "testUser";

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testDoFilterInternal_WithValidToken() throws ServletException, IOException {
        when(request.getHeader("Authorization")).thenReturn("Bearer " + VALID_TOKEN);
        when(jwtUtil.extractUsername(VALID_TOKEN)).thenReturn(USERNAME);
        when(userDetailsService.loadUserByUsername(USERNAME)).thenReturn(userDetails);
        when(jwtUtil.validateToken(VALID_TOKEN, userDetails)).thenReturn(true);
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);
        verify(filterChain, times(1)).doFilter(request, response);
        assertNotNull(SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    void testDoFilterInternal_WithExpiredToken() throws ServletException, IOException {
        when(request.getHeader("Authorization")).thenReturn("Bearer " + EXPIRED_TOKEN);
        when(jwtUtil.extractUsername(EXPIRED_TOKEN)).thenThrow(new ExpiredJwtException(null, null, "Token expired"));
        PrintWriter writer = mock(PrintWriter.class);
        when(response.getWriter()).thenReturn(writer);
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);
        verify(response).setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        verify(response).setContentType("application/json");
        verify(writer).write("{\"error\": \"Token expired\"}");
    }


    @Test
    void testDoFilterInternal_WithoutAuthorizationHeader() throws ServletException, IOException {
        when(request.getHeader("Authorization")).thenReturn(null);
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);
        verify(filterChain, times(1)).doFilter(request, response);
        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    void testDoFilterInternal_WithInvalidToken() throws ServletException, IOException {
        when(request.getHeader("Authorization")).thenReturn("Bearer invalidToken");
        when(jwtUtil.extractUsername("invalidToken")).thenReturn(null);
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);
        verify(filterChain, times(1)).doFilter(request, response);
        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    void testDoFilterInternal_WithNonBearerHeader() throws ServletException, IOException {
        when(request.getHeader("Authorization")).thenReturn("Basic someBase64Token");

        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        verify(filterChain, times(1)).doFilter(request, response);
        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    void testDoFilterInternal_UsernameNull_SkipsAuth() throws Exception {
        SecurityContextHolder.clearContext();

        when(request.getHeader("Authorization")).thenReturn("Bearer token");
        when(jwtUtil.extractUsername("token")).thenReturn(null);

        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }


    @Test
    void testDoFilterInternal_AuthenticationAlreadySet() throws Exception {
        when(request.getHeader("Authorization")).thenReturn("Bearer token");
        when(jwtUtil.extractUsername("token")).thenReturn("user");
        SecurityContextHolder.getContext().setAuthentication(mock(UsernamePasswordAuthenticationToken.class));

        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
    }

    @Test
    void testDoFilterInternal_ValidUsernameButInvalidToken() throws Exception {
        when(request.getHeader("Authorization")).thenReturn("Bearer token");
        when(jwtUtil.extractUsername("token")).thenReturn("user");
        SecurityContextHolder.clearContext();
        when(userDetailsService.loadUserByUsername("user")).thenReturn(userDetails);
        when(jwtUtil.validateToken("token", userDetails)).thenReturn(false);

        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    void testHandleJwtError_WithMalformedJwtException_UsingReflection() throws Exception {
        Method method = JwtAuthenticationFilter.class.getDeclaredMethod("handleJwtError", HttpServletResponse.class, Exception.class);
        method.setAccessible(true);

        PrintWriter writer = mock(PrintWriter.class);
        when(response.getWriter()).thenReturn(writer);

        MalformedJwtException exception = new MalformedJwtException("Invalid token");

        method.invoke(jwtAuthenticationFilter, response, exception);

        verify(response).setStatus(HttpServletResponse.SC_BAD_REQUEST);
        verify(response).setContentType("application/json");
        verify(writer).write("{\"error\": \"Malformed JWT token\"}");
    }

    @Test
    void testHandleJwtError_WithUnsupportedJwtException_UsingReflection() throws Exception {
        Method method = JwtAuthenticationFilter.class.getDeclaredMethod("handleJwtError", HttpServletResponse.class, Exception.class);
        method.setAccessible(true);

        PrintWriter writer = mock(PrintWriter.class);
        when(response.getWriter()).thenReturn(writer);

        UnsupportedJwtException exception = new UnsupportedJwtException("Unsupported token");

        method.invoke(jwtAuthenticationFilter, response, exception);

        verify(response).setStatus(HttpServletResponse.SC_BAD_REQUEST);
        verify(response).setContentType("application/json");
        verify(writer).write("{\"error\": \"Unsupported JWT token\"}");
    }

    @Test
    void testHandleJwtError_WithGenericException_UsingReflection() throws Exception {
        Method method = JwtAuthenticationFilter.class.getDeclaredMethod("handleJwtError", HttpServletResponse.class, Exception.class);
        method.setAccessible(true);

        PrintWriter writer = mock(PrintWriter.class);
        when(response.getWriter()).thenReturn(writer);

        Exception exception = new Exception("Generic error");

        method.invoke(jwtAuthenticationFilter, response, exception);

        verify(response).setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        verify(response).setContentType("application/json");
        verify(writer).write("{\"error\": \"Internal server error\"}");
    }

}
