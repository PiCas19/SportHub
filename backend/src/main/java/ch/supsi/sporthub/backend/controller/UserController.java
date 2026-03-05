package ch.supsi.sporthub.backend.controller;

import ch.supsi.sporthub.backend.dto.request.UserMetricsRequest;
import ch.supsi.sporthub.backend.dto.response.Response;
import ch.supsi.sporthub.backend.dto.response.UserProfileResponse;
import ch.supsi.sporthub.backend.exception.InvalidProfileImageException;
import ch.supsi.sporthub.backend.exception.UserImageNotFoundException;
import ch.supsi.sporthub.backend.model.User;
import ch.supsi.sporthub.backend.model.UserImage;
import ch.supsi.sporthub.backend.service.JwtTokenService;
import ch.supsi.sporthub.backend.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;

/**
 * UserController provides API endpoints for managing user profile data, including updating profile images,
 * managing user metrics, and retrieving user profile information.
 */
@RestController
@RequestMapping("/api/user")
public class UserController extends BaseController {

    private final UserService userService;

    /**
     * Constructs an instance of UserController with the specified services.
     *
     * @param userService     The service responsible for user operations, such as updating profile data and metrics.
     * @param jwtTokenService The service responsible for JWT token handling.
     */
    public UserController(UserService userService, JwtTokenService jwtTokenService) {
        super(jwtTokenService, userService);
        this.userService = userService;
    }

    /**
     * Updates the profile image of the authenticated user.
     * The image is validated, and if it is invalid, an exception is thrown.
     *
     * @param file       The image file to be uploaded.
     * @param authHeader The authorization header containing the user's JWT token.
     * @return A response entity confirming the success of the image update.
     * @throws IOException If there is an error while saving the image.
     */
    @PutMapping("/update-profile-image")
    public ResponseEntity<Response> updateProfileImage(@RequestParam("file") MultipartFile file,
                                                       @RequestHeader("Authorization") String authHeader) throws IOException {
        User user = getUserFromHeader(authHeader);
        File savedFile = userService.saveMultipartFile(file);

        if (!userService.validateProfileImage(savedFile)) {
            throw new InvalidProfileImageException("Invalid profile image");
        }

        userService.updateUserImage(user.getId(), savedFile);
        return ResponseEntity.ok(new Response("Profile image updated successfully"));
    }

    /**
     * Retrieves the profile image of the authenticated user.
     *
     * @param authHeader The authorization header containing the user's JWT token.
     * @return A response entity containing the user's image data in the appropriate content type.
     * @throws UserImageNotFoundException If the user image does not exist.
     */
    @GetMapping("/profile-image")
    public ResponseEntity<?> getUserImage(@RequestHeader("Authorization") String authHeader) {
        User user = getUserFromHeader(authHeader);

        UserImage userImage = userService.getUserImage(user.getId())
                .orElseThrow(() -> new UserImageNotFoundException("User image not found"));

        return ResponseEntity.ok()
                .header("Content-Type", userImage.getFileType())
                .body(userImage.getImageData());
    }

    /**
     * Updates the metrics (weight and height) of the authenticated user.
     *
     * @param metricsRequest The request body containing the updated weight and height.
     * @param authHeader     The authorization header containing the user's JWT token.
     * @return A response entity confirming the success of the update.
     */
    @PutMapping("/update-metrics")
    public ResponseEntity<Response> updateUserMetrics(@RequestBody UserMetricsRequest metricsRequest,
                                                      @RequestHeader("Authorization") String authHeader) {
        User user = getUserFromHeader(authHeader);
        userService.updateUserMetrics(user, metricsRequest.getWeight(), metricsRequest.getHeight());
        return ResponseEntity.ok(new Response("User metrics updated successfully"));
    }

    /**
     * Retrieves the profile information of the authenticated user, including weight, height, email,
     * first name, last name, and username.
     *
     * @param authHeader The authorization header containing the user's JWT token.
     * @return A response entity containing the user's profile information.
     */
    @GetMapping("/profile")
    public ResponseEntity<?> getUserProfile(@RequestHeader("Authorization") String authHeader) {
        User user = getUserFromHeader(authHeader);
        double weight = user.getWeight();
        double height = user.getHeight();
        String email = user.getEmail();
        String firstName = user.getFirstName();
        String lastName = user.getLastName();
        String username = user.getUsername();
        UserProfileResponse response = new UserProfileResponse(weight, height, email, firstName, lastName, username);
        return ResponseEntity.ok(response);
    }

}