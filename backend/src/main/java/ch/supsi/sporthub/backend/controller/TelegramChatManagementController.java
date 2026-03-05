package ch.supsi.sporthub.backend.controller;

import ch.supsi.sporthub.backend.dto.request.auth.AddChatRequest;
import ch.supsi.sporthub.backend.dto.response.Response;
import ch.supsi.sporthub.backend.dto.response.auth.ChatManagementResponse;
import ch.supsi.sporthub.backend.dto.response.telegram.ChatInfoResponse;
import ch.supsi.sporthub.backend.model.User;
import ch.supsi.sporthub.backend.service.JwtTokenService;
import ch.supsi.sporthub.backend.service.UserService;
import ch.supsi.sporthub.backend.service.api.ITelegramChatManagementService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * TelegramChatManagementController handles the management of Telegram chats for users.
 * It provides endpoints for adding, retrieving, and removing user chats in the system.
 */
@RestController
@RequestMapping("/api/telegram/chats")
public class TelegramChatManagementController extends BaseController {

    private final ITelegramChatManagementService chatManagementService;

    /**
     * Constructs an instance of TelegramChatManagementController with the necessary services.
     *
     * @param chatManagementService The service for managing Telegram chats.
     * @param userService           The service for handling user-related operations.
     * @param jwtTokenService       The service for handling JWT token operations.
     */
    public TelegramChatManagementController(ITelegramChatManagementService chatManagementService,
                                            UserService userService,
                                            JwtTokenService jwtTokenService) {
        super(jwtTokenService, userService);
        this.chatManagementService = chatManagementService;
    }

    /**
     * Adds a chat to the user's list of Telegram chats.
     * This method requires the user to be authenticated and provides the chat ID in the request body.
     *
     * @param authHeader The authorization header containing the JWT token of the user.
     * @param request    The request body containing the chat ID to be added.
     * @return A response entity containing the result of adding the chat.
     */
    @PostMapping
    public ResponseEntity<ChatManagementResponse> addChat(@RequestHeader("Authorization") String authHeader,
                                                          @RequestBody AddChatRequest request) {
        User user = getUserFromHeader(authHeader);
        return chatManagementService.addChat(user, request.getChatId());
    }

    /**
     * Retrieves all the chats associated with the authenticated user.
     * This method requires the user to be authenticated and retrieves the user's group chat IDs.
     *
     * @param authHeader The authorization header containing the JWT token of the user.
     * @return A response entity containing a map of chat IDs associated with the user.
     */
    @GetMapping
    public ResponseEntity<Map<String, List<ChatInfoResponse>>> getUserChats(@RequestHeader("Authorization") String authHeader) {
        User user = getUserFromHeader(authHeader);
        List<ChatInfoResponse> chats = chatManagementService.getUserGroupChatIds(user);
        return ResponseEntity.ok(Map.of("chats", chats));
    }

    /**
     * Removes a chat from the user's list of Telegram chats.
     * This method requires the user to be authenticated and the chat ID to be specified in the URL path.
     *
     * @param authHeader The authorization header containing the JWT token of the user.
     * @param id         The ID of the chat to be removed.
     * @return A response entity confirming that the chat was successfully removed.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Response> removeChat(@RequestHeader("Authorization") String authHeader,
                                               @PathVariable("id") Long id) {
        try {
            User user = getUserFromHeader(authHeader);
            chatManagementService.removeChat(user, id);
            return ResponseEntity.ok(new Response("Chat successfully removed"));
        } catch (IllegalStateException ex) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new Response(ex.getMessage()));
        }
    }
}