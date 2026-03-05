package ch.supsi.sporthub.backend.service.impl;

import ch.supsi.sporthub.backend.dto.response.auth.ChatManagementResponse;
import ch.supsi.sporthub.backend.dto.response.telegram.ChatInfoResponse;
import ch.supsi.sporthub.backend.model.Chat;
import ch.supsi.sporthub.backend.model.ChatType;
import ch.supsi.sporthub.backend.model.User;
import ch.supsi.sporthub.backend.repository.jpa.ChatRepository;
import ch.supsi.sporthub.backend.repository.jpa.LeaderboardRepository;
import ch.supsi.sporthub.backend.service.api.IChatTypeFactory;
import ch.supsi.sporthub.backend.service.api.ITelegramChatManagementService;
import ch.supsi.sporthub.backend.service.api.ITelegramService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Implementation of the ITelegramChatManagementService interface that handles the management of user chats in Telegram.
 * This service includes functionalities to add, remove, and list user chats, as well as interact with the Telegram API to
 * retrieve chat information and manage chat-related data in the system.
 */
@Service
public class TelegramChatManagementServiceImpl implements ITelegramChatManagementService {

    private final ChatRepository chatRepository;
    private final LeaderboardRepository leaderboardRepository;
    private final ITelegramService telegramService;
    private final IChatTypeFactory chatTypeFactory;

    /**
     * Constructor for the TelegramChatManagementServiceImpl class.
     * Initializes the service with the provided repositories and services required to manage chats.
     *
     * @param chatRepository       The repository for accessing and managing chats.
     * @param leaderboardRepository The repository for accessing and managing leaderboards.
     * @param chatTypeFactory      The factory for determining chat types (e.g., GROUP, CHANNEL).
     * @param telegramService      The service for interacting with the Telegram API.
     */
    public TelegramChatManagementServiceImpl(ChatRepository chatRepository,
                                             LeaderboardRepository leaderboardRepository,
                                             IChatTypeFactory chatTypeFactory, ITelegramService telegramService) {
        this.chatRepository = chatRepository;
        this.leaderboardRepository = leaderboardRepository;
        this.telegramService = telegramService;
        this.chatTypeFactory = chatTypeFactory;
    }

    /**
     * Retrieves a list of group chat information associated with the given user.
     * Only group chats (not private chats) are included in the response.
     *
     * @param user The user whose group chats are being retrieved.
     * @return A list of ChatInfoResponse objects representing the user's group chats.
     */
    @Override
    public List<ChatInfoResponse> getUserGroupChatIds(User user) {
        return chatRepository.findByUser(user)
                .stream()
                .filter(chat -> chat.getChatType() == ChatType.GROUP)
                .map(chat -> {
                    ChatInfoResponse info = telegramService.getChatInfo(chat.getChatId());
                    info.setId(chat.getId());
                  return  info;
                })
                .collect(Collectors.toList());
    }


    /**
     * Removes a chat associated with the given user and chat ID.
     * This includes removing the chat from the user's chat list and updating the leaderboard if necessary.
     *
     * @param user The user requesting to remove a chat.
     * @param id   The ID of the chat to remove.
     * @throws IllegalStateException if the chat is not found or not associated with the user.
     */
    @Override
    public void removeChat(User user, Long id) {
        Optional<Chat> chatOpt = chatRepository.findById(id);
        if (chatOpt.isEmpty() || !chatOpt.get().getUser().equals(user)) {
            throw new IllegalStateException("Chat non trovata o non associata a te.");
        }
        Chat chat = chatOpt.get();
        leaderboardRepository.findByChatId(chat.getChatId()).ifPresent(leaderboard -> {
            leaderboard.getScores().remove(user.getUsername());
            leaderboardRepository.save(leaderboard);
        });
        chatRepository.delete(chat);
    }


    /**
     * Adds a new chat for the given user, based on the provided chat ID.
     * It ensures that the user is registered with Telegram and the chat ID is valid.
     *
     * @param user    The user adding a new chat.
     * @param chatId  The chat ID to add.
     * @return A response indicating the success or failure of the operation.
     */
    @Override
    public ResponseEntity<ChatManagementResponse> addChat(User user, String chatId) {

        List<Chat> userChats = chatRepository.findByUser(user);
        boolean hasPrivateChat = userChats.stream()
                .anyMatch(chat -> chat.getChatType() == ChatType.PRIVATE);

        if (!hasPrivateChat) {
            return ResponseEntity.badRequest()
                    .body(new ChatManagementResponse(
                            "Devi prima registrarti su Telegram tramite il bot di SportHub usando il comando /start_notification.",
                            false,
                            HttpStatus.BAD_REQUEST.value()));
        }

        if (!telegramService.verifyChatId(chatId)) {
            return ResponseEntity.badRequest()
                    .body(new ChatManagementResponse("Il chat id del gruppo/canale non è valido.", false, HttpStatus.BAD_REQUEST.value()));
        }

        Optional<Chat> existingChat = chatRepository.findByChatIdAndUser(chatId, user);
        if (existingChat.isPresent()) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(new ChatManagementResponse("La chat è già stata aggiunta per questo utente.", false, HttpStatus.CONFLICT.value()));
        }

        String telegramUsername = userChats.stream()
                .filter(chat -> chat.getChatType() == ChatType.PRIVATE)
                .map(Chat::getTelegramUserName)
                .findFirst()
                .orElse("unknown");

        ChatType chatType;
        if (chatTypeFactory.isChatType(chatId, ChatType.GROUP)) {
            chatType = ChatType.GROUP;
        } else if (chatTypeFactory.isChatType(chatId, ChatType.CHANNEL)) {
            chatType = ChatType.CHANNEL;
        } else {
            return ResponseEntity.badRequest()
                    .body(new ChatManagementResponse("Tipo di chat non supportato per il chat id fornito.", false, HttpStatus.BAD_REQUEST.value()));
        }

        Chat chat = new Chat();
        chat.setChatId(chatId);
        chat.setChatType(chatType);
        chat.setUser(user);
        chat.setTelegramUserName(telegramUsername);

        chatRepository.save(chat);

        return ResponseEntity.ok(new ChatManagementResponse("Chat aggiunta con successo.", true, HttpStatus.OK.value()));
    }


}