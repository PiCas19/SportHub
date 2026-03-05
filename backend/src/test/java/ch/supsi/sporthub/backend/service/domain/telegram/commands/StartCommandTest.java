package ch.supsi.sporthub.backend.service.domain.telegram.commands;

import ch.supsi.sporthub.backend.model.Chat;
import ch.supsi.sporthub.backend.model.ChatType;
import ch.supsi.sporthub.backend.model.User;
import ch.supsi.sporthub.backend.service.api.IChatTypeFactory;
import ch.supsi.sporthub.backend.service.api.IUserChatService;
import ch.supsi.sporthub.backend.service.domain.telegram.TelegramCommandInput;
import ch.supsi.sporthub.backend.service.impl.ChatValidationServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class StartCommandTest {

    @Mock
    private IUserChatService userChatService;

    @Mock
    private IChatTypeFactory chatTypeFactory;

    @Mock
    private ChatValidationServiceImpl chatValidationService;

    @InjectMocks
    private StartCommand startCommand;

    private TelegramCommandInput input;
    private final String chatId = "-4602071182";
    private final String username = "test-user";
    private final String telegramName = "telegram-user";
    private final String messageId = "12345";

    @BeforeEach
    void setUp() {
        input = new TelegramCommandInput(chatId, telegramName, messageId, new String[0]);
    }

    @Test
    void execute_withRealGroupId_shouldRegisterSuccessfully() {
        String realGroupId = "-4602071182";
        input = new TelegramCommandInput(chatId, telegramName, messageId, new String[]{username, realGroupId});
        User user = new User();
        user.setUsername(username);

        when(userChatService.findUserByUsername(username)).thenReturn(Optional.of(user));
        when(chatTypeFactory.isChatType(chatId, ChatType.PRIVATE)).thenReturn(true);
        when(userChatService.getChatsByChatId(chatId)).thenReturn(Collections.emptyList());
        when(userChatService.getChatsByChatId(realGroupId)).thenReturn(Collections.emptyList());
        when(chatValidationService.verifyChatId(realGroupId)).thenReturn(true);
        lenient().when(chatTypeFactory.isChatType(realGroupId, ChatType.PRIVATE)).thenReturn(false);
        lenient().when(chatTypeFactory.isChatType(realGroupId, ChatType.GROUP)).thenReturn(true);

        when(chatValidationService.updateLeaderboards(realGroupId, username)).thenReturn(List.of("Group Leaderboard"));

        String result = startCommand.execute(input);
        String expectedPattern = ".*<b>✅ Group registration complete!</b>.*" +
                "• <code>" + realGroupId + "</code>.*" +
                "📊 <b>You have been added to the following leaderboards:</b>.*" +
                "• 🏆 <b>Group Leaderboard</b>.*";
        assertTrue(result.replaceAll("\\s+", " ").matches(expectedPattern),
                "Output should match expected format with group ID and leaderboard");
        assertTrue(result.contains("<b>✅ Group registration complete!</b>"),
                "Should show registration success message");
        assertTrue(result.contains("• <code>" + realGroupId + "</code>"),
                "Should show the registered group ID");
        assertTrue(result.contains("📊 <b>You have been added to the following leaderboards:</b>"),
                "Should show leaderboard section header");
        assertTrue(result.contains("• 🏆 <b>Group Leaderboard</b>"),
                "Should show the leaderboard name");
    }

    @Test
    void execute_withMultipleRealGroupIds_shouldHandleCorrectly() {
        String groupId1 = "-4602071182";
        String groupId2 = "-1001234567890";
        input = new TelegramCommandInput(chatId, telegramName, messageId, new String[]{username, groupId1, groupId2});
        User user = new User();
        user.setUsername(username);

        when(userChatService.findUserByUsername(username)).thenReturn(Optional.of(user));
        when(chatTypeFactory.isChatType(chatId, ChatType.PRIVATE)).thenReturn(true);
        when(userChatService.getChatsByChatId(chatId)).thenReturn(Collections.emptyList());

        when(userChatService.getChatsByChatId(groupId1)).thenReturn(Collections.emptyList());
        when(chatValidationService.verifyChatId(groupId1)).thenReturn(true);
        lenient().when(chatTypeFactory.isChatType(groupId1, ChatType.PRIVATE)).thenReturn(false);
        lenient().when(chatTypeFactory.isChatType(groupId1, ChatType.GROUP)).thenReturn(true);
        when(chatValidationService.updateLeaderboards(groupId1, username)).thenReturn(List.of("Group 1 Leaderboard"));

        when(userChatService.getChatsByChatId(groupId2)).thenReturn(Collections.emptyList());
        when(chatValidationService.verifyChatId(groupId2)).thenReturn(true);
        lenient().when(chatTypeFactory.isChatType(groupId2, ChatType.PRIVATE)).thenReturn(false);
        lenient().when(chatTypeFactory.isChatType(groupId2, ChatType.CHANNEL)).thenReturn(true);
        when(chatValidationService.updateLeaderboards(groupId2, username)).thenReturn(List.of("Group 2 Leaderboard"));

        String result = startCommand.execute(input);
        String expectedOutput = """
                <b>✅ Group registration complete!</b>
                • <code>-4602071182</code>
                • <code>-1001234567890</code>
                
                📊 <b>You have been added to the following leaderboards:</b>
                • 🏆 <b>Group 1 Leaderboard</b>
                • 🏆 <b>Group 2 Leaderboard</b>
                """.replaceAll("\n", "").replaceAll(" ", "");

        String actualOutput = result.replaceAll("\n", "").replaceAll(" ", "");

        assertTrue(actualOutput.contains(expectedOutput),
                "Output should contain all registered groups and leaderboards");
        assertTrue(result.contains("<b>✅ Group registration complete!</b>"));
        assertTrue(result.contains("• <code>" + groupId1 + "</code>"));
        assertTrue(result.contains("• <code>" + groupId2 + "</code>"));
        assertTrue(result.contains("📊 <b>You have been added to the following leaderboards:</b>"));
        assertTrue(result.contains("• 🏆 <b>Group 1 Leaderboard</b>"));
        assertTrue(result.contains("• 🏆 <b>Group 2 Leaderboard</b>"));
    }

    @Test
    void execute_withVeryLargeGroupId_shouldHandleCorrectly() {
        String largeGroupId = "-1001234567890";
        input = new TelegramCommandInput(chatId, telegramName, messageId, new String[]{username, largeGroupId});
        User user = new User();
        user.setUsername(username);

        when(userChatService.findUserByUsername(username)).thenReturn(Optional.of(user));
        when(chatTypeFactory.isChatType(chatId, ChatType.PRIVATE)).thenReturn(true);
        when(userChatService.getChatsByChatId(chatId)).thenReturn(Collections.emptyList());
        when(userChatService.getChatsByChatId(largeGroupId)).thenReturn(Collections.emptyList());
        when(chatValidationService.verifyChatId(largeGroupId)).thenReturn(true);
        lenient().when(chatTypeFactory.isChatType(largeGroupId, ChatType.PRIVATE)).thenReturn(false);
        lenient().when(chatTypeFactory.isChatType(largeGroupId, ChatType.GROUP)).thenReturn(true);
        lenient().when(chatValidationService.updateLeaderboards(largeGroupId, username)).thenReturn(List.of("Large Group Leaderboard"));

        String result = startCommand.execute(input);

        assertTrue(result.contains("<b>✅ Registration complete!</b>"));
        assertTrue(result.contains("• <code>" + largeGroupId + "</code>"));
    }

    @Test
    void getCommandName_shouldReturnCorrectCommand() {
        String commandName = startCommand.getCommandName();
        assertEquals("/start_notification", commandName,
                "Should return the correct command name");
    }

    @Test
    void isAllowedInChat_whenPrivateChat_shouldReturnTrue() {
        when(chatTypeFactory.isChatType(chatId, ChatType.PRIVATE)).thenReturn(true);
        boolean isAllowed = startCommand.isAllowedInChat(chatId);
        assertTrue(isAllowed, "Should allow in private chats");
        verify(chatTypeFactory).isChatType(chatId, ChatType.PRIVATE);
    }

    @Test
    void isAllowedInChat_whenGroupChat_shouldReturnFalse() {
        when(chatTypeFactory.isChatType(chatId, ChatType.PRIVATE)).thenReturn(false);
        boolean isAllowed = startCommand.isAllowedInChat(chatId);
        assertFalse(isAllowed, "Should not allow in group chats");
        verify(chatTypeFactory).isChatType(chatId, ChatType.PRIVATE);
    }

    @Test
    void isAllowedInChat_whenChannelChat_shouldReturnFalse() {
        when(chatTypeFactory.isChatType(chatId, ChatType.PRIVATE)).thenReturn(false);
        boolean isAllowed = startCommand.isAllowedInChat(chatId);
        assertFalse(isAllowed, "Should not allow in channel chats");
        verify(chatTypeFactory).isChatType(chatId, ChatType.PRIVATE);
    }

    @Test
    void execute_whenGroupChat_shouldResolveGroupType() throws Exception {
        String groupId = "-1001234567890";
        var input = new TelegramCommandInput("private-chat-id", "test-user", "123",
                new String[]{"username", groupId});
        User user = new User();

        when(userChatService.findUserByUsername(any())).thenReturn(Optional.of(user));
        when(chatTypeFactory.isChatType(any(), any())).thenReturn(false);
        when(chatTypeFactory.isChatType(groupId, ChatType.GROUP)).thenReturn(true);
        when(userChatService.getChatsByChatId(any())).thenReturn(Collections.emptyList());
        when(chatValidationService.verifyChatId(any())).thenReturn(true);
        when(chatValidationService.updateLeaderboards(any(), any())).thenReturn(Collections.emptyList());
        startCommand.execute(input);
        verify(chatTypeFactory).isChatType(groupId, ChatType.PRIVATE);
        verify(chatTypeFactory).isChatType(groupId, ChatType.GROUP);
        verify(chatTypeFactory, never()).isChatType(groupId, ChatType.CHANNEL);
    }

    @Test
    void execute_whenChannelChat_shouldResolveChannelType() throws Exception {
        String channelId = "-1009876543210";
        var input = new TelegramCommandInput("private-chat-id", "test-user", "123",
                new String[]{"username", channelId});
        User user = new User();

        when(userChatService.findUserByUsername(any())).thenReturn(Optional.of(user));
        when(chatTypeFactory.isChatType(any(), any())).thenReturn(false);
        when(chatTypeFactory.isChatType(channelId, ChatType.CHANNEL)).thenReturn(true);
        when(userChatService.getChatsByChatId(any())).thenReturn(Collections.emptyList());
        when(chatValidationService.verifyChatId(any())).thenReturn(true);
        when(chatValidationService.updateLeaderboards(any(), any())).thenReturn(Collections.emptyList());
        startCommand.execute(input);
        verify(chatTypeFactory).isChatType(channelId, ChatType.PRIVATE);
        verify(chatTypeFactory).isChatType(channelId, ChatType.GROUP);
        verify(chatTypeFactory).isChatType(channelId, ChatType.CHANNEL);
    }

    @Test
    void execute_withNoArguments_shouldReturnErrorMessage() {
        TelegramCommandInput input = new TelegramCommandInput("chatId", "telegramName", "12345", new String[]{});
        String result = startCommand.execute(input);
        String expectedMessage = "❌ <b>Error:</b> You must specify your username.\n<b>Correct usage:</b> /start_notification <code>&lt;username&gt; [group_id1 group_id2 ...]</code>";
        assertEquals(expectedMessage, result);
    }

    @Test
    void execute_withNonExistingUser_shouldReturnErrorMessage() {
        String nonExistingUsername = "non-existing-user";
        TelegramCommandInput input = new TelegramCommandInput("chatId", "telegramName", "12345", new String[]{nonExistingUsername});
        when(userChatService.findUserByUsername(nonExistingUsername)).thenReturn(Optional.empty());
        String result = startCommand.execute(input);
        String expectedMessage = "❌ <b>Error:</b> The user <b>" + nonExistingUsername + "</b> does not exist on SportHub.\nPlease register on SportHub first, then try again.";
        assertEquals(expectedMessage, result);
    }


    @Test
    void execute_withInvalidGroupIds_shouldReturnErrorMessage() {
        String username = "test-user";
        String chatId = "4602071182";
        String invalidGroupId1 = "invalid_group_1";
        String invalidGroupId2 = "invalid_group_2";

        TelegramCommandInput input = new TelegramCommandInput(chatId, "telegramName", "12345", new String[]{username, invalidGroupId1, invalidGroupId2});

        User user = new User();
        user.setUsername(username);
        when(userChatService.findUserByUsername(username)).thenReturn(Optional.of(user));
        when(userChatService.getChatsByChatId(chatId)).thenReturn(Collections.emptyList());
        lenient().when(chatValidationService.verifyChatId(invalidGroupId1)).thenReturn(false);
        lenient().when(chatValidationService.verifyChatId(invalidGroupId2)).thenReturn(false);
        String result = startCommand.execute(input);
        String expectedMessage = "\n\n❌ <b>Error:</b> The following group IDs are invalid:\n" +
                "• <code>" + invalidGroupId1 + "</code>\n" +
                "• <code>" + invalidGroupId2 + "</code>";
        assertTrue(result.contains(expectedMessage), "The response should contain error messages for invalid group IDs");
    }


    @Test
    void execute_WhenPrivateChatRegisteredWithAnotherUser_ShouldReturnError() {
        final String chatId = "12345";
        final String username = "testuser";
        final String telegramName = "testtelegram";
        final String telegramMessageId = "msg123";

        User currentUser = new User();
        currentUser.setUsername(username);

        User otherUser = new User();
        otherUser.setUsername("otheruser");
        Chat existingChat = new Chat();
        existingChat.setChatId(chatId);
        existingChat.setUser(otherUser);
        when(chatTypeFactory.isChatType(chatId, ChatType.PRIVATE)).thenReturn(true);
        when(userChatService.findUserByUsername(username)).thenReturn(Optional.of(currentUser));
        when(userChatService.getChatsByChatId(chatId)).thenReturn(Collections.singletonList(existingChat));
        TelegramCommandInput input = new TelegramCommandInput(
                chatId,
                telegramName,
                telegramMessageId,
                new String[]{username}
        );
        String result = startCommand.execute(input);
        String expectedError = "❌ <b>Error:</b> This private chat is already registered with another user.";
        assertEquals(expectedError, result);
    }

    @Test
    void execute_WhenGroupIdIsValid_ShouldProcessNormally() {
        final String validGroupId = "67890";
        String[] args = {"testuser", validGroupId};

        User user = new User();
        user.setUsername("testuser");

        when(chatTypeFactory.isChatType(any(), eq(ChatType.PRIVATE))).thenReturn(true);
        when(userChatService.findUserByUsername(any())).thenReturn(Optional.of(user));
        when(chatValidationService.verifyChatId(validGroupId)).thenReturn(true);

        TelegramCommandInput input = new TelegramCommandInput(
                "12345", "testuser", "msg123", args);
        String result = startCommand.execute(input);
        assertTrue(result.contains(validGroupId));
        assertFalse(result.contains("❌ <b>Error:</b> The following group IDs are invalid"));
        verify(userChatService).save(argThat(chat ->
                chat.getChatId().equals(validGroupId) &&
                        chat.getUser().equals(user)
        ));
    }

    @Test
    void execute_WhenPrivateChatRegisteredButWithNewGroups_ShouldShowBothMessages() {
        final String chatId = "12345";
        final String username = "testuser";
        final String telegramName = "testtelegram";
        final String telegramMessageId = "msg123";
        final String newGroupId = "67890";

        User currentUser = new User();
        currentUser.setUsername(username);
        Chat existingChat = new Chat();
        existingChat.setChatId(chatId);
        existingChat.setUser(currentUser);
        when(chatTypeFactory.isChatType(chatId, ChatType.PRIVATE)).thenReturn(true);
        when(userChatService.findUserByUsername(username)).thenReturn(Optional.of(currentUser));
        when(userChatService.getChatsByChatId(chatId)).thenReturn(Collections.singletonList(existingChat));
        when(chatValidationService.verifyChatId(newGroupId)).thenReturn(true);
        when(userChatService.getChatsByChatId(newGroupId)).thenReturn(Collections.emptyList());
        TelegramCommandInput input = new TelegramCommandInput(
                chatId,
                telegramName,
                telegramMessageId,
                new String[]{username, newGroupId}
        );

        String result = startCommand.execute(input);
        assertTrue(result.contains("⚠️ <b>Attention:</b> You are already registered in this private chat."));
        assertTrue(result.contains("👉 You have added the following new groups:"));
        assertTrue(result.contains(newGroupId));
        verify(userChatService).save(argThat(chat ->
                chat.getChatId().equals(newGroupId) &&
                        chat.getUser().equals(currentUser)
        ));
    }

    @Test
    void execute_WithAlreadyRegisteredGroups_ShouldShowWarningMessage() {
        final String chatId = "12345";
        final String username = "testuser";
        final String telegramName = "testtelegram";
        final String telegramMessageId = "msg123";
        final String registeredGroupId = "67890";

        User currentUser = new User();
        currentUser.setUsername(username);
        Chat existingPrivateChat = new Chat();
        existingPrivateChat.setChatId(chatId);
        existingPrivateChat.setUser(currentUser);
        Chat existingGroupChat = new Chat();
        existingGroupChat.setChatId(registeredGroupId);
        existingGroupChat.setUser(currentUser);

        when(chatTypeFactory.isChatType(chatId, ChatType.PRIVATE)).thenReturn(true);
        when(userChatService.findUserByUsername(username)).thenReturn(Optional.of(currentUser));
        when(userChatService.getChatsByChatId(chatId)).thenReturn(Collections.singletonList(existingPrivateChat));
        when(userChatService.getChatsByChatId(registeredGroupId)).thenReturn(Collections.singletonList(existingGroupChat));

        when(chatValidationService.verifyChatId(registeredGroupId)).thenReturn(true);
        TelegramCommandInput input = new TelegramCommandInput(
                chatId,
                telegramName,
                telegramMessageId,
                new String[]{username, registeredGroupId}
        );

        String result = startCommand.execute(input);

        String expectedWarning = "\n⚠️ <b>Attention:</b> You are already registered in the following groups:\n" +
                "• <code>" + registeredGroupId + "</code>";
        assertTrue(result.contains(expectedWarning));

        assertTrue(result.contains("• <code>" + registeredGroupId + "</code>"));
        verify(userChatService, never()).save(argThat(chat ->
                chat.getChatId().equals(registeredGroupId)
        ));
    }

    @Test
    void execute_WithMixedValidAndInvalidGroupIds_ShouldHandleBoth() {
        String validGroupId = "54321";
        String invalidFormatGroupId = "abc123";
        String invalidVerifiedGroupId = "67890";
        String telegramMessageId = "msg123";

        User testUser = new User();
        testUser.setUsername(username);

        when(userChatService.findUserByUsername(username)).thenReturn(Optional.of(testUser));

        when(chatTypeFactory.isChatType(chatId, ChatType.PRIVATE)).thenReturn(true);
        Chat existingPrivateChat = new Chat();
        existingPrivateChat.setChatId(chatId);
        existingPrivateChat.setUser(testUser);
        when(userChatService.getChatsByChatId(chatId)).thenReturn(Collections.singletonList(existingPrivateChat));

        when(chatValidationService.verifyChatId(validGroupId)).thenReturn(true);
        when(chatValidationService.verifyChatId(invalidVerifiedGroupId)).thenReturn(false);
        when(userChatService.getChatsByChatId(validGroupId)).thenReturn(Collections.emptyList());

        TelegramCommandInput input = new TelegramCommandInput(
                chatId,
                telegramName,
                telegramMessageId,
                new String[]{username, validGroupId, invalidFormatGroupId, invalidVerifiedGroupId}
        );

        String result = startCommand.execute(input);
        assertTrue(result.contains(validGroupId), "Valid group ID should appear in output");
        verify(userChatService).save(argThat(c ->
                c.getChatId().equals(validGroupId) &&
                        c.getUser().equals(testUser)
        ));

        assertTrue(result.contains("❌ <b>Error:</b> The following group IDs are invalid:"),
                "Should show invalid IDs message");
        assertTrue(result.contains("• <code>" + invalidFormatGroupId + "</code>"),
                "Should list format-invalid ID");
        assertTrue(result.contains("• <code>" + invalidVerifiedGroupId + "</code>"),
                "Should list validation-invalid ID");

        verify(userChatService, never()).save(argThat(c ->
                c.getChatId().equals(invalidFormatGroupId) ||
                        c.getChatId().equals(invalidVerifiedGroupId)
        ));
    }
}