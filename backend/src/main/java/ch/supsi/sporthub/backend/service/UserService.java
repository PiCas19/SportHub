package ch.supsi.sporthub.backend.service;

import ch.supsi.sporthub.backend.exception.EmailAlreadyTakenException;
import ch.supsi.sporthub.backend.model.Leaderboard;
import ch.supsi.sporthub.backend.model.Role;
import ch.supsi.sporthub.backend.model.UserImage;
import ch.supsi.sporthub.backend.repository.jpa.*;
import ch.supsi.sporthub.backend.model.User;
import ch.supsi.sporthub.backend.service.exception.ServiceException;
import ch.supsi.sporthub.backend.service.impl.ImageServiceImpl;
import jakarta.transaction.Transactional;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.*;

/**
 * Service class responsible for managing user-related operations such as registration, profile updates,
 * and user deletion. It interacts with the repositories to perform CRUD operations for user data,
 * manages user roles, handles user profile images, and supports user account activation and password management.
 */
@Service
public class UserService {
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserImageRepository userImageRepository;
    private final ImageServiceImpl imageService;
    private final StravaTokenRepository stravaTokenRepository;
    private final LeaderboardRepository leaderboardRepository;
    private final CompetitionParticipantRepository competitionParticipantRepository;
    private final CompetitionLeaderboardEntryRepository competitionLeaderboardEntryRepository;
    private final GoalRepository goalRepository;

    /**
     * Constructor for the UserService class.
     * Initializes the service with the provided dependencies.
     *
     * @param userRepository               The repository for managing user data.
     * @param roleRepository               The repository for managing user roles.
     * @param userImageRepository          The repository for managing user profile images.
     * @param passwordEncoder             The encoder for encoding user passwords.
     * @param stravaTokenRepository       The repository for managing Strava tokens.
     * @param leaderboardRepository       The repository for managing leaderboards.
     * @param competitionParticipantRepository The repository for managing competition participants.
     * @param goalRepository              The repository for managing user goals.
     * @param competitionLeaderboardEntryRepository The repository for managing competition leaderboard entries.
     */
    public UserService(UserRepository userRepository, RoleRepository roleRepository,
                       UserImageRepository userImageRepository, PasswordEncoder passwordEncoder,
                       StravaTokenRepository stravaTokenRepository, LeaderboardRepository leaderboardRepository,
                       CompetitionParticipantRepository competitionParticipantRepository, GoalRepository goalRepository,
                       CompetitionLeaderboardEntryRepository competitionLeaderboardEntryRepository) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.userImageRepository = userImageRepository;
        this.passwordEncoder = passwordEncoder;
        this.imageService = new ImageServiceImpl();
        this.stravaTokenRepository = stravaTokenRepository;
        this.leaderboardRepository = leaderboardRepository;
        this.competitionParticipantRepository = competitionParticipantRepository;
        this.goalRepository = goalRepository;
        this.competitionLeaderboardEntryRepository = competitionLeaderboardEntryRepository;
    }

    /**
     * Finds a user by their username.
     *
     * @param username The username of the user to find.
     * @return The user with the given username, or null if not found.
     */
    public User findByUsername(String username) {
        return userRepository.findByUsername(username).orElse(null);
    }

    /**
     * Finds a user by their email.
     *
     * @param email The email of the user to find.
     * @return The user with the given email, or null if not found.
     */
    public User findByEmail(String email) {
        return userRepository.findByEmail(email).orElse(null);
    }

    /**
     * Finds a user by their email.
     *
     * @param email The email of the user to find.
     * @return The user with the given email, or null if not found.
     */
    public User registerUser(String username, String password, String roleName, String email, String firstName, String lastName) {
        if (userRepository.existsByEmail(email)) {
            throw new EmailAlreadyTakenException("Email is already in use");
        }
        Role role = roleRepository.findByName(roleName)
                .orElseGet(() -> {
                    Role newRole = new Role();
                    newRole.setName(roleName);
                    return roleRepository.save(newRole);
                });

        User newUser = new User();
        newUser.setUsername(username);
        newUser.setPassword(passwordEncoder.encode(password));
        newUser.setFirstName(firstName);
        newUser.setLastName(lastName);
        newUser.setEmail(email);
        newUser.setRole(role);
        newUser.setAccountStatus(false);
        newUser.setHeight(0.0);
        newUser.setWeight(0.0);
        newUser.setUserImage(null);
        userRepository.save(newUser);
        return newUser;
    }

    /**
     * Activates a user account by setting the account status to true.
     *
     * @param user The user to activate.
     */
    public void activateUser(User user) {
        if (user != null) {
            user.setAccountStatus(true);
            userRepository.save(user);
        }
    }

    /**
     * Updates the password of a user.
     *
     * @param user        The user whose password is to be updated.
     * @param newPassword The new password to set for the user.
     */
    public void updateUserPassword(User user, String newPassword) {
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }

    /**
     * Updates the height and weight metrics for a user.
     *
     * @param user   The user whose metrics are to be updated.
     * @param weight The new weight of the user.
     * @param height The new height of the user.
     */
    public void updateUserMetrics(User user, Double weight, Double height) {
        user.setWeight(weight);
        user.setHeight(height);
        userRepository.save(user);
    }

    /**
     * Retrieves the profile image for a user by their ID.
     *
     * @param userId The ID of the user whose profile image is to be retrieved.
     * @return An optional containing the user's profile image if it exists, otherwise empty.
     */
    public Optional<UserImage> getUserImage(Long userId) {
        return userImageRepository.findByUserId(userId);
    }

    /**
     * Updates the profile image for a user by saving the given file.
     *
     * @param userId The ID of the user whose profile image is to be updated.
     * @param file   The new profile image file to save.
     * @throws IOException If an error occurs while reading the file.
     */
    public void updateUserImage(Long userId, File file) throws IOException {
        Optional<User> userOptional = userRepository.findById(userId);
        if (userOptional.isEmpty()) {
            throw new IllegalArgumentException("User not found");
        }

        User user = userOptional.get();
        Optional<UserImage> existingImage = userImageRepository.findByUserId(userId);

        UserImage userImage = existingImage.orElse(new UserImage());
        userImage.setUser(user);
        userImage.setFileName(file.getName());
        userImage.setFileType(getMimeType(file));
        userImage.setFileSize(file.length());
        userImage.setImageData(java.nio.file.Files.readAllBytes(file.toPath()));

        userImageRepository.save(userImage);
    }

    /**
     * Validates the profile image file by checking if it exists and is not empty.
     *
     * @param file The file to validate.
     * @return true if the file is valid, false otherwise.
     */
    public boolean validateProfileImage(File file) {
        if (file == null || !file.exists() || file.length() == 0) {
            return false;
        }
        try {
            imageService.load(file.getAbsolutePath());
            return imageService.get() != null;
        } catch (ServiceException e) {
            return false;
        }
    }

    /**
     * Saves the multipart file to the system's temporary directory.
     *
     * @param file The multipart file to save.
     * @return The saved file.
     * @throws IOException If an error occurs while saving the file.
     */
    public File saveMultipartFile(MultipartFile file) throws IOException {
        File savedFile = new File(System.getProperty("java.io.tmpdir"), Objects.requireNonNull(file.getOriginalFilename()));
        file.transferTo(savedFile);
        return savedFile;
    }

    /**
     * Deletes the user and all associated data (Strava tokens, profile image, leaderboard data, etc.).
     *
     * @param user The user to delete.
     */
    @Transactional
    public void deleteUser(User user) {
        if (user == null) {
            return;
        }

        Long userId = user.getId();
        String username = user.getUsername();
        stravaTokenRepository.findByUser(user)
                .ifPresent(token -> stravaTokenRepository.deleteByUser(user));

        userImageRepository.findByUserId(userId)
                .ifPresent(userImageRepository::delete);
        List<Leaderboard> leaderboards = leaderboardRepository.findAll();
        for (Leaderboard lb : leaderboards) {
            Map<String, Integer> scores = lb.getScores();
            if (scores != null && scores.containsKey(username)) {
                scores.remove(username);
                lb.setScores(scores);
                leaderboardRepository.save(lb);
            }
        }

        if (competitionParticipantRepository.existsByUserId(userId)) {
            competitionParticipantRepository.deleteByUserId(userId);
        }
        if (goalRepository.existsByUserId(userId)) {
            goalRepository.deleteByUserId(userId);
        }
        if (competitionLeaderboardEntryRepository.existsByUserId(userId)) {
            competitionLeaderboardEntryRepository.deleteByUserId(userId);
        }
        userRepository.delete(user);
    }

    /**
     * Retrieves the MIME type of the given file.
     *
     * @param file The file to check.
     * @return The MIME type of the file.
     * @throws IOException If an error occurs while determining the MIME type.
     */
    private String getMimeType(File file) throws IOException {
        return Files.probeContentType(file.toPath());
    }
}