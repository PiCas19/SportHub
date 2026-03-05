
package ch.supsi.sporthub.backend.service;

import ch.supsi.sporthub.backend.exception.EmailAlreadyTakenException;
import ch.supsi.sporthub.backend.model.*;
import ch.supsi.sporthub.backend.repository.jpa.*;
import ch.supsi.sporthub.backend.service.domain.Image;
import ch.supsi.sporthub.backend.service.exception.ServiceException;
import ch.supsi.sporthub.backend.service.impl.ImageServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private UserImageRepository userImageRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private StravaTokenRepository stravaTokenRepository;

    @Mock
    private LeaderboardRepository leaderboardRepository;

    @Mock private CompetitionParticipantRepository competitionParticipantRepository;

    @Mock private GoalRepository goalRepository;

    @Mock private CompetitionLeaderboardEntryRepository competitionLeaderboardEntryRepository;


    @InjectMocks
    private UserService userService;

    private User testUser;
    private Role testRole;

    @BeforeEach
    void setUp() {
        testRole = new Role();
        testRole.setName("ROLE_USER");

        testUser = new User();
        testUser.setUsername("testUser");
        testUser.setPassword("encodedPassword");
        testUser.setEmail("test@example.com");
        testUser.setFirstName("Test");
        testUser.setLastName("User");
        testUser.setRole(testRole);
    }

    @Test
    void testFindByUsername() {
        when(userRepository.findByUsername("testUser")).thenReturn(Optional.of(testUser));
        User foundUser = userService.findByUsername("testUser");
        assertEquals(testUser, foundUser);
    }

    @Test
    void testFindByEmail() {
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));
        User foundUser = userService.findByEmail("test@example.com");
        assertEquals(testUser, foundUser);
    }

    @Test
    void testRegisterUser() {
        when(roleRepository.findByName("ROLE_USER")).thenReturn(Optional.of(testRole));
        when(passwordEncoder.encode("password")).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        User registeredUser = userService.registerUser("testUser", "password", "ROLE_USER", "test@example.com", "Test", "User");
        assertEquals("encodedPassword", registeredUser.getPassword());
        assertFalse(registeredUser.getAccountStatus());
    }

    @Test
    void testRegisterUser_RoleNotFoundCreatesNewRole() {
        when(roleRepository.findByName("ROLE_USER")).thenReturn(Optional.empty());
        when(roleRepository.save(any(Role.class))).thenAnswer(invocation -> {
            Role role = invocation.getArgument(0);
            role.setId(1L);
            return role;
        });
        when(passwordEncoder.encode("password")).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        User registeredUser = userService.registerUser("testUser", "password", "ROLE_USER", "test@example.com", "Test", "User");

        assertEquals("encodedPassword", registeredUser.getPassword());
        assertEquals("ROLE_USER", registeredUser.getRole().getName());
        assertFalse(registeredUser.getAccountStatus());
        verify(roleRepository, times(1)).save(argThat(role -> role.getName().equals("ROLE_USER")));
    }

    @Test
    void testActivateUser() {
        userService.activateUser(testUser);
        verify(userRepository, times(1)).save(testUser);
        assertTrue(testUser.getAccountStatus());
    }

    @Test
    void testUpdateUserPassword() {
        when(passwordEncoder.encode("newPassword")).thenReturn("encodedNewPassword");
        userService.updateUserPassword(testUser, "newPassword");
        verify(userRepository, times(1)).save(testUser);
        assertEquals("encodedNewPassword", testUser.getPassword());
    }

    @Test
    void testGetUserImage() {
        UserImage userImage = new UserImage();
        when(userImageRepository.findByUserId(1L)).thenReturn(Optional.of(userImage));
        Optional<UserImage> foundImage = userService.getUserImage(1L);
        assertTrue(foundImage.isPresent());
        assertEquals(userImage, foundImage.get());
    }

    @Test
    void testUpdateUserImage_Success(@TempDir Path tempDir) throws IOException {
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        Path tempFilePath = tempDir.resolve("test.jpg");
        File tempFile = tempFilePath.toFile();
        Files.createFile(tempFilePath);
        UserImage userImage = new UserImage();
        when(userImageRepository.findByUserId(1L)).thenReturn(Optional.of(userImage));
        when(userImageRepository.save(any(UserImage.class))).thenReturn(userImage);

        userService.updateUserImage(1L, tempFile);

        verify(userImageRepository, times(1)).save(any(UserImage.class));
    }

    @Test
    void testUpdateUserImage_UserNotFound(@TempDir Path tempDir) throws IOException {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());
        Path tempFilePath = tempDir.resolve("test.jpg");
        File tempFile = tempFilePath.toFile();
        Files.createFile(tempFilePath);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            userService.updateUserImage(1L, tempFile);
        });
        assertEquals("User not found", exception.getMessage());
        verify(userImageRepository, never()).save(any(UserImage.class));
    }

    @Test
    void testValidateProfileImage_InvalidImage() {
        File invalidFile = new File("nonexistent.jpg");
        assertFalse(userService.validateProfileImage(invalidFile));
    }

    @Test
    void testSaveMultipartFile() throws IOException {
        MultipartFile multipartFile = new MockMultipartFile("file", "test.txt", "text/plain", "content".getBytes());
        File savedFile = userService.saveMultipartFile(multipartFile);
        assertTrue(savedFile.exists());
        assertEquals("test.txt", savedFile.getName());
        assertEquals(Paths.get(System.getProperty("java.io.tmpdir")).toString(), Paths.get(savedFile.getParent()).toString());
        savedFile.delete();
    }

    @Test
    void testDeleteUser() {
        testUser.setId(1L);

        StravaToken token = new StravaToken();
        when(stravaTokenRepository.findByUser(testUser)).thenReturn(Optional.of(token));

        UserImage userImage = new UserImage();
        when(userImageRepository.findByUserId(1L)).thenReturn(Optional.of(userImage));

        Leaderboard leaderboard1 = new Leaderboard();
        leaderboard1.setScores(new HashMap<>(Map.of("testUser", 100, "otherUser", 50)));
        Leaderboard leaderboard2 = new Leaderboard();
        leaderboard2.setScores(new HashMap<>(Map.of("someoneElse", 80)));
        when(leaderboardRepository.findAll()).thenReturn(List.of(leaderboard1, leaderboard2));
        when(competitionParticipantRepository.existsByUserId(1L)).thenReturn(true);
        when(goalRepository.existsByUserId(1L)).thenReturn(true);
        when(competitionLeaderboardEntryRepository.existsByUserId(1L)).thenReturn(true);
        userService.deleteUser(testUser);
        verify(stravaTokenRepository).deleteByUser(testUser);
        verify(userImageRepository).delete(userImage);
        verify(leaderboardRepository, times(1)).save(leaderboard1);
        verify(leaderboardRepository, never()).save(leaderboard2);

        verify(competitionParticipantRepository).deleteByUserId(1L);
        verify(goalRepository).deleteByUserId(1L);
        verify(competitionLeaderboardEntryRepository).deleteByUserId(1L);

        verify(userRepository).delete(testUser);

        assertFalse(leaderboard1.getScores().containsKey("testUser"));
        assertEquals(1, leaderboard1.getScores().size());
    }


    @Test
    void testValidateProfileImage_NullFile() {
        assertFalse(userService.validateProfileImage(null));
    }

    @Test
    void testValidateProfileImage_NonExistentFile() {
        File nonExistentFile = mock(File.class);
        when(nonExistentFile.exists()).thenReturn(false);
        assertFalse(userService.validateProfileImage(nonExistentFile));
    }

    @Test
    void testValidateProfileImage_EmptyFile() {
        File emptyFile = mock(File.class);
        when(emptyFile.exists()).thenReturn(true);
        when(emptyFile.length()).thenReturn(0L);
        assertFalse(userService.validateProfileImage(emptyFile));
    }

    @Test
    void testValidateProfileImage_ValidImage_WithSpy() {
        UserService realService = new UserService(userRepository, roleRepository, userImageRepository, passwordEncoder,
                stravaTokenRepository, leaderboardRepository, competitionParticipantRepository,
                goalRepository, competitionLeaderboardEntryRepository);
        File mockFile = mock(File.class);
        when(mockFile.exists()).thenReturn(true);
        when(mockFile.length()).thenReturn(100L);
        when(mockFile.getAbsolutePath()).thenReturn("/some/path/image.png");
        ImageServiceImpl mockedImageService = mock(ImageServiceImpl.class);
        doNothing().when(mockedImageService).load("/some/path/image.png");
        when(mockedImageService.get()).thenReturn(new Image(1, 1));
        ReflectionTestUtils.setField(realService, "imageService", mockedImageService);
        boolean result = realService.validateProfileImage(mockFile);
        assertTrue(result);
    }



    @Test
    void testValidateProfileImage_ThrowsServiceException_WithSpy() throws Exception {
        UserService realService = new UserService(userRepository, roleRepository,
                userImageRepository, passwordEncoder, stravaTokenRepository, leaderboardRepository,
                competitionParticipantRepository, goalRepository, competitionLeaderboardEntryRepository);

        File mockFile = mock(File.class);
        when(mockFile.exists()).thenReturn(true);
        when(mockFile.length()).thenReturn(100L);
        when(mockFile.getAbsolutePath()).thenReturn("/some/path/image.png");

        ImageServiceImpl mockedImageService = mock(ImageServiceImpl.class);
        doThrow(new ServiceException("fail")).when(mockedImageService).load("/some/path/image.png");

        ReflectionTestUtils.setField(realService, "imageService", mockedImageService);

        boolean result = realService.validateProfileImage(mockFile);
        assertFalse(result);
    }


    @Test
    void testDeleteUser_UserNullSafe() {
        userService.deleteUser(null);
        verify(userRepository, never()).delete(any());
        verify(competitionParticipantRepository, never()).deleteByUserId(any());
        verify(goalRepository, never()).deleteByUserId(any());
        verify(competitionLeaderboardEntryRepository, never()).deleteByUserId(any());
    }


    @Test
    void testDeleteUser_UserWithoutStravaOrImageOrScore() {
        User user = new User();
        user.setId(42L);
        user.setUsername("ghostUser");

        when(stravaTokenRepository.findByUser(user)).thenReturn(Optional.empty());
        when(userImageRepository.findByUserId(user.getId())).thenReturn(Optional.empty());

        Leaderboard leaderboard = new Leaderboard();
        leaderboard.setScores(new HashMap<>());

        when(leaderboardRepository.findAll()).thenReturn(List.of(leaderboard));

        userService.deleteUser(user);

        verify(stravaTokenRepository, never()).deleteByUser(any());
        verify(userImageRepository, never()).delete(any());
        verify(leaderboardRepository, never()).save(any());
        verify(userRepository).delete(user);
    }

    @Test
    void testActivateUser_NullSafe() {
        userService.activateUser(null);
        verify(userRepository, never()).save(any());
    }

    @Test
    void testUpdateUserMetrics_SavesValues() {
        User user = new User();
        userService.updateUserMetrics(user, 72.5, 180.0);
        assertEquals(72.5, user.getWeight());
        assertEquals(180.0, user.getHeight());
        verify(userRepository).save(user);
    }

    @Test
    void testValidateProfileImage_ReturnsFalse_WhenImageIsNull() {
        UserService realService = new UserService(userRepository, roleRepository,
                userImageRepository, passwordEncoder,
                stravaTokenRepository, leaderboardRepository,
                competitionParticipantRepository, goalRepository, competitionLeaderboardEntryRepository);

        File mockFile = mock(File.class);
        when(mockFile.exists()).thenReturn(true);
        when(mockFile.length()).thenReturn(100L);
        when(mockFile.getAbsolutePath()).thenReturn("/some/path/image.png");

        ImageServiceImpl mockedImageService = mock(ImageServiceImpl.class);
        doNothing().when(mockedImageService).load("/some/path/image.png");
        when(mockedImageService.get()).thenReturn(null);

        ReflectionTestUtils.setField(realService, "imageService", mockedImageService);

        boolean result = realService.validateProfileImage(mockFile);
        assertFalse(result);
    }

    @Test
    void testDeleteUser_ScoresNull() {
        testUser.setId(1L);

        Leaderboard leaderboard = new Leaderboard();
        leaderboard.setScores(null);
        when(leaderboardRepository.findAll()).thenReturn(List.of(leaderboard));

        userService.deleteUser(testUser);
        verify(leaderboardRepository, never()).save(any());
    }


    @Test
    void testRegisterUser_EmailAlreadyTaken_ThrowsException() {
        when(userRepository.existsByEmail("test@example.com")).thenReturn(true);
        EmailAlreadyTakenException exception = assertThrows(EmailAlreadyTakenException.class, () -> {
            userService.registerUser("testUser", "password", "ROLE_USER", "test@example.com", "Test", "User");
        });

        assertEquals("Email is already in use", exception.getMessage());
        verify(userRepository, never()).save(any(User.class));
    }

}