package ch.supsi.sporthub.backend.utils;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class TelegramUpdateParserTest {

    private final String validJson = "{}";

    @Test
    void testGetChatId_ValidJson() {
        String json = "{ \"message\": { \"chat\": { \"id\": \"123456\" }, \"text\": \"Hello\" } }";
        assertEquals("123456", TelegramUpdateParser.getChatId(json));
    }

    @Test
    void testGetChatId_MissingChatId() {
        String json = "{ \"message\": { \"chat\": {} } }";
        assertEquals("", TelegramUpdateParser.getChatId(json));
    }

    @Test
    void testGetChatId_InvalidJson() {
        String json = "{ invalid json }";
        assertNull(TelegramUpdateParser.getChatId(json));
    }

    @Test
    void testGetChatId_NullOrEmptyInput() {
        assertNull(TelegramUpdateParser.getChatId(null));
        assertNull(TelegramUpdateParser.getChatId(""));
    }

    @Test
    void testGetMessageText_ValidJson() {
        String json = "{ \"message\": { \"chat\": { \"id\": \"123456\" }, \"text\": \"Hello\" } }";
        assertEquals("Hello", TelegramUpdateParser.getMessageText(json));
    }

    @Test
    void testGetMessageText_MissingTextField() {
        String json = "{ \"message\": { \"chat\": { \"id\": \"123456\" } } }";
        assertEquals("", TelegramUpdateParser.getMessageText(json));
    }

    @Test
    void testGetMessageText_InvalidJson() {
        assertNull(TelegramUpdateParser.getMessageText("{ invalid json }"));
    }

    @Test
    void testGetMessageText_NullOrEmptyInput() {
        assertNull(TelegramUpdateParser.getMessageText(null));
        assertNull(TelegramUpdateParser.getMessageText(""));
    }

    @Test
    void testGetChatTitle() {
        String json = "{ \"message\": { \"chat\": { \"title\": \"Test Group\" } } }";
        assertEquals("Test Group", TelegramUpdateParser.getChatTitle(json));
    }

    @Test
    void testGetChatTitle_InvalidJson() {
        assertNull(TelegramUpdateParser.getChatTitle("{ invalid json }"));
    }

    @Test
    void testGetChatTitle_NullOrEmptyInput() {
        assertNull(TelegramUpdateParser.getChatTitle(null));
        assertNull(TelegramUpdateParser.getChatTitle(""));
    }

    @Test
    void testIsBotAddedToGroup() {
        String json = "{ \"message\": { \"new_chat_member\": { \"is_bot\": true } } }";
        assertTrue(TelegramUpdateParser.isBotAddedToGroup(json));
    }

    @Test
    void testIsBotAddedToGroup_NotBot() {
        String json = "{ \"message\": { \"new_chat_member\": { \"is_bot\": false } } }";
        assertFalse(TelegramUpdateParser.isBotAddedToGroup(json));
    }

    @Test
    void testIsBotAddedToGroup_InvalidJsonOrNull() {
        assertFalse(TelegramUpdateParser.isBotAddedToGroup("{ invalid json }"));
        assertFalse(TelegramUpdateParser.isBotAddedToGroup(null));
        assertFalse(TelegramUpdateParser.isBotAddedToGroup(""));
    }

    @Test
    void testIsPrivateChat() {
        String json = "{ \"message\": { \"chat\": { \"type\": \"private\" } } }";
        assertTrue(TelegramUpdateParser.isPrivateChat(json));
    }

    @Test
    void testIsPrivateChat_NotPrivate() {
        String json = "{ \"message\": { \"chat\": { \"type\": \"group\" } } }";
        assertFalse(TelegramUpdateParser.isPrivateChat(json));
    }

    @Test
    void testIsPrivateChat_InvalidJsonOrNull() {
        assertFalse(TelegramUpdateParser.isPrivateChat("{ invalid json }"));
        assertFalse(TelegramUpdateParser.isPrivateChat(null));
        assertFalse(TelegramUpdateParser.isPrivateChat(""));
    }

    @Test
    void testGetTelegramUsername() {
        String json = "{ \"message\": { \"from\": { \"username\": \"user123\" } } }";
        assertEquals("user123", TelegramUpdateParser.getTelegramUsername(json));
    }

    @Test
    void testGetTelegramUsername_EmptyOrNullJson() {
        String json = "{ \"message\": { \"from\": { \"username\": \"\" } } }";
        assertEquals("unknown", TelegramUpdateParser.getTelegramUsername(json));
        assertEquals("unknown", TelegramUpdateParser.getTelegramUsername("{ invalid json }"));
        assertEquals("unknown", TelegramUpdateParser.getTelegramUsername(null));
        assertEquals("unknown", TelegramUpdateParser.getTelegramUsername(""));
    }

    @Test
    void testGetMessageId() {
        String json = "{ \"message\": { \"message_id\": \"777\" } }";
        assertEquals("777", TelegramUpdateParser.getMessageId(json));
    }

    @Test
    void testGetMessageId_InvalidJsonOrNull() {
        assertNull(TelegramUpdateParser.getMessageId("{ invalid json }"));
        assertNull(TelegramUpdateParser.getMessageId(null));
        assertNull(TelegramUpdateParser.getMessageId(""));
    }


    @Test
    void testGetFilePathFromGetFileResponse() {
        String json = "{ \"result\": { \"file_path\": \"photos/file_123.jpg\" } }";
        assertEquals("photos/file_123.jpg", TelegramUpdateParser.getFilePathFromGetFileResponse(json));
        assertNull(TelegramUpdateParser.getFilePathFromGetFileResponse("{ invalid }"));
        assertNull(TelegramUpdateParser.getFilePathFromGetFileResponse(null));
        assertNull(TelegramUpdateParser.getFilePathFromGetFileResponse(""));
    }

    @Test
    void testGetChatMemberCount() {
        String json = "{ \"result\": 5 }";
        assertEquals(5, TelegramUpdateParser.getChatMemberCount(json));
        assertEquals(0, TelegramUpdateParser.getChatMemberCount("{ invalid }"));
        assertEquals(0, TelegramUpdateParser.getChatMemberCount(null));
    }

    @Test
    void testGetChatTitleFromResponse() {
        String json = "{ \"result\": { \"title\": \"Sport Hub\" } }";
        assertEquals("Sport Hub", TelegramUpdateParser.getChatTitleFromResponse(json));
        assertNull(TelegramUpdateParser.getChatTitleFromResponse("{ invalid }"));
        assertNull(TelegramUpdateParser.getChatTitleFromResponse(null));
    }

    @Test
    void testGetChatType() {
        String json = "{ \"result\": { \"type\": \"group\" } }";
        assertEquals("group", TelegramUpdateParser.getChatType(json));
        assertNull(TelegramUpdateParser.getChatType("{ invalid }"));
        assertNull(TelegramUpdateParser.getChatType(null));
    }

    @Test
    void testGetChatDescription() {
        String json = "{ \"result\": { \"description\": \"This is a test group\" } }";
        assertEquals("This is a test group", TelegramUpdateParser.getChatDescription(json));
        assertNull(TelegramUpdateParser.getChatDescription("{ invalid }"));
        assertNull(TelegramUpdateParser.getChatDescription(null));
    }

    @Test
    void testGetChatPhotoSmall() {
        String json = "{ \"result\": { \"photo\": { \"small_file_id\": \"abc123\" } } }";
        assertEquals("abc123", TelegramUpdateParser.getChatPhotoSmall(json));
        assertNull(TelegramUpdateParser.getChatPhotoSmall("{ invalid }"));
        assertNull(TelegramUpdateParser.getChatPhotoSmall(null));
    }

    @Test
    void testGetChatPhotoBig() {
        String json = "{ \"result\": { \"photo\": { \"big_file_id\": \"big456\" } } }";
        assertEquals("big456", TelegramUpdateParser.getChatPhotoBig(json));
        assertNull(TelegramUpdateParser.getChatPhotoBig("{ invalid }"));
        assertNull(TelegramUpdateParser.getChatPhotoBig(null));
    }

    @Test
    void testGetChatAdministrators() {
        String json = """
            {
              "result": [
                { "user": { "username": "admin1" } },
                { "user": { "first_name": "NoUsername" } }
              ]
            }
            """;

        List<String> admins = TelegramUpdateParser.getChatAdministrators(json);
        assertEquals(2, admins.size());
        assertEquals("https://t.me/admin1", admins.get(0));
        assertEquals("NoUsername (no link available)", admins.get(1));

        assertTrue(TelegramUpdateParser.getChatAdministrators(null).isEmpty());
        assertTrue(TelegramUpdateParser.getChatAdministrators("{ invalid }").isEmpty());
    }

    @Test
    void testGetTelegramUsernameFromResult() {
        String json = "{ \"result\": { \"username\": \"bot_user\" } }";
        assertEquals("bot_user", TelegramUpdateParser.getTelegramUsernameFromResult(json));

        String emptyUsername = "{ \"result\": { \"username\": \"\" } }";
        assertEquals("unknown", TelegramUpdateParser.getTelegramUsernameFromResult(emptyUsername));

        assertEquals("unknown", TelegramUpdateParser.getTelegramUsernameFromResult("{ invalid }"));
        assertEquals("unknown", TelegramUpdateParser.getTelegramUsernameFromResult(null));
    }

    @Test
    void testNullAndEmptyUpdate() {
        assertNull(TelegramUpdateParser.getChatId(null));
        assertNull(TelegramUpdateParser.getChatId(""));
        assertEquals("", TelegramUpdateParser.getChatId("{\"message\":{\"chat\":{}}}"));
    }

    @Test
    void testChatAdministratorsIsArray() {
        String validAdmins = """
        { "result": [
            { "user": { "username": "admin1" } }
        ] }
        """;
        assertFalse(TelegramUpdateParser.getChatAdministrators(validAdmins).isEmpty());

        String notArray = """
        { "result": { "user": { "username": "admin1" } } }
        """;
        assertTrue(TelegramUpdateParser.getChatAdministrators(notArray).isEmpty());
    }

    @Test
    void testGetChatId_NullAndEmpty() {
        assertNull(TelegramUpdateParser.getChatId(null));
        assertNull(TelegramUpdateParser.getChatId(""));
        TelegramUpdateParser.getChatId(validJson);
    }

    @Test
    void testGetFilePathFromGetFileResponse_NullAndEmpty() {
        assertNull(TelegramUpdateParser.getFilePathFromGetFileResponse(null));
        assertNull(TelegramUpdateParser.getFilePathFromGetFileResponse(""));
        TelegramUpdateParser.getFilePathFromGetFileResponse(validJson);
    }

    @Test
    void testGetChatMemberCount_NullAndEmpty() {
        assertEquals(0, TelegramUpdateParser.getChatMemberCount(null));
        assertEquals(0, TelegramUpdateParser.getChatMemberCount(""));
        TelegramUpdateParser.getChatMemberCount(validJson);
    }

    @Test
    void testGetChatTitleFromResponse_NullAndEmpty() {
        assertNull(TelegramUpdateParser.getChatTitleFromResponse(null));
        assertNull(TelegramUpdateParser.getChatTitleFromResponse(""));
        TelegramUpdateParser.getChatTitleFromResponse(validJson);
    }

    @Test
    void testGetChatType_NullAndEmpty() {
        assertNull(TelegramUpdateParser.getChatType(null));
        assertNull(TelegramUpdateParser.getChatType(""));
        TelegramUpdateParser.getChatType(validJson);
    }

    @Test
    void testGetChatDescription_NullAndEmpty() {
        assertNull(TelegramUpdateParser.getChatDescription(null));
        assertNull(TelegramUpdateParser.getChatDescription(""));
        TelegramUpdateParser.getChatDescription(validJson);
    }

    @Test
    void testGetChatPhotoSmall_NullAndEmpty() {
        assertNull(TelegramUpdateParser.getChatPhotoSmall(null));
        assertNull(TelegramUpdateParser.getChatPhotoSmall(""));
        TelegramUpdateParser.getChatPhotoSmall(validJson);
    }

    @Test
    void testGetChatPhotoBig_NullAndEmpty() {
        assertNull(TelegramUpdateParser.getChatPhotoBig(null));
        assertNull(TelegramUpdateParser.getChatPhotoBig(""));
        TelegramUpdateParser.getChatPhotoBig(validJson);
    }

    @Test
    void testGetChatAdministrators_NullAndEmpty() {
        assertTrue(TelegramUpdateParser.getChatAdministrators(null).isEmpty());
        assertTrue(TelegramUpdateParser.getChatAdministrators("").isEmpty());
        TelegramUpdateParser.getChatAdministrators(validJson);
    }

    @Test
    void testGetChatTitle_NullAndEmpty() {
        assertNull(TelegramUpdateParser.getChatTitle(null));
        assertNull(TelegramUpdateParser.getChatTitle(""));
        TelegramUpdateParser.getChatTitle(validJson);
    }

    @Test
    void testGetMessageText_NullAndEmpty() {
        assertNull(TelegramUpdateParser.getMessageText(null));
        assertNull(TelegramUpdateParser.getMessageText(""));
        TelegramUpdateParser.getMessageText(validJson);
    }

    @Test
    void testIsBotAddedToGroup_NullAndEmpty() {
        assertFalse(TelegramUpdateParser.isBotAddedToGroup(null));
        assertFalse(TelegramUpdateParser.isBotAddedToGroup(""));
        TelegramUpdateParser.isBotAddedToGroup(validJson);
    }

    @Test
    void testIsPrivateChat_NullAndEmpty() {
        assertFalse(TelegramUpdateParser.isPrivateChat(null));
        assertFalse(TelegramUpdateParser.isPrivateChat(""));
        TelegramUpdateParser.isPrivateChat(validJson);
    }

    @Test
    void testGetTelegramUsernameFromResult_NullAndEmpty() {
        assertEquals("unknown", TelegramUpdateParser.getTelegramUsernameFromResult(null));
        assertEquals("unknown", TelegramUpdateParser.getTelegramUsernameFromResult(""));
        TelegramUpdateParser.getTelegramUsernameFromResult(validJson);
    }

    @Test
    void testGetTelegramUsername_NullAndEmpty() {
        assertEquals("unknown", TelegramUpdateParser.getTelegramUsername(null));
        assertEquals("unknown", TelegramUpdateParser.getTelegramUsername(""));
        TelegramUpdateParser.getTelegramUsername(validJson);
    }

    @Test
    void testGetMessageId_NullAndEmpty() {
        assertNull(TelegramUpdateParser.getMessageId(null));
        assertNull(TelegramUpdateParser.getMessageId(""));
        TelegramUpdateParser.getMessageId(validJson);
    }

    @Test
    void testUsernameNullOrEmpty() {
        String json = """
        { "result": [
            { "user": { "username": "admin1" } },
            { "user": { "first_name": "Luca" } },
            { "user": { "username": "" , "first_name": "Marco" } }
        ]}
        """;
        List<String> admins = TelegramUpdateParser.getChatAdministrators(json);
        assertEquals("https://t.me/admin1", admins.get(0));
        assertEquals("Luca (no link available)", admins.get(1));
        assertEquals("Marco (no link available)", admins.get(2));
    }

    @Test
    void testUsernameIsNullCondition() {
        String json = """
        {
          "result": [
            { "user": { "first_name": "Luca" } }
          ]
        }
        """;

        List<String> admins = TelegramUpdateParser.getChatAdministrators(json);
        assertEquals(1, admins.size());
        assertEquals("Luca (no link available)", admins.get(0));
    }

    @Test
    void testUsernameIsExplicitlyNull() {
        String json = """
        {
          "result": [
            { "user": { "first_name": "Luca" } }
          ]
        }
        """;

        List<String> admins = TelegramUpdateParser.getChatAdministrators(json);
        assertEquals(1, admins.size());
        assertEquals("Luca (no link available)", admins.get(0));
    }

    @Test
    void testUsernameIsEmpty() {
        String json = """
        {
          "result": [
            { "user": { "username": "", "first_name": "Marco" } }
          ]
        }
        """;

        List<String> admins = TelegramUpdateParser.getChatAdministrators(json);
        assertEquals(1, admins.size());
        assertEquals("Marco (no link available)", admins.get(0));
    }


}