package ch.supsi.sporthub.backend.utils;

import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class ArgumentParserTest {

    @Test
    void testParseArgs_SingleKeyValue() {
        String[] args = {"key=value"};
        Map<String, String> result = ArgumentParser.parseArgs(args);
        assertEquals(1, result.size());
        assertEquals("value", result.get("key"));
    }

    @Test
    void testParseArgs_MultipleKeyValuePairs() {
        String[] args = {"user=admin", "pass=1234", "debug=true"};
        Map<String, String> result = ArgumentParser.parseArgs(args);
        assertEquals(3, result.size());
        assertEquals("admin", result.get("user"));
        assertEquals("1234", result.get("pass"));
        assertEquals("true", result.get("debug"));
    }

    @Test
    void testParseArgs_KeyValueWithQuotes() {
        String[] args = {"message=\"Hello World\""};
        Map<String, String> result = ArgumentParser.parseArgs(args);
        assertEquals("Hello World", result.get("message"));
    }

    @Test
    void testParseArgs_ValueWithMultipleTokens() {
        String[] args = {"msg=Hello", "World", "From", "GPT"};
        Map<String, String> result = ArgumentParser.parseArgs(args);
        assertEquals("Hello World From GPT", result.get("msg"));
    }

    @Test
    void testParseArgs_TrimSpacesAndLowerCaseKey() {
        String[] args = {"  UserName = John  "};
        Map<String, String> result = ArgumentParser.parseArgs(args);
        assertTrue(result.containsKey("username"));
        assertEquals("John", result.get("username"));
    }

    @Test
    void testParseArgs_KeyWithEmptyValue() {
        String[] args = {"key="};
        Map<String, String> result = ArgumentParser.parseArgs(args);
        assertEquals("", result.get("key"));
    }

    @Test
    void testParseArgs_DuplicateKeys_LastOneWins() {
        String[] args = {"key=first", "key=second"};
        Map<String, String> result = ArgumentParser.parseArgs(args);
        assertEquals("second", result.get("key"));
    }

    @Test
    void testParseArgs_HandlesValueWithQuotesAndExtraParts() {
        String[] args = {"msg=\"Hello", "World\"", "end=done"};
        Map<String, String> result = ArgumentParser.parseArgs(args);
        assertEquals("Hello World", result.get("msg"));
        assertEquals("done", result.get("end"));
    }

    @Test
    void testParseArgs_EmptyArgsArray() {
        String[] args = {};
        Map<String, String> result = ArgumentParser.parseArgs(args);
        assertTrue(result.isEmpty());
    }

    @Test
    void testParseArgs_ValueWithOnlyQuotes() {
        String[] args = {"note=\"\""};
        Map<String, String> result = ArgumentParser.parseArgs(args);
        assertEquals("", result.get("note"));
    }

    @Test
    void testParseArgs_SkipsArgsWithoutEquals() {
        String[] args = {"debug", "user=admin", "verbose"};
        Map<String, String> result = ArgumentParser.parseArgs(args);
        assertEquals(1, result.size());
        assertTrue(result.containsKey("user"));
        assertEquals("admin verbose", result.get("user"));
    }

}
