package ch.supsi.sporthub.backend.service.impl;

import ch.supsi.sporthub.backend.model.Chat;
import ch.supsi.sporthub.backend.model.ChatType;
import ch.supsi.sporthub.backend.model.User;
import ch.supsi.sporthub.backend.repository.jpa.ChatRepository;
import ch.supsi.sporthub.backend.repository.jpa.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class UserChatServiceImplTest {

    @Mock
    private ChatRepository chatRepository;
    @Mock
    private UserRepository userRepository;
    @InjectMocks
    private UserChatServiceImpl userChatService;

    private User user1;
    private User user2;
    private Chat chat1;
    private Chat chat2;
    private Chat groupChat;
    private final String chatId1 = "chat1";
    private final String groupChatId = "group1";
    private final String username1 = "user1";

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        user1 = new User();
        user1.setUsername(username1);
        user2 = new User();
        String username2 = "user2";
        user2.setUsername(username2);

        chat1 = new Chat();
        chat1.setChatId(chatId1);
        chat1.setUser(user1);
        chat1.setChatType(ChatType.PRIVATE);

        chat2 = new Chat();
        chat2.setChatId(chatId1);
        chat2.setUser(user2);
        chat2.setChatType(ChatType.PRIVATE);

        groupChat = new Chat();
        groupChat.setChatId(groupChatId);
        groupChat.setUser(user1);
        groupChat.setChatType(ChatType.GROUP);
    }

    @Test
    void testGetUsersInChat() {
        when(chatRepository.findByChatId(chatId1)).thenReturn(Optional.of(chat1));
        List<User> users = userChatService.getUsersInChat(chatId1);
        assertEquals(1, users.size());
        assertTrue(users.contains(user1));
        verify(chatRepository).findByChatId(chatId1);
    }

    @Test
    void testFindUserInChatByUsername_exists() {
        when(chatRepository.findByChatId(chatId1)).thenReturn(Optional.of(chat1));
        Optional<User> user = userChatService.findUserInChatByUsername(chatId1, username1);
        assertTrue(user.isPresent());
        assertEquals(user1, user.get());
        verify(chatRepository).findByChatId(chatId1);
    }

    @Test
    void testFindUserInChatByUsername_notExists() {
        when(chatRepository.findByChatId(chatId1)).thenReturn(Optional.of(chat1));
        Optional<User> user = userChatService.findUserInChatByUsername(chatId1, "nonExistentUser");
        assertFalse(user.isPresent());
        verify(chatRepository).findByChatId(chatId1);
    }


    @Test
    void testGetUserFromChat_exists() {
        when(chatRepository.findByChatId(chatId1)).thenReturn(Optional.ofNullable(chat1));
        User user = userChatService.getUserFromChat(chatId1);
        assertEquals(user1, user);
        verify(chatRepository).findByChatId(chatId1);
    }


    @Test
    void testGetChatsByChatId() {
        when(chatRepository.findAllByChatId(chatId1)).thenReturn(Arrays.asList(chat1, chat2));
        List<Chat> chats = userChatService.getChatsByChatId(chatId1);
        assertEquals(2, chats.size());
        assertTrue(chats.contains(chat1));
        assertTrue(chats.contains(chat2));
        verify(chatRepository).findAllByChatId(chatId1);
    }

    @Test
    void testGetChatsForUser() {
        when(chatRepository.findByUser(user1)).thenReturn(Arrays.asList(chat1, groupChat));
        List<Chat> chats = userChatService.getChatsForUser(user1);
        assertEquals(2, chats.size());
        assertTrue(chats.contains(chat1));
        assertTrue(chats.contains(groupChat));
        verify(chatRepository).findByUser(user1);
    }

    @Test
    void testSave() {
        userChatService.save(chat1);
        verify(chatRepository).save(chat1);
    }

    @Test
    void testFindUserByUsername_exists() {
        when(userRepository.findByUsername(username1)).thenReturn(Optional.of(user1));
        Optional<User> user = userChatService.findUserByUsername(username1);
        assertTrue(user.isPresent());
        assertEquals(user1, user.get());
        verify(userRepository).findByUsername(username1);
    }

    @Test
    void testFindUserByUsername_notExists() {
        when(userRepository.findByUsername("nonExistentUser")).thenReturn(Optional.empty());
        Optional<User> user = userChatService.findUserByUsername("nonExistentUser");
        assertFalse(user.isPresent());
        verify(userRepository).findByUsername("nonExistentUser");
    }

    @Test
    void testGetChatByChatId_exists() {
        when(chatRepository.findByChatId(chatId1)).thenReturn(Optional.of(chat1));
        Optional<Chat> chat = userChatService.getChatByChatId(chatId1);
        assertTrue(chat.isPresent());
        assertEquals(chat1, chat.get());
        verify(chatRepository).findByChatId(chatId1);
    }

    @Test
    void testGetChatByChatId_notExists() {
        when(chatRepository.findByChatId("nonExistentChat")).thenReturn(Optional.empty());
        Optional<Chat> chat = userChatService.getChatByChatId("nonExistentChat");
        assertFalse(chat.isPresent());
        verify(chatRepository).findByChatId("nonExistentChat");
    }

    @Test
    void testRemovePrivateChat_exists() {
        when(chatRepository.findByChatId(chatId1)).thenReturn(Optional.of(chat1));
        userChatService.removePrivateChat(chatId1);
        verify(chatRepository).findByChatId(chatId1);
        verify(chatRepository).delete(chat1);
    }

    @Test
    void testRemovePrivateChat_notExists() {
        when(chatRepository.findByChatId("nonExistentChat")).thenReturn(Optional.empty());
        userChatService.removePrivateChat("nonExistentChat");
        verify(chatRepository).findByChatId("nonExistentChat");
        verify(chatRepository, never()).delete(any());
    }

    @Test
    void testRemoveGroupChat() {
        userChatService.removeGroupChat(groupChat);
        verify(chatRepository).delete(groupChat);
    }

    @Test
    void testRemoveGroupChatForUser_existsAndMatches() {
        when(chatRepository.findByChatId(groupChatId)).thenReturn(Optional.of(groupChat));
        boolean removed = userChatService.removeGroupChatForUser(user1, groupChatId);
        assertTrue(removed);
        verify(chatRepository).findByChatId(groupChatId);
        verify(chatRepository).delete(groupChat);
    }

    @Test
    void testRemoveGroupChatForUser_existsButNotMatches() {
        User anotherUser = new User();
        anotherUser.setUsername("anotherUser");
        when(chatRepository.findByChatId(groupChatId)).thenReturn(Optional.of(groupChat));
        boolean removed = userChatService.removeGroupChatForUser(anotherUser, groupChatId);
        assertFalse(removed);
        verify(chatRepository).findByChatId(groupChatId);
        verify(chatRepository, never()).delete(any());
    }

    @Test
    void testRemoveGroupChatForUser_notExists() {
        when(chatRepository.findByChatId("nonExistentGroup")).thenReturn(Optional.empty());
        boolean removed = userChatService.removeGroupChatForUser(user1, "nonExistentGroup");
        assertFalse(removed);
        verify(chatRepository).findByChatId("nonExistentGroup");
        verify(chatRepository, never()).delete(any());
    }

    @Test
    void testHasPrivateChat_existsAndMatches() {
        when(chatRepository.findByChatId(chatId1)).thenReturn(Optional.of(chat1));
        boolean hasChat = userChatService.hasPrivateChat(chatId1, user1);
        assertTrue(hasChat);
        verify(chatRepository).findByChatId(chatId1);
    }

    @Test
    void testHasPrivateChat_existsButUserNotMatches() {
        when(chatRepository.findByChatId(chatId1)).thenReturn(Optional.of(chat1));
        boolean hasChat = userChatService.hasPrivateChat(chatId1, user2);
        assertFalse(hasChat);
        verify(chatRepository).findByChatId(chatId1);
    }

    @Test
    void testHasPrivateChat_existsButNotPrivate() {
        when(chatRepository.findByChatId(groupChatId)).thenReturn(Optional.of(groupChat));
        boolean hasChat = userChatService.hasPrivateChat(groupChatId, user1);
        assertFalse(hasChat);
        verify(chatRepository).findByChatId(groupChatId);
    }

    @Test
    void testHasPrivateChat_notExists() {
        when(chatRepository.findByChatId("nonExistentChat")).thenReturn(Optional.empty());
        boolean hasChat = userChatService.hasPrivateChat("nonExistentChat", user1);
        assertFalse(hasChat);
        verify(chatRepository).findByChatId("nonExistentChat");
    }

    @Test
    void getTelegramUsernameForUserInChat_ShouldReturnUsernameWhenExists() {
        String chatId = "chat123";
        user1.setId(1L);
        chat1.setTelegramUserName("telegramUser");

        when(chatRepository.findAllByChatId(chatId)).thenReturn(List.of(chat1));
        String result = userChatService.getTelegramUsernameForUserInChat(chatId, user1);
        assertEquals("telegramUser", result);
        verify(chatRepository).findAllByChatId(chatId);
    }

    @Test
    void getTelegramUsernameForUserInChat_ShouldReturnNullWhenNoMatchingUser() {
        String chatId = "chat123";
        user1.setId(1L);
        user2.setId(2L);
        chat1.setUser(user2);

        when(chatRepository.findAllByChatId(chatId)).thenReturn(List.of(chat1));
        String result = userChatService.getTelegramUsernameForUserInChat(chatId, user1);
        assertNull(result);
        verify(chatRepository).findAllByChatId(chatId);
    }

    @Test
    void getTelegramUsernameForUserInChat_ShouldReturnNullWhenUsernameEmpty() {
        String chatId = "chat123";
        user1.setId(1L);
        chat1.setTelegramUserName("");

        when(chatRepository.findAllByChatId(chatId)).thenReturn(List.of(chat1));
        String result = userChatService.getTelegramUsernameForUserInChat(chatId, user1);
        assertNull(result);
        verify(chatRepository).findAllByChatId(chatId);
    }

    @Test
    void getTelegramUsernameForUserInChat_ShouldReturnNullWhenUsernameNull() {
        String chatId = "chat123";
        user1.setId(1L);
        chat1.setTelegramUserName(null);

        when(chatRepository.findAllByChatId(chatId)).thenReturn(List.of(chat1));
        String result = userChatService.getTelegramUsernameForUserInChat(chatId, user1);
        assertNull(result);
        verify(chatRepository).findAllByChatId(chatId);
    }

    @Test
    void getTelegramUsernameForUserInChat_ShouldReturnNullWhenNoChats() {
        String chatId = "chat123";
        user1.setId(1L);

        when(chatRepository.findAllByChatId(chatId)).thenReturn(List.of());
        String result = userChatService.getTelegramUsernameForUserInChat(chatId, user1);
        assertNull(result);
        verify(chatRepository).findAllByChatId(chatId);
    }
}