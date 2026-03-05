package ch.supsi.sporthub.backend.controller;

import ch.supsi.sporthub.backend.model.User;
import ch.supsi.sporthub.backend.service.EmailService;
import ch.supsi.sporthub.backend.service.JwtTokenService;
import ch.supsi.sporthub.backend.service.UserService;
import ch.supsi.sporthub.backend.service.api.ITelegramService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * TelegramController provides API endpoints to interact with Telegram bots and manage Telegram-related operations.
 * This includes sending messages, handling updates, and sending invitation emails to users and groups.
 */
@RestController
@RequestMapping("/api/telegram")
public class TelegramController extends BaseController {

    private final ITelegramService telegramService;
    private final EmailService emailService;

    /**
     * Constructs an instance of TelegramController with the necessary services.
     *
     * @param telegramService The service for interacting with Telegram bots and chat updates.
     * @param emailService    The service for sending email invitations.
     * @param jwtTokenService The service for handling JWT token operations.
     * @param userService     The service for handling user-related operations.
     */
    public TelegramController(ITelegramService telegramService,
                              EmailService emailService,
                              JwtTokenService jwtTokenService,
                              UserService userService) {
        super(jwtTokenService, userService);
        this.telegramService = telegramService;
        this.emailService = emailService;
    }

    /**
     * Sends a message to a specified Telegram chat.
     *
     * @param chatId The ID of the Telegram chat.
     * @param text   The message text to send.
     * @return A response entity containing the result of the send message operation.
     */
    @PostMapping("/send")
    public ResponseEntity<String> sendMessage(@RequestParam String chatId,
                                              @RequestParam String text) {
        return ResponseEntity.ok(telegramService.sendMessage(chatId, text));
    }

    /**
     * Retrieves the latest updates from a Telegram chat.
     *
     * @return A response entity containing the chat updates in string format.
     */
    @GetMapping("/getUpdates")
    public ResponseEntity<String> getChatUpdates() {
        return ResponseEntity.ok(telegramService.getChatUpdates());
    }

    /**
     * Sends an invitation email to the authenticated user with a link to invite the user to the Telegram bot.
     *
     * @param authHeader The authorization header containing the JWT token of the user.
     * @return A response entity confirming that the Telegram bot invitation email has been sent.
     */
    @PostMapping("/invite/user")
    public ResponseEntity<String> sendUserInviteEmail(@RequestHeader("Authorization") String authHeader) {
        User user = getUserFromHeader(authHeader);
        String inviteLink = telegramService.getBotLink();
        emailService.sendInviteTelegramBot(user.getEmail(), inviteLink);
        return ResponseEntity.accepted().body("Telegram bot invitation successfully sent to " + user.getEmail());
    }

    /**
     * Sends an invitation email to the authenticated user with a link to invite the group to the Telegram bot.
     *
     * @param authHeader The authorization header containing the JWT token of the user.
     * @return A response entity confirming that the group Telegram bot invitation email has been sent.
     */
    @PostMapping("/invite/group")
    public ResponseEntity<String> sendGroupInviteEmail(@RequestHeader("Authorization") String authHeader) {
        User user = getUserFromHeader(authHeader);
        String inviteLink = telegramService.getGroupInviteLink();
        emailService.sendInviteTelegramBot(user.getEmail(), inviteLink);
        return ResponseEntity.accepted().body("Invitation to add bot to a group successfully sent to " + user.getEmail());
    }

    /**
     * Handles incoming Telegram updates (webhook data) and processes the update.
     *
     * @param update The update data sent by Telegram.
     * @return A response entity containing the result of the update processing.
     */
    @PostMapping("/update")
    public ResponseEntity<String> handleUpdate(@RequestBody String update) {
        return ResponseEntity.ok(telegramService.handleUpdate(update));
    }
}