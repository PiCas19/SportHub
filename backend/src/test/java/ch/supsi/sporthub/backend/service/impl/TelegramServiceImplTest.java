package ch.supsi.sporthub.backend.service.impl;

import ch.supsi.sporthub.backend.config.TelegramProperties;
import ch.supsi.sporthub.backend.dto.response.telegram.ChatInfoResponse;
import ch.supsi.sporthub.backend.service.api.IBaseApiService;
import ch.supsi.sporthub.backend.service.api.ICommandHandler;
import ch.supsi.sporthub.backend.service.api.ILeaderboardService;
import ch.supsi.sporthub.backend.utils.TelegramUpdateParser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class TelegramServiceImplTest {

    @Mock
    private IBaseApiService baseApiService;
    @Mock
    private TelegramProperties telegramProperties;
    @Mock
    private ICommandHandler commandHandler;
    @Mock
    private ILeaderboardService leaderboardService;
    @Mock
    private ChatValidationServiceImpl chatValidationService;

    @InjectMocks
    private TelegramServiceImpl telegramService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        when(telegramProperties.getBotToken()).thenReturn("test-token");
        when(telegramProperties.getBaseUrl()).thenReturn("https://api.telegram.org");
        when(telegramProperties.getBotUrlLink()).thenReturn("https://t.me/");
        when(telegramProperties.getBotUsername()).thenReturn("SportHubBot");
    }

    @Test
    void testSendMessage_Success() {
        when(baseApiService.postJson(any(), any(), any(), eq(String.class)))
                .thenReturn(new ResponseEntity<>("Message Sent", HttpStatus.OK));
        String response = telegramService.sendMessage("12345", "Hello");
        assertEquals("Message Sent", response);
    }


    @Test
    void testSendMessage_ClientError() {
        WebClientResponseException ex = mock(WebClientResponseException.class);
        when(ex.getResponseBodyAsString()).thenReturn("Bad Request");
        when(baseApiService.postJson(any(), any(), any(), eq(String.class)))
                .thenThrow(ex);
        String response = telegramService.sendMessage("12345", "Hello");
        assertTrue(response.contains("Failed to send message. Error: Bad Request"));
    }

    @Test
    void testSendMessage_UnexpectedException() {
        when(baseApiService.postJson(any(), any(), any(), eq(String.class)))
                .thenThrow(new RuntimeException("Internal error"));
        String response = telegramService.sendMessage("12345", "Hello");
        assertTrue(response.contains("Failed to send message due to unexpected error"));
    }

    @Test
    void testGetChatUpdates() {
        when(baseApiService.get(any(), any(), eq(String.class))).thenReturn(new ResponseEntity<>("updates", HttpStatus.OK));
        String result = telegramService.getChatUpdates();
        assertEquals("updates", result);
    }

    @Test
    void testGetBotLink() {
        assertEquals("https://t.me/SportHubBot", telegramService.getBotLink());
    }

    @Test
    void testGetGroupInviteLink() {
        assertEquals("https://t.me/SportHubBot?startgroup=true", telegramService.getGroupInviteLink());
    }

    @Test
    void testGetTelegramUsername_NullChatId() {
        assertNull(telegramService.getTelegramUsername(null));
    }

    @Test
    void testGetTelegramUsername_Success() {
        String responseJson = "{...}";
        when(baseApiService.get(any(), any(), eq(String.class))).thenReturn(new ResponseEntity<>(responseJson, HttpStatus.OK));

        try (MockedStatic<TelegramUpdateParser> mocked = mockStatic(TelegramUpdateParser.class)) {
            mocked.when(() -> TelegramUpdateParser.getTelegramUsernameFromResult(responseJson)).thenReturn("username");
            String result = telegramService.getTelegramUsername("12345");
            assertEquals("username", result);
        }
    }

    @Test
    void testGetTelegramUsername_Exception() {
        when(baseApiService.get(any(), any(), eq(String.class))).thenThrow(new RuntimeException("Error"));
        String result = telegramService.getTelegramUsername("12345");
        assertNull(result);
    }

    @Test
    void testVerifyChatId() {
        when(chatValidationService.verifyChatId("12345")).thenReturn(true);
        assertTrue(telegramService.verifyChatId("12345"));
    }

    @Test
    void testHandleUpdate_ChatIdNull() {
        try (MockedStatic<TelegramUpdateParser> mocked = mockStatic(TelegramUpdateParser.class)) {
            mocked.when(() -> TelegramUpdateParser.getChatId(anyString())).thenReturn(null);
            String result = telegramService.handleUpdate("update");
            assertEquals("Error: chatId is missing.", result);
        }
    }

    @Test
    void testHandleUpdate_BotAddedToGroup() {
        try (MockedStatic<TelegramUpdateParser> mocked = mockStatic(TelegramUpdateParser.class)) {
            mocked.when(() -> TelegramUpdateParser.getChatId(anyString())).thenReturn("12345");
            mocked.when(() -> TelegramUpdateParser.getChatTitle(anyString())).thenReturn("GroupTitle");
            mocked.when(() -> TelegramUpdateParser.isBotAddedToGroup(anyString())).thenReturn(true);

            when(leaderboardService.existsByName("GroupTitle")).thenReturn(false);
            doNothing().when(leaderboardService).createLeaderboard(anyString(), anyString());
            when(baseApiService.postJson(any(), any(), any(), eq(String.class))).thenReturn(new ResponseEntity<>("Welcome", HttpStatus.OK));

            String result = telegramService.handleUpdate("update");
            assertTrue(result.contains("Welcome"));
        }
    }

    @Test
    void testHandleUpdate_PrivateChatStartCommand() {
        try (MockedStatic<TelegramUpdateParser> mocked = mockStatic(TelegramUpdateParser.class)) {
            mocked.when(() -> TelegramUpdateParser.getChatId(anyString())).thenReturn("12345");
            mocked.when(() -> TelegramUpdateParser.getTelegramUsername(anyString())).thenReturn("user");
            mocked.when(() -> TelegramUpdateParser.getMessageText(anyString())).thenReturn("/start");
            mocked.when(() -> TelegramUpdateParser.getChatTitle(anyString())).thenReturn("title");
            mocked.when(() -> TelegramUpdateParser.getMessageId(anyString())).thenReturn("1");
            mocked.when(() -> TelegramUpdateParser.isBotAddedToGroup(anyString())).thenReturn(false);
            mocked.when(() -> TelegramUpdateParser.isPrivateChat(anyString())).thenReturn(true);

            when(baseApiService.postJson(any(), any(), any(), eq(String.class))).thenReturn(new ResponseEntity<>("Instructions", HttpStatus.OK));

            String result = telegramService.handleUpdate("update");
            assertTrue(result.contains("Instructions"));
        }
    }

    @Test
    void testHandleUpdate_RegularCommand() {
        try (MockedStatic<TelegramUpdateParser> mocked = mockStatic(TelegramUpdateParser.class)) {
            mocked.when(() -> TelegramUpdateParser.getChatId(anyString())).thenReturn("12345");
            mocked.when(() -> TelegramUpdateParser.getTelegramUsername(anyString())).thenReturn("user");
            mocked.when(() -> TelegramUpdateParser.getMessageText(anyString())).thenReturn("/help");
            mocked.when(() -> TelegramUpdateParser.getChatTitle(anyString())).thenReturn("title");
            mocked.when(() -> TelegramUpdateParser.getMessageId(anyString())).thenReturn("1");
            mocked.when(() -> TelegramUpdateParser.isBotAddedToGroup(anyString())).thenReturn(false);
            mocked.when(() -> TelegramUpdateParser.isPrivateChat(anyString())).thenReturn(false);

            when(commandHandler.handleCommand("12345", "user", "1", "/help")).thenReturn("Handled Command");
            when(baseApiService.postJson(any(), any(), any(), eq(String.class))).thenReturn(new ResponseEntity<>("Handled Command", HttpStatus.OK));

            String result = telegramService.handleUpdate("update");
            assertTrue(result.contains("Handled Command"));
        }
    }

    @Test
    void testGetChatInfo() {
        String chatJson = "chat-json";
        String countJson = "count-json";
        String adminsJson = "admins-json";

        when(baseApiService.get(any(), contains("getChat?chat_id"), eq(String.class))).thenReturn(new ResponseEntity<>(chatJson, HttpStatus.OK));
        when(baseApiService.get(any(), contains("getChatMemberCount"), eq(String.class))).thenReturn(new ResponseEntity<>(countJson, HttpStatus.OK));
        when(baseApiService.get(any(), contains("getChatAdministrators"), eq(String.class))).thenReturn(new ResponseEntity<>(adminsJson, HttpStatus.OK));

        try (MockedStatic<TelegramUpdateParser> mocked = mockStatic(TelegramUpdateParser.class)) {
            mocked.when(() -> TelegramUpdateParser.getChatTitleFromResponse(chatJson)).thenReturn("Title");
            mocked.when(() -> TelegramUpdateParser.getChatMemberCount(countJson)).thenReturn(10);
            mocked.when(() -> TelegramUpdateParser.getChatType(chatJson)).thenReturn("group");
            mocked.when(() -> TelegramUpdateParser.getChatDescription(chatJson)).thenReturn("Description");
            mocked.when(() -> TelegramUpdateParser.getChatPhotoSmall(chatJson)).thenReturn("small.jpg");
            mocked.when(() -> TelegramUpdateParser.getChatPhotoBig(chatJson)).thenReturn("big.jpg");
            mocked.when(() -> TelegramUpdateParser.getFilePathFromGetFileResponse(anyString())).thenReturn("files/path.jpg");
            mocked.when(() -> TelegramUpdateParser.getChatAdministrators(adminsJson)).thenReturn(List.of("admin1"));

            ChatInfoResponse response = telegramService.getChatInfo("12345");
            assertEquals("Title", response.getTitle());
            assertEquals("group", response.getChatType());
            assertEquals("Description", response.getDescription());
            assertEquals("admin1", response.getAdmins().getFirst());
        }
    }

    @Test
    void testSendMessage_NullChatId() {
        String result = telegramService.sendMessage(null, "Hello");
        assertEquals("Error: chatId is missing", result);
    }

    @Test
    void testSendMessage_EmptyChatId() {
        String result = telegramService.sendMessage("", "Hello");
        assertEquals("Error: chatId is missing", result);
    }

    @Test
    void testSendMessage_NullText() {
        String result = telegramService.sendMessage("12345", null);
        assertEquals("Error: text is empty", result);
    }

    @Test
    void testSendMessage_EmptyText() {
        String result = telegramService.sendMessage("12345", "");
        assertEquals("Error: text is empty", result);
    }

    @Test
    void testGetFileUrlFromTelegram_NullFileId() throws Exception {
        var method = TelegramServiceImpl.class.getDeclaredMethod("getFileUrlFromTelegram", String.class);
        method.setAccessible(true);
        String result = (String) method.invoke(telegramService, (String) null);
        assertNull(result);
    }

    @Test
    void testGetFileUrlFromTelegram_EmptyFileId() throws Exception {
        var method = TelegramServiceImpl.class.getDeclaredMethod("getFileUrlFromTelegram", String.class);
        method.setAccessible(true);
        String result = (String) method.invoke(telegramService, "");
        assertNull(result);
    }

    @Test
    void testGetFileUrlFromTelegram_WithValidFileId() throws Exception {
        String fileId = "file123";
        String filePath = "path/to/file.jpg";

        when(baseApiService.get(anyString(), contains("getFile"), eq(String.class))).thenReturn(new ResponseEntity<>("response", HttpStatus.OK));

        try (MockedStatic<TelegramUpdateParser> mocked = mockStatic(TelegramUpdateParser.class)) {
            mocked.when(() -> TelegramUpdateParser.getFilePathFromGetFileResponse("response")).thenReturn(filePath);

            var method = TelegramServiceImpl.class.getDeclaredMethod("getFileUrlFromTelegram", String.class);
            method.setAccessible(true);
            String result = (String) method.invoke(telegramService, fileId);

            assertTrue(result.contains(filePath));
            assertTrue(result.startsWith("https://api.telegram.org/file/bot"));
        }
    }

    @Test
    void testGetFileUrlFromTelegram_Exception() throws Exception {
        String fileId = "file123";
        when(baseApiService.get(anyString(), contains("getFile"), eq(String.class))).thenThrow(new RuntimeException("error"));

        var method = TelegramServiceImpl.class.getDeclaredMethod("getFileUrlFromTelegram", String.class);
        method.setAccessible(true);
        String result = (String) method.invoke(telegramService, fileId);

        assertNull(result);
    }

    @Test
    void testGetTelegramUsername_NullChatId_BranchCoverage() {
        String result = telegramService.getTelegramUsername(null);
        assertNull(result);
    }

    @Test
    void testGetTelegramUsername_EmptyChatId_BranchCoverage() {
        String result = telegramService.getTelegramUsername("");
        assertNull(result);
    }

    @Test
    void testHandleUpdate_PrivateChatStartCommand_BranchCoverage() {
        try (MockedStatic<TelegramUpdateParser> mocked = mockStatic(TelegramUpdateParser.class)) {
            String update = "updateJson";
            mocked.when(() -> TelegramUpdateParser.getChatId(update)).thenReturn("12345");
            mocked.when(() -> TelegramUpdateParser.getTelegramUsername(update)).thenReturn("testuser");
            mocked.when(() -> TelegramUpdateParser.getMessageText(update)).thenReturn("/start");
            mocked.when(() -> TelegramUpdateParser.getChatTitle(update)).thenReturn("groupTitle");
            mocked.when(() -> TelegramUpdateParser.getMessageId(update)).thenReturn("10");
            mocked.when(() -> TelegramUpdateParser.isBotAddedToGroup(update)).thenReturn(false);
            mocked.when(() -> TelegramUpdateParser.isPrivateChat(update)).thenReturn(true);

            when(baseApiService.postJson(anyString(), contains("sendMessage"), any(), eq(String.class)))
                    .thenReturn(new ResponseEntity<>("Registration instructions", HttpStatus.OK));

            String result = telegramService.handleUpdate(update);
            assertTrue(result.contains("Registration instructions"));
        }
    }

    @Test
    void testHandleUpdate_PrivateChatStartCommand_FalseBranch() {
        try (MockedStatic<TelegramUpdateParser> mocked = mockStatic(TelegramUpdateParser.class)) {
            String update = "updateJson";
            mocked.when(() -> TelegramUpdateParser.getChatId(update)).thenReturn("12345");
            mocked.when(() -> TelegramUpdateParser.getTelegramUsername(update)).thenReturn("testuser");
            mocked.when(() -> TelegramUpdateParser.getMessageText(update)).thenReturn("not_start");
            mocked.when(() -> TelegramUpdateParser.getChatTitle(update)).thenReturn("groupTitle");
            mocked.when(() -> TelegramUpdateParser.getMessageId(update)).thenReturn("10");
            mocked.when(() -> TelegramUpdateParser.isBotAddedToGroup(update)).thenReturn(false);
            mocked.when(() -> TelegramUpdateParser.isPrivateChat(update)).thenReturn(true);

            when(commandHandler.handleCommand("12345", "testuser", "10", "not_start")).thenReturn("Command handled");
            when(baseApiService.postJson(anyString(), contains("sendMessage"), any(), eq(String.class)))
                    .thenReturn(new ResponseEntity<>("Command handled", HttpStatus.OK));

            String result = telegramService.handleUpdate(update);
            assertEquals("Command handled", result);
        }
    }


    @Test
    void testGetFileUrlFromTelegram_FilePathNotNullNotEmpty() throws Exception {
        String fileId = "file123";
        String responseBody = "telegram-response-body";
        String filePath = "path/to/file.jpg";

        when(baseApiService.get(anyString(), contains("getFile"), eq(String.class)))
                .thenReturn(new ResponseEntity<>(responseBody, HttpStatus.OK));

        try (MockedStatic<TelegramUpdateParser> mocked = mockStatic(TelegramUpdateParser.class)) {
            mocked.when(() -> TelegramUpdateParser.getFilePathFromGetFileResponse(responseBody))
                    .thenReturn(filePath);

            var method = TelegramServiceImpl.class.getDeclaredMethod("getFileUrlFromTelegram", String.class);
            method.setAccessible(true);
            String result = (String) method.invoke(telegramService, fileId);

            assertNotNull(result);
            assertTrue(result.contains(filePath));
        }
    }

    @Test
    void testGetFileUrlFromTelegram_FilePathEmptyBranch() throws Exception {
        String fileId = "file123";
        String responseBody = "telegram-response-body";

        when(baseApiService.get(anyString(), contains("getFile"), eq(String.class)))
                .thenReturn(new ResponseEntity<>(responseBody, HttpStatus.OK));

        try (MockedStatic<TelegramUpdateParser> mocked = mockStatic(TelegramUpdateParser.class)) {
            mocked.when(() -> TelegramUpdateParser.getFilePathFromGetFileResponse(responseBody))
                    .thenReturn("");

            var method = TelegramServiceImpl.class.getDeclaredMethod("getFileUrlFromTelegram", String.class);
            method.setAccessible(true);
            String result = (String) method.invoke(telegramService, fileId);

            assertNull(result);
        }
    }

    @Test
    void testGetFileUrlFromTelegram_FilePathNullBranch() throws Exception {
        String fileId = "file123";
        String responseBody = "telegram-response-body";

        when(baseApiService.get(anyString(), contains("getFile"), eq(String.class)))
                .thenReturn(new ResponseEntity<>(responseBody, HttpStatus.OK));

        try (MockedStatic<TelegramUpdateParser> mocked = mockStatic(TelegramUpdateParser.class)) {
            mocked.when(() -> TelegramUpdateParser.getFilePathFromGetFileResponse(responseBody))
                    .thenReturn(null);

            var method = TelegramServiceImpl.class.getDeclaredMethod("getFileUrlFromTelegram", String.class);
            method.setAccessible(true);
            String result = (String) method.invoke(telegramService, fileId);

            assertNull(result);
        }
    }

    @Test
    void testCreateLeaderboardIfNotExists_ConditionTrue() throws Exception {
        String chatId = "12345";
        String chatTitle = "Test Group";

        when(leaderboardService.existsByName(chatTitle)).thenReturn(false);

        var method = TelegramServiceImpl.class.getDeclaredMethod("createLeaderboardIfNotExists", String.class, String.class);
        method.setAccessible(true);
        method.invoke(telegramService, chatId, chatTitle);

        verify(leaderboardService).createLeaderboard(chatId, chatTitle);
    }

    @Test
    void testCreateLeaderboardIfNotExists_ConditionFalse() throws Exception {
        String chatId = "12345";
        String chatTitle = "Test Group";

        when(leaderboardService.existsByName(chatTitle)).thenReturn(true);

        var method = TelegramServiceImpl.class.getDeclaredMethod("createLeaderboardIfNotExists", String.class, String.class);
        method.setAccessible(true);
        method.invoke(telegramService, chatId, chatTitle);

        verify(leaderboardService, never()).createLeaderboard(anyString(), anyString());
    }

}