package ch.supsi.sporthub.backend.dto.response.telegram;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class ChatInfoResponseTest {

    @Test
    void testConstructorAndGetters() {
        ChatInfoResponse response = new ChatInfoResponse(
                "Fetched",
                "Test Chat",
                42,
                "123456",
                987L,
                "This is a test chat",
                "small.jpg",
                "big.jpg",
                List.of("admin1", "admin2"),
                "group"
        );

        assertEquals("Fetched", response.getMessage());
        assertEquals("Test Chat", response.getTitle());
        assertEquals(42, response.getMemberCount());
        assertEquals("123456", response.getChatId());
        assertEquals(987L, response.getId());
        assertEquals("This is a test chat", response.getDescription());
        assertEquals("small.jpg", response.getPhotoSmall());
        assertEquals("big.jpg", response.getPhotoBig());
        assertEquals(List.of("admin1", "admin2"), response.getAdmins());
        assertEquals("group", response.getChatType());
    }

    @Test
    void testEqualsAndHashCode() {
        ChatInfoResponse r1 = new ChatInfoResponse(
                "OK", "Chat", 10, "111", 1L,
                "desc", "small.jpg", "big.jpg", List.of("a"), "group"
        );
        ChatInfoResponse r2 = new ChatInfoResponse(
                "OK", "Chat", 10, "111", 1L,
                "desc", "small.jpg", "big.jpg", List.of("a"), "group"
        );
        ChatInfoResponse r3 = new ChatInfoResponse(
                "Fail", "Chat2", 20, "222", 2L,
                "other", "s2.jpg", "b2.jpg", List.of("b"), "channel"
        );

        assertEquals(r1, r2);
        assertEquals(r1.hashCode(), r2.hashCode());
        assertNotEquals(r1, r3);
    }
}
