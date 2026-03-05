package ch.supsi.sporthub.backend.service;

import ch.supsi.sporthub.backend.model.Role;
import ch.supsi.sporthub.backend.model.User;
import ch.supsi.sporthub.backend.repository.jpa.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class CustomUserDetailServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private CustomUserDetailService customUserDetailService;

    @Test
    void testLoadUserByUsername_UserFound() {
        User user = new User();
        user.setUsername("testuser");
        user.setPassword("password");
        Role role = new Role();
        role.setName("ROLE_USER");
        user.setRole(role);
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));
        UserDetails userDetails = customUserDetailService.loadUserByUsername("testuser");
        assertNotNull(userDetails);
        assertEquals("testuser", userDetails.getUsername());
        assertEquals("password", userDetails.getPassword());
        assertTrue(userDetails.getAuthorities().stream()
                .anyMatch(auth -> auth.getAuthority().equals("ROLE_ROLE_USER")));
    }

    @Test
    void testLoadUserByUsername_UserNotFound() {
        when(userRepository.findByUsername("unknown")).thenReturn(Optional.empty());
        assertThrows(UsernameNotFoundException.class, () -> {
            customUserDetailService.loadUserByUsername("unknown");
        });
    }
}