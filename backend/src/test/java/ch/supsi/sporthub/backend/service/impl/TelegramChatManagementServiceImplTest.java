package ch.supsi.sporthub.backend.service.impl;

import ch.supsi.sporthub.backend.dto.response.auth.ChatManagementResponse;
import ch.supsi.sporthub.backend.dto.response.telegram.ChatInfoResponse;
import ch.supsi.sporthub.backend.model.*;
import ch.supsi.sporthub.backend.repository.jpa.ChatRepository;
import ch.supsi.sporthub.backend.repository.jpa.LeaderboardRepository;
import ch.supsi.sporthub.backend.service.api.IChatTypeFactory;
import ch.supsi.sporthub.backend.service.api.ITelegramService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class TelegramChatManagementServiceImplTest {

    @Mock
    private ChatRepository chatRepository;
    @Mock
    private LeaderboardRepository leaderboardRepository;
    @Mock
    private IChatTypeFactory chatTypeFactory;
    @Mock
    private ITelegramService telegramService;

    @InjectMocks
    private TelegramChatManagementServiceImpl service;

    private User user;
    private Chat privateChat;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        user = new User();
        user.setUsername("testuser");

        privateChat = new Chat();
        privateChat.setChatId("123");
        privateChat.setChatType(ChatType.PRIVATE);
        privateChat.setUser(user);
        privateChat.setTelegramUserName("telegram_user");
    }

    @Test
    void testGetUserGroupChatIds() {
        Chat groupChat = new Chat();
        groupChat.setChatId("456");
        groupChat.setChatType(ChatType.GROUP);
        groupChat.setId(99L);

        when(chatRepository.findByUser(user)).thenReturn(List.of(groupChat));

        ChatInfoResponse info = new ChatInfoResponse(
                "Chat info",
                "My Group",
                10,
                "456",
                99L,
                "Test Description",
                "small.jpg",
                "big.jpg",
                List.of("admin1", "admin2"),
                "group"
        );

        when(telegramService.getChatInfo("456")).thenReturn(info);

        List<ChatInfoResponse> result = service.getUserGroupChatIds(user);
        assertEquals(1, result.size());
        assertEquals(99L, result.getFirst().getId());
        assertEquals("My Group", result.getFirst().getTitle());
        assertEquals("456", result.getFirst().getChatId());
    }


    @Test
    void testRemoveChat_NotFound() {
        when(chatRepository.findById(1L)).thenReturn(Optional.empty());
        assertThrows(IllegalStateException.class, () -> service.removeChat(user, 1L));
    }

    @Test
    void testRemoveChat_NotOwned() {
        Chat otherChat = new Chat();
        otherChat.setUser(new User());
        when(chatRepository.findById(1L)).thenReturn(Optional.of(otherChat));
        assertThrows(IllegalStateException.class, () -> service.removeChat(user, 1L));
    }

    @Test
    void testRemoveChat_Success() {
        Chat chat = new Chat();
        chat.setChatId("999");
        chat.setUser(user);

        Leaderboard leaderboard = new Leaderboard();
        Map<String, Integer> scores = new HashMap<>();
        scores.put("testuser", 100);
        leaderboard.setScores(scores);

        when(chatRepository.findById(1L)).thenReturn(Optional.of(chat));
        when(leaderboardRepository.findByChatId("999")).thenReturn(Optional.of(leaderboard));

        service.removeChat(user, 1L);

        assertFalse(leaderboard.getScores().containsKey("testuser"));
        verify(chatRepository).delete(chat);
        verify(leaderboardRepository).save(leaderboard);
    }

    @Test
    void testAddChat_NoPrivateChat() {
        when(chatRepository.findByUser(user)).thenReturn(Collections.emptyList());

        ResponseEntity<ChatManagementResponse> response = service.addChat(user, "group_chat_id");
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    void testAddChat_InvalidChatId() {
        when(chatRepository.findByUser(user)).thenReturn(List.of(privateChat));
        when(telegramService.verifyChatId("group_chat_id")).thenReturn(false);

        ResponseEntity<ChatManagementResponse> response = service.addChat(user, "group_chat_id");
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    void testAddChat_AlreadyExists() {
        when(chatRepository.findByUser(user)).thenReturn(List.of(privateChat));
        when(telegramService.verifyChatId("group_chat_id")).thenReturn(true);
        when(chatRepository.findByChatIdAndUser("group_chat_id", user)).thenReturn(Optional.of(new Chat()));

        ResponseEntity<ChatManagementResponse> response = service.addChat(user, "group_chat_id");
        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
    }

    @Test
    void testAddChat_UnsupportedChatType() {
        when(chatRepository.findByUser(user)).thenReturn(List.of(privateChat));
        when(telegramService.verifyChatId("group_chat_id")).thenReturn(true);
        when(chatRepository.findByChatIdAndUser("group_chat_id", user)).thenReturn(Optional.empty());
        when(chatTypeFactory.isChatType("group_chat_id", ChatType.GROUP)).thenReturn(false);
        when(chatTypeFactory.isChatType("group_chat_id", ChatType.CHANNEL)).thenReturn(false);

        ResponseEntity<ChatManagementResponse> response = service.addChat(user, "group_chat_id");
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    void testAddChat_SuccessGroupChat() {
        when(chatRepository.findByUser(user)).thenReturn(List.of(privateChat));
        when(telegramService.verifyChatId("group_chat_id")).thenReturn(true);
        when(chatRepository.findByChatIdAndUser("group_chat_id", user)).thenReturn(Optional.empty());
        when(chatTypeFactory.isChatType("group_chat_id", ChatType.GROUP)).thenReturn(true);

        ResponseEntity<ChatManagementResponse> response = service.addChat(user, "group_chat_id");
        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(chatRepository).save(any(Chat.class));
    }

    @Test
    void testAddChat_SuccessChannelChat() {
        when(chatRepository.findByUser(user)).thenReturn(List.of(privateChat));
        when(telegramService.verifyChatId("group_chat_id")).thenReturn(true);
        when(chatRepository.findByChatIdAndUser("group_chat_id", user)).thenReturn(Optional.empty());
        when(chatTypeFactory.isChatType("group_chat_id", ChatType.GROUP)).thenReturn(false);
        when(chatTypeFactory.isChatType("group_chat_id", ChatType.CHANNEL)).thenReturn(true);

        ResponseEntity<ChatManagementResponse> response = service.addChat(user, "group_chat_id");
        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(chatRepository).save(any(Chat.class));
    }

    @Test
    void testAddChat_HasPrivateChatTrue() {
        when(chatRepository.findByUser(user)).thenReturn(List.of(privateChat));
        when(telegramService.verifyChatId("456")).thenReturn(false);

        ResponseEntity<ChatManagementResponse> response = service.addChat(user, "456");

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertTrue(Objects.requireNonNull(response.getBody()).getMessage().contains("chat id del gruppo/canale non è valido"));
    }

    @Test
    void testAddChat_HasPrivateChatFalse() {
        Chat groupChat = new Chat();
        groupChat.setChatId("789");
        groupChat.setChatType(ChatType.GROUP);

        when(chatRepository.findByUser(user)).thenReturn(List.of(groupChat));

        ResponseEntity<ChatManagementResponse> response = service.addChat(user, "789");

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertTrue(Objects.requireNonNull(response.getBody()).getMessage().contains("prima registrarti"));
    }
    @Test
    void testGetUserGroupChatIds_IgnoresPrivateChats() {
        Chat privateChat = new Chat();
        privateChat.setChatId("999");
        privateChat.setChatType(ChatType.PRIVATE);
        privateChat.setId(77L);

        when(chatRepository.findByUser(user)).thenReturn(List.of(privateChat));

        List<ChatInfoResponse> result = service.getUserGroupChatIds(user);

        assertTrue(result.isEmpty());
        verify(telegramService, never()).getChatInfo(anyString());
    }

    @Test
    void testAddChat_TelegramUsernameFallbackToUnknown() {
        Chat privateChat = new Chat();
        privateChat.setChatId("999");
        privateChat.setChatType(ChatType.PRIVATE);
        privateChat.setUser(user);
        privateChat.setTelegramUserName("unknown");

        when(chatRepository.findByUser(user)).thenReturn(List.of(privateChat));
        when(telegramService.verifyChatId("888")).thenReturn(true);
        when(chatRepository.findByChatIdAndUser("888", user)).thenReturn(Optional.empty());
        when(chatTypeFactory.isChatType("888", ChatType.GROUP)).thenReturn(true);

        ResponseEntity<ChatManagementResponse> response = service.addChat(user, "888");

        assertEquals(HttpStatus.OK, response.getStatusCode());

        verify(chatRepository).save(argThat(chat -> "unknown".equals(chat.getTelegramUserName())));
    }

    @Test
    void testAddChat_NoPrivateChat_FallbackUsername() {
        Chat groupChat = new Chat();
        groupChat.setChatId("123");
        groupChat.setChatType(ChatType.GROUP);
        groupChat.setUser(user);
        groupChat.setTelegramUserName("group_user");

        when(chatRepository.findByUser(user)).thenReturn(List.of(groupChat));
        when(telegramService.verifyChatId("999")).thenReturn(true);
        when(chatRepository.findByChatIdAndUser("999", user)).thenReturn(Optional.empty());
        when(chatTypeFactory.isChatType("999", ChatType.GROUP)).thenReturn(true);

        Chat privateChat = new Chat();
        privateChat.setChatId("p1");
        privateChat.setChatType(ChatType.PRIVATE);
        privateChat.setUser(user);
        privateChat.setTelegramUserName("telegram_user");

        when(chatRepository.findByUser(user)).thenReturn(List.of(privateChat, groupChat));

        ResponseEntity<ChatManagementResponse> response = service.addChat(user, "999");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(chatRepository).save(argThat(chat -> chat.getTelegramUserName() != null));
    }

}
