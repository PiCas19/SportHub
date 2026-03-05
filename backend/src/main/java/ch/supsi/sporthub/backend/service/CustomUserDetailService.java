package ch.supsi.sporthub.backend.service;

import ch.supsi.sporthub.backend.model.User;
import ch.supsi.sporthub.backend.repository.jpa.UserRepository;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collections;

/**
 * Custom implementation of the UserDetailsService interface for loading user details during authentication.
 * This service retrieves user information from the database and provides it to the Spring Security framework.
 * It also maps the user’s role to an authority for access control.
 */
@Service
public class CustomUserDetailService implements UserDetailsService {

    private final UserRepository userRepository;

    /**
     * Constructor for the CustomUserDetailService class.
     * Initializes the service with the provided UserRepository for fetching user data from the database.
     *
     * @param userRepository The repository for accessing and managing user data.
     */
    public CustomUserDetailService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * Loads user details based on the provided username. The method retrieves the user from the database,
     * and if the user is found, it builds a UserDetails object for use by Spring Security.
     * If the user is not found, a UsernameNotFoundException is thrown.
     *
     * @param username The username of the user to load.
     * @return A UserDetails object containing the user's information (username, password, and authorities).
     * @throws UsernameNotFoundException If the user cannot be found by the provided username.
     */
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        return org.springframework.security.core.userdetails.User.builder()
                .username(user.getUsername())
                .password(user.getPassword())
                .authorities(Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + user.getRole().getName())))
                .build();
    }
}