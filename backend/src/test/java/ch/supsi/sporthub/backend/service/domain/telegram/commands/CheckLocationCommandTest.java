package ch.supsi.sporthub.backend.service.domain.telegram.commands;

import ch.supsi.sporthub.backend.model.ChatType;
import ch.supsi.sporthub.backend.model.User;
import ch.supsi.sporthub.backend.service.StravaAuthService;
import ch.supsi.sporthub.backend.service.api.*;
import ch.supsi.sporthub.backend.service.domain.strava.*;
import ch.supsi.sporthub.backend.service.domain.telegram.ChatTypeFactory;
import ch.supsi.sporthub.backend.service.domain.telegram.TelegramCommandInput;
import ch.supsi.sporthub.backend.utils.PolylineUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import java.util.*;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

public class CheckLocationCommandTest {

    @Mock
    private StravaAuthService stravaAuthService;
    @Mock
    private IStravaService stravaService;
    @Mock
    private IGeocodingService geocodingService;
    @Mock
    private IMapCacheService mapCacheService;
    @Mock
    private IUserChatService userChatService;
    @Mock
    private ChatTypeFactory chatTypeFactory;

    @InjectMocks
    private CheckLocationCommand checkLocationCommand;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testExecuteCommandInGroupChat() {
        String chatId = "-4602071182";
        TelegramCommandInput input = mock(TelegramCommandInput.class);
        when(input.getChatId()).thenReturn(chatId);
        when(input.getArgs()).thenReturn(new String[]{"lat=45.0", "lon=10.0"});

        when(chatTypeFactory.isChatType(chatId, ChatType.GROUP)).thenReturn(true);
        User user = new User();
        user.setUsername("testuser");
        when(userChatService.getUsersInChat(chatId)).thenReturn(Collections.singletonList(user));

        Map<String, String> params = new HashMap<>();
        params.put("lat", "45.0");
        params.put("lon", "10.0");

        when(geocodingService.geocode("test location")).thenReturn(new double[]{45.0, 10.0});
        when(geocodingService.reverseGeocode(45.0, 10.0)).thenReturn("Test Location");

        StravaTokenResponse tokenResponse = new StravaTokenResponse("access_token", "bearer", "refresh_token", 3600, -1, null);
        when(stravaAuthService.getOrRefreshAccessToken("testuser")).thenReturn(Optional.of(tokenResponse));

        StravaActivity activity = new StravaActivity();
        activity.setStartLatlng(Arrays.asList(45.1, 10.1));
        activity.setEndLatlng(Arrays.asList(45.2, 10.2));
        activity.setName("Test Activity");
        activity.setSportType("Run");
        activity.setDistanceMeters(1000);
        activity.setCalories(100);

        when(stravaService.getActivities("access_token")).thenReturn(Collections.singletonList(activity));
        String result = checkLocationCommand.execute(input);
        assertTrue(result.contains("Target Location"));
        assertTrue(result.contains("Test Activity"));
        assertTrue(result.contains("View all activities on the interactive map"));
    }

    @Test
    void testInvalidCoordinates() {
        String chatId = "-4602071182";
        TelegramCommandInput input = mock(TelegramCommandInput.class);
        when(input.getChatId()).thenReturn(chatId);
        when(input.getArgs()).thenReturn(new String[]{"lat=invalid", "lon=10.0"});

        when(chatTypeFactory.isChatType(chatId, ChatType.GROUP)).thenReturn(true);
        User user = new User();
        user.setFirstName("testuser");
        when(userChatService.getUsersInChat(chatId)).thenReturn(Collections.singletonList(user));
        String result = checkLocationCommand.execute(input);
        assertEquals("❌ Invalid coordinates. Please provide valid numbers for 'lat' and 'lon'.", result);
    }

    @Test
    void testNoActivitiesFound() {
        String chatId = "-4602071182";
        TelegramCommandInput input = mock(TelegramCommandInput.class);
        when(input.getChatId()).thenReturn(chatId);
        when(input.getArgs()).thenReturn(new String[]{"lat=45.0", "lon=10.0"});

        when(chatTypeFactory.isChatType(chatId, ChatType.GROUP)).thenReturn(true);
        User user = new User();
        user.setFirstName("testuser");
        when(userChatService.getUsersInChat(chatId)).thenReturn(Collections.singletonList(user));

        Map<String, String> params = new HashMap<>();
        params.put("lat", "45.0");
        params.put("lon", "10.0");

        when(geocodingService.geocode("test location")).thenReturn(new double[]{45.0, 10.0});
        when(geocodingService.reverseGeocode(45.0, 10.0)).thenReturn("Test Location");

        StravaTokenResponse tokenResponse = new StravaTokenResponse("access_token", "bearer", "refresh_token", 3600, -1, null);
        when(stravaAuthService.getOrRefreshAccessToken("testuser")).thenReturn(Optional.of(tokenResponse));

        when(stravaService.getActivities("access_token")).thenReturn(Collections.emptyList());
        String result = checkLocationCommand.execute(input);

        assertTrue(result.contains("❌ No activity found near this location"));
    }

    @Test
    void testInvalidLocationName() {
        String chatId = "-4602071182";
        TelegramCommandInput input = mock(TelegramCommandInput.class);
        when(input.getChatId()).thenReturn(chatId);
        when(input.getArgs()).thenReturn(new String[]{"location=\"Invalid Location\""});

        when(chatTypeFactory.isChatType(chatId, ChatType.GROUP)).thenReturn(true);
        User user = new User();
        user.setFirstName("testuser");
        when(userChatService.getUsersInChat(chatId)).thenReturn(Collections.singletonList(user));

        when(geocodingService.geocode("Invalid Location")).thenReturn(null);
        String result = checkLocationCommand.execute(input);

        assertEquals("❌ Unable to geocode the location provided.", result);
    }

    @Test
    void testGetCommandName() {
        String commandName = checkLocationCommand.getCommandName();
        assertEquals("/check_location", commandName);
    }
    @Test
    void testIsAllowedInChat_groupChat() {
        String chatId = "-4602071182";
        when(chatTypeFactory.isChatType(chatId, ChatType.GROUP)).thenReturn(true);
        boolean isAllowed = checkLocationCommand.isAllowedInChat(chatId);
        assertTrue(isAllowed);
    }
    @Test
    void testIsAllowedInChat_nonGroupChat() {
        String chatId = "6611010833";
        when(chatTypeFactory.isChatType(chatId, ChatType.GROUP)).thenReturn(false);
        boolean isAllowed = checkLocationCommand.isAllowedInChat(chatId);
        assertFalse(isAllowed);
    }



    @Test
    void testLatWithoutLon() {
        String[] args = {"lat=45.0"};
        String chatId = "-4602071182";
        TelegramCommandInput input = mock(TelegramCommandInput.class);
        when(input.getChatId()).thenReturn(chatId);
        when(input.getArgs()).thenReturn(args);
        when(chatTypeFactory.isChatType(chatId, ChatType.GROUP)).thenReturn(true);

        String result = checkLocationCommand.execute(input);
        assertEquals("❌ Please provide both 'lat' and 'lon'.", result);
    }

    @Test
    void testLonWithoutLat() {
        String[] args = {"lon=10.0"};
        String chatId = "-4602071182";
        TelegramCommandInput input = mock(TelegramCommandInput.class);
        when(input.getChatId()).thenReturn(chatId);
        when(input.getArgs()).thenReturn(args);
        when(chatTypeFactory.isChatType(chatId, ChatType.GROUP)).thenReturn(true);
        String result = checkLocationCommand.execute(input);
        assertEquals("❌ Please provide both 'lat' and 'lon'.", result);
    }

    @Test
    void testLatAndLonValid() {
        String[] args = {"lat=45.0", "lon=10.0"};
        String chatId = "-4602071182";
        TelegramCommandInput input = mock(TelegramCommandInput.class);
        when(input.getChatId()).thenReturn(chatId);
        when(input.getArgs()).thenReturn(args);
        when(chatTypeFactory.isChatType(chatId, ChatType.GROUP)).thenReturn(true);
        User user = new User();
        user.setUsername("testuser");
        when(userChatService.getUsersInChat(chatId)).thenReturn(Collections.singletonList(user));
        when(geocodingService.geocode("test location")).thenReturn(new double[]{45.0, 10.0});
        StravaTokenResponse tokenResponse = new StravaTokenResponse("access_token", "bearer", "refresh_token", 3600, -1, null);
        when(stravaAuthService.getOrRefreshAccessToken("testuser")).thenReturn(Optional.of(tokenResponse));

        StravaActivity activity = new StravaActivity();
        activity.setStartLatlng(Arrays.asList(45.1, 10.1));
        activity.setEndLatlng(Arrays.asList(45.2, 10.2));
        activity.setName("Test Activity");
        activity.setSportType("Run");
        activity.setDistanceMeters(1000);
        activity.setCalories(100);
        when(stravaService.getActivities("access_token")).thenReturn(Collections.singletonList(activity));
        String result = checkLocationCommand.execute(input);
        assertTrue(result.contains("Target Location"));
        assertTrue(result.contains("Test Activity"));
        assertTrue(result.contains("View all activities on the interactive map"));
    }


    @Test
    void testLatAndLonInvalid() {
        String[] args = {"lat=invalid", "lon=10.0"};
        String chatId = "-4602071182";
        TelegramCommandInput input = mock(TelegramCommandInput.class);
        when(input.getChatId()).thenReturn(chatId);
        when(input.getArgs()).thenReturn(args);
        when(chatTypeFactory.isChatType(chatId, ChatType.GROUP)).thenReturn(true);
        String result = checkLocationCommand.execute(input);
        assertEquals("❌ Invalid coordinates. Please provide valid numbers for 'lat' and 'lon'.", result);
    }

    @Test
    void testLocationNotFound() {
        String[] args = {"location=Nonexistent Location"};
        String chatId = "-4602071182";
        TelegramCommandInput input = mock(TelegramCommandInput.class);
        when(input.getChatId()).thenReturn(chatId);
        when(input.getArgs()).thenReturn(args);
        when(chatTypeFactory.isChatType(chatId, ChatType.GROUP)).thenReturn(true);
        String result = checkLocationCommand.execute(createTelegramCommandInput(args));
        assertEquals("❌ Unable to geocode the location provided.", result);
    }

    @Test
    void testNoLatNoLonNoLocation() {
        String[] args = {};
        String chatId = "-4602071182";
        TelegramCommandInput input = mock(TelegramCommandInput.class);
        when(input.getChatId()).thenReturn(chatId);
        when(input.getArgs()).thenReturn(args);
        when(chatTypeFactory.isChatType(chatId, ChatType.GROUP)).thenReturn(true);
        String result = checkLocationCommand.execute(createTelegramCommandInput(args));
        assertEquals("❌ <b>Usage:</b> <code>/check_location lat=&lt;latitude&gt; lon=&lt;longitude&gt; [radius=&lt;km&gt;]</code> OR <code>/check_location location='Location Name' [radius=&lt;km&gt;]</code>", result);
    }

    @Test
    void testLocationWithValidCoords() {
        String[] args = {"location=Some Location"};
        String chatId = "-4602071182";

        TelegramCommandInput input = mock(TelegramCommandInput.class);
        when(input.getChatId()).thenReturn(chatId);
        when(input.getArgs()).thenReturn(args);
        when(chatTypeFactory.isChatType(chatId, ChatType.GROUP)).thenReturn(true);

        User user = new User();
        user.setUsername("testuser");
        when(userChatService.getUsersInChat(chatId)).thenReturn(Collections.singletonList(user));
        when(geocodingService.geocode("Some Location")).thenReturn(new double[]{45.0, 10.0});

        StravaTokenResponse tokenResponse = new StravaTokenResponse("access_token", "bearer", "refresh_token", 3600, -1, null);
        when(stravaAuthService.getOrRefreshAccessToken("testuser")).thenReturn(Optional.of(tokenResponse));

        StravaActivity activity = new StravaActivity();
        activity.setStartLatlng(Arrays.asList(45.1, 10.1));
        activity.setEndLatlng(Arrays.asList(45.2, 10.2));
        activity.setName("Test Activity");
        activity.setSportType("Run");
        activity.setDistanceMeters(1000);
        activity.setCalories(100);
        when(stravaService.getActivities("access_token")).thenReturn(Collections.singletonList(activity));
        String result = checkLocationCommand.execute(input);

        assertTrue(result.contains("Target Location"));
        assertTrue(result.contains("Test Activity"));
        assertTrue(result.contains("View all activities on the interactive map"));
        assertTrue(result.contains("45.00000"));
        assertTrue(result.contains("10.00000"));
    }


    @Test
    void testRadiusValid() {
        String[] args = {"lat=45.0", "lon=10.0", "radius=5.0"};
        String chatId = "-4602071182";
        TelegramCommandInput input = mock(TelegramCommandInput.class);
        when(input.getChatId()).thenReturn(chatId);
        when(input.getArgs()).thenReturn(args);
        when(chatTypeFactory.isChatType(chatId, ChatType.GROUP)).thenReturn(true);

        User user = new User();
        user.setUsername("testuser");
        when(userChatService.getUsersInChat(chatId)).thenReturn(Collections.singletonList(user));

        String result = checkLocationCommand.execute(input);

        assertTrue(result.contains("Target Location"));
        assertTrue(result.contains("Radius: 5.00 km"));
    }

    @Test
    void testRadiusInvalid() {
        String[] args = {"lat=45.0", "lon=10.0", "radius=invalid"};
        String chatId = "-4602071182";
        TelegramCommandInput input = mock(TelegramCommandInput.class);
        when(input.getChatId()).thenReturn(chatId);
        when(input.getArgs()).thenReturn(args);
        when(chatTypeFactory.isChatType(chatId, ChatType.GROUP)).thenReturn(true);

        User user = new User();
        user.setUsername("testuser");
        when(userChatService.getUsersInChat(chatId)).thenReturn(Collections.singletonList(user));

        String result = checkLocationCommand.execute(input);

        assertEquals("❌ Invalid radius. Please provide a valid number.", result);
    }

    @Test
    void testRadiusNotProvided() {
        String[] args = {"lat=45.0", "lon=10.0"};
        String chatId = "-4602071182";
        TelegramCommandInput input = mock(TelegramCommandInput.class);
        when(input.getChatId()).thenReturn(chatId);
        when(input.getArgs()).thenReturn(args);
        when(chatTypeFactory.isChatType(chatId, ChatType.GROUP)).thenReturn(true);

        User user = new User();
        user.setUsername("testuser");
        when(userChatService.getUsersInChat(chatId)).thenReturn(Collections.singletonList(user));

        String result = checkLocationCommand.execute(input);

        assertTrue(result.contains("Radius: 2.00 km"));
    }

    @Test
    void testValidPolylineProcessing() {
        Map<String, Object> map = new HashMap<>();
        String polylineStr = "oikcFf~vxC~zO`kSv}pH~grJ|xsMz`rAzdiFqljGhgbAdnvMr}vChxdTw|pSzjfBp`iAdpa";
        map.put("summary_polyline", polylineStr);
        ActivitySegment segment = mock(ActivitySegment.class);
        if (map.get("summary_polyline") instanceof String polyline) {
            List<List<Double>> decoded = PolylineUtils.decodePolyline(polyline);
            for (List<Double> point : decoded) {
                if (point.size() >= 2) {
                    segment.addPoint(point.get(0), point.get(1));
                }
            }
        }
        verify(segment, atLeastOnce()).addPoint(anyDouble(), anyDouble());
    }




    @Test
    void testInvalidPolyline() {
        Map<String, Object> map = new HashMap<>();
        map.put("summary_polyline", 12345);
        ActivitySegment segment = mock(ActivitySegment.class);
        if (map.get("summary_polyline") instanceof String polyline) {
            List<List<Double>> decoded = PolylineUtils.decodePolyline(polyline);
            for (List<Double> point : decoded) {
                if (point.size() >= 2) {
                    segment.addPoint(point.get(0), point.get(1));
                }
            }
        }
        verify(segment, never()).addPoint(anyDouble(), anyDouble());
    }

    @Test
    void testMapNull() {
        ActivitySegment segment = mock(ActivitySegment.class);
        verify(segment, never()).addPoint(anyDouble(), anyDouble());
    }

    @Test
    void testNoValidStravaToken() {
        String chatId = "-4602071182";
        TelegramCommandInput input = mock(TelegramCommandInput.class);
        when(input.getChatId()).thenReturn(chatId);
        when(input.getArgs()).thenReturn(new String[]{"lat=45.0", "lon=10.0"});

        when(chatTypeFactory.isChatType(chatId, ChatType.GROUP)).thenReturn(true);
        User user = new User();
        user.setUsername("testuser");
        when(userChatService.getUsersInChat(chatId)).thenReturn(Collections.singletonList(user));
        when(geocodingService.reverseGeocode(45.0, 10.0)).thenReturn("Test Location");
        when(stravaAuthService.getOrRefreshAccessToken("testuser")).thenReturn(Optional.empty());

        String result = checkLocationCommand.execute(input);
        assertTrue(result.contains("Target Location"));
        assertTrue(result.contains("No activity found near this location"));
    }

    @Test
    void testNoUsersFoundInChat() {
        String chatId = "-4602071182";
        TelegramCommandInput input = mock(TelegramCommandInput.class);
        when(input.getChatId()).thenReturn(chatId);
        when(input.getArgs()).thenReturn(new String[]{"lat=45.0", "lon=10.0"});

        when(chatTypeFactory.isChatType(chatId, ChatType.GROUP)).thenReturn(true);
        when(userChatService.getUsersInChat(chatId)).thenReturn(Collections.emptyList());

        String result = checkLocationCommand.execute(input);
        assertEquals("❌ No users found in this chat.", result);
    }

    @Test
    void testStravaServiceThrowsException() {
        String chatId = "-4602071182";
        TelegramCommandInput input = mock(TelegramCommandInput.class);
        when(input.getChatId()).thenReturn(chatId);
        when(input.getArgs()).thenReturn(new String[]{"lat=45.0", "lon=10.0"});

        when(chatTypeFactory.isChatType(chatId, ChatType.GROUP)).thenReturn(true);
        User user = new User();
        user.setUsername("testuser");
        when(userChatService.getUsersInChat(chatId)).thenReturn(Collections.singletonList(user));
        when(geocodingService.reverseGeocode(45.0, 10.0)).thenReturn("Test Location");

        StravaTokenResponse tokenResponse = new StravaTokenResponse("access_token", "bearer", "refresh_token", 3600, -1, null);
        when(stravaAuthService.getOrRefreshAccessToken("testuser")).thenReturn(Optional.of(tokenResponse));
        when(stravaService.getActivities("access_token")).thenThrow(new RuntimeException("API error"));

        String result = checkLocationCommand.execute(input);
        assertTrue(result.contains("Target Location"));
        assertTrue(result.contains("No activity found near this location"));
    }

    @Test
    void testActivityWithNullCoordinates() {
        String chatId = "-4602071182";
        TelegramCommandInput input = mock(TelegramCommandInput.class);
        when(input.getChatId()).thenReturn(chatId);
        when(input.getArgs()).thenReturn(new String[]{"lat=45.0", "lon=10.0"});

        when(chatTypeFactory.isChatType(chatId, ChatType.GROUP)).thenReturn(true);
        User user = new User();
        user.setUsername("testuser");
        when(userChatService.getUsersInChat(chatId)).thenReturn(Collections.singletonList(user));
        when(geocodingService.reverseGeocode(45.0, 10.0)).thenReturn("Test Location");

        StravaTokenResponse tokenResponse = new StravaTokenResponse("access_token", "bearer", "refresh_token", 3600, -1, null);
        when(stravaAuthService.getOrRefreshAccessToken("testuser")).thenReturn(Optional.of(tokenResponse));
        StravaActivity activity1 = new StravaActivity();
        activity1.setStartLatlng(null);
        activity1.setEndLatlng(Arrays.asList(45.2, 10.2));
        StravaActivity activity2 = new StravaActivity();
        activity2.setStartLatlng(Arrays.asList(45.1, 10.1));
        activity2.setEndLatlng(null);
        StravaActivity activity3 = new StravaActivity();
        activity3.setStartLatlng(Collections.singletonList(45.1));
        activity3.setEndLatlng(Arrays.asList(45.2, 10.2));

        when(stravaService.getActivities("access_token")).thenReturn(
                Arrays.asList(activity1, activity2, activity3)
        );

        String result = checkLocationCommand.execute(input);
        assertTrue(result.contains("Target Location"));
        assertTrue(result.contains("No activity found near this location"));
    }

    @Test
    void testActivityWithMatchButNoPolyline() {
        String chatId = "-4602071182";
        TelegramCommandInput input = mock(TelegramCommandInput.class);
        when(input.getChatId()).thenReturn(chatId);
        when(input.getArgs()).thenReturn(new String[]{"lat=45.0", "lon=10.0"});

        when(chatTypeFactory.isChatType(chatId, ChatType.GROUP)).thenReturn(true);
        User user = new User();
        user.setUsername("testuser");
        when(userChatService.getUsersInChat(chatId)).thenReturn(Collections.singletonList(user));
        when(geocodingService.reverseGeocode(45.0, 10.0)).thenReturn("Test Location");
        when(geocodingService.distancePointToSegment(eq(45.0), eq(10.0), anyDouble(), anyDouble(), anyDouble(), anyDouble()))
                .thenReturn(1.5);

        StravaTokenResponse tokenResponse = new StravaTokenResponse("access_token", "bearer", "refresh_token", 3600, -1, null);
        when(stravaAuthService.getOrRefreshAccessToken("testuser")).thenReturn(Optional.of(tokenResponse));
        StravaActivity activity = new StravaActivity();
        activity.setStartLatlng(Arrays.asList(45.1, 10.1));
        activity.setEndLatlng(Arrays.asList(45.2, 10.2));
        activity.setName("Test Activity");
        activity.setSportType("Run");
        activity.setDistanceMeters(1000);
        activity.setCalories(100);
        activity.setMap(null);

        when(stravaService.getActivities("access_token")).thenReturn(Collections.singletonList(activity));
        when(mapCacheService.saveMapData(any())).thenReturn("test-map-token");

        String result = checkLocationCommand.execute(input);
        assertTrue(result.contains("Test Activity"));
        assertTrue(result.contains("View all activities on the interactive map"));
    }

    @Test
    void testActivityWithMapButNoPolyline() {
        String chatId = "-4602071182";
        TelegramCommandInput input = mock(TelegramCommandInput.class);
        when(input.getChatId()).thenReturn(chatId);
        when(input.getArgs()).thenReturn(new String[]{"lat=45.0", "lon=10.0"});

        when(chatTypeFactory.isChatType(chatId, ChatType.GROUP)).thenReturn(true);
        User user = new User();
        user.setUsername("testuser");
        when(userChatService.getUsersInChat(chatId)).thenReturn(Collections.singletonList(user));
        when(geocodingService.reverseGeocode(45.0, 10.0)).thenReturn("Test Location");
        when(geocodingService.distancePointToSegment(eq(45.0), eq(10.0), anyDouble(), anyDouble(), anyDouble(), anyDouble()))
                .thenReturn(1.5);

        StravaTokenResponse tokenResponse = new StravaTokenResponse("access_token", "bearer", "refresh_token", 3600, -1, null);
        when(stravaAuthService.getOrRefreshAccessToken("testuser")).thenReturn(Optional.of(tokenResponse));
        StravaActivity activity = new StravaActivity();
        activity.setStartLatlng(Arrays.asList(45.1, 10.1));
        activity.setEndLatlng(Arrays.asList(45.2, 10.2));
        activity.setName("Test Activity");
        activity.setSportType("Run");
        activity.setDistanceMeters(1000);
        activity.setCalories(100);

        Map<String, Object> map = new HashMap<>();
        map.put("other_data", "some value");
        activity.setMap(map);

        when(stravaService.getActivities("access_token")).thenReturn(Collections.singletonList(activity));
        when(mapCacheService.saveMapData(any())).thenReturn("test-map-token");

        String result = checkLocationCommand.execute(input);
        assertTrue(result.contains("Test Activity"));
        assertTrue(result.contains("View all activities on the interactive map"));
    }

    @Test
    void testActivityWithCompletePolyline() {
        String chatId = "-4602071182";
        TelegramCommandInput input = mock(TelegramCommandInput.class);
        when(input.getChatId()).thenReturn(chatId);
        when(input.getArgs()).thenReturn(new String[]{"lat=45.0", "lon=10.0"});

        when(chatTypeFactory.isChatType(chatId, ChatType.GROUP)).thenReturn(true);
        User user = new User();
        user.setUsername("testuser");
        when(userChatService.getUsersInChat(chatId)).thenReturn(Collections.singletonList(user));
        when(geocodingService.reverseGeocode(45.0, 10.0)).thenReturn("Test Location");
        when(geocodingService.distancePointToSegment(eq(45.0), eq(10.0), anyDouble(), anyDouble(), anyDouble(), anyDouble()))
                .thenReturn(1.5);

        StravaTokenResponse tokenResponse = new StravaTokenResponse("access_token", "bearer", "refresh_token", 3600, -1, null);
        when(stravaAuthService.getOrRefreshAccessToken("testuser")).thenReturn(Optional.of(tokenResponse));
        StravaActivity activity = new StravaActivity();
        activity.setStartLatlng(Arrays.asList(45.1, 10.1));
        activity.setEndLatlng(Arrays.asList(45.2, 10.2));
        activity.setName("Test Activity");
        activity.setSportType("Run");
        activity.setDistanceMeters(1000);
        activity.setCalories(100);

        Map<String, Object> map = new HashMap<>();
        map.put("summary_polyline", "oikcFf~vxC~zO`kS");
        activity.setMap(map);

        when(stravaService.getActivities("access_token")).thenReturn(Collections.singletonList(activity));
        when(mapCacheService.saveMapData(any())).thenReturn("test-map-token");

        String result = checkLocationCommand.execute(input);
        assertTrue(result.contains("Test Activity"));
        assertTrue(result.contains("View all activities on the interactive map"));
    }

    @Test
    void testMultipleUsers() {
        String chatId = "-4602071182";
        TelegramCommandInput input = mock(TelegramCommandInput.class);
        when(input.getChatId()).thenReturn(chatId);
        when(input.getArgs()).thenReturn(new String[]{"lat=45.0", "lon=10.0"});

        when(chatTypeFactory.isChatType(chatId, ChatType.GROUP)).thenReturn(true);
        User user1 = new User();
        user1.setUsername("user1");

        User user2 = new User();
        user2.setUsername("user2");

        when(userChatService.getUsersInChat(chatId)).thenReturn(Arrays.asList(user1, user2));
        when(geocodingService.reverseGeocode(45.0, 10.0)).thenReturn("Test Location");
        when(geocodingService.distancePointToSegment(eq(45.0), eq(10.0), anyDouble(), anyDouble(), anyDouble(), anyDouble()))
                .thenReturn(1.5);

        StravaTokenResponse tokenResponse1 = new StravaTokenResponse("access_token1", "bearer", "refresh_token", 3600, -1, null);
        when(stravaAuthService.getOrRefreshAccessToken("user1")).thenReturn(Optional.of(tokenResponse1));

        StravaTokenResponse tokenResponse2 = new StravaTokenResponse("access_token2", "bearer", "refresh_token", 3600, -1, null);
        when(stravaAuthService.getOrRefreshAccessToken("user2")).thenReturn(Optional.of(tokenResponse2));

        StravaActivity activity1 = new StravaActivity();
        activity1.setStartLatlng(Arrays.asList(45.1, 10.1));
        activity1.setEndLatlng(Arrays.asList(45.2, 10.2));
        activity1.setName("User1 Activity");
        activity1.setSportType("Run");
        activity1.setDistanceMeters(1000);
        activity1.setCalories(100);

        StravaActivity activity2 = new StravaActivity();
        activity2.setStartLatlng(Arrays.asList(45.15, 10.15));
        activity2.setEndLatlng(Arrays.asList(45.25, 10.25));
        activity2.setName("User2 Activity");
        activity2.setSportType("Cycling");
        activity2.setDistanceMeters(2000);
        activity2.setCalories(200);

        when(stravaService.getActivities("access_token1")).thenReturn(Collections.singletonList(activity1));
        when(stravaService.getActivities("access_token2")).thenReturn(Collections.singletonList(activity2));
        when(mapCacheService.saveMapData(any())).thenReturn("test-map-token");

        String result = checkLocationCommand.execute(input);
        assertTrue(result.contains("User1 Activity"));
        assertTrue(result.contains("User2 Activity"));
        assertTrue(result.contains("View all activities on the interactive map"));
    }

    @Test
    void testActivityOutsideRadius() {
        String chatId = "-4602071182";
        TelegramCommandInput input = mock(TelegramCommandInput.class);
        when(input.getChatId()).thenReturn(chatId);
        when(input.getArgs()).thenReturn(new String[]{"lat=45.0", "lon=10.0", "radius=1.0"});

        when(chatTypeFactory.isChatType(chatId, ChatType.GROUP)).thenReturn(true);
        User user = new User();
        user.setUsername("testuser");
        when(userChatService.getUsersInChat(chatId)).thenReturn(Collections.singletonList(user));
        when(geocodingService.reverseGeocode(45.0, 10.0)).thenReturn("Test Location");
        when(geocodingService.distancePointToSegment(eq(45.0), eq(10.0), anyDouble(), anyDouble(), anyDouble(), anyDouble()))
                .thenReturn(1.5);

        StravaTokenResponse tokenResponse = new StravaTokenResponse("access_token", "bearer", "refresh_token", 3600, -1, null);
        when(stravaAuthService.getOrRefreshAccessToken("testuser")).thenReturn(Optional.of(tokenResponse));

        StravaActivity activity = new StravaActivity();
        activity.setStartLatlng(Arrays.asList(45.1, 10.1));
        activity.setEndLatlng(Arrays.asList(45.2, 10.2));
        activity.setName("Test Activity");
        activity.setSportType("Run");
        activity.setDistanceMeters(1000);
        activity.setCalories(100);

        when(stravaService.getActivities("access_token")).thenReturn(Collections.singletonList(activity));

        String result = checkLocationCommand.execute(input);
        assertTrue(result.contains("Target Location"));
        assertTrue(result.contains("Radius: 1.00 km"));
        assertTrue(result.contains("No activity found near this location"));
    }

    @Test
    void testActivitiesWithMissingOrInvalidCoordinates() {
        String chatId = "-4602071182";
        TelegramCommandInput input = mock(TelegramCommandInput.class);
        when(input.getChatId()).thenReturn(chatId);
        when(input.getArgs()).thenReturn(new String[]{"lat=45.0", "lon=10.0"});

        when(chatTypeFactory.isChatType(chatId, ChatType.GROUP)).thenReturn(true);
        User user = new User();
        user.setUsername("testuser");
        when(userChatService.getUsersInChat(chatId)).thenReturn(Collections.singletonList(user));
        when(geocodingService.reverseGeocode(45.0, 10.0)).thenReturn("Test Location");

        StravaTokenResponse tokenResponse = new StravaTokenResponse("access_token", "bearer", "refresh_token", 3600, -1, null);
        when(stravaAuthService.getOrRefreshAccessToken("testuser")).thenReturn(Optional.of(tokenResponse));

        StravaActivity validActivity = new StravaActivity();
        validActivity.setStartLatlng(Arrays.asList(45.1, 10.1));
        validActivity.setEndLatlng(Arrays.asList(45.2, 10.2));
        validActivity.setName("Valid Activity");
        validActivity.setSportType("Run");
        validActivity.setDistanceMeters(1000);
        validActivity.setCalories(100);

        StravaActivity nullStartActivity = new StravaActivity();
        nullStartActivity.setStartLatlng(null);
        nullStartActivity.setEndLatlng(Arrays.asList(45.2, 10.2));
        nullStartActivity.setName("Null Start Activity");

        StravaActivity nullEndActivity = new StravaActivity();
        nullEndActivity.setStartLatlng(Arrays.asList(45.1, 10.1));
        nullEndActivity.setEndLatlng(null);
        nullEndActivity.setName("Null End Activity");

        StravaActivity shortStartActivity = new StravaActivity();
        shortStartActivity.setStartLatlng(Collections.singletonList(45.1));
        shortStartActivity.setEndLatlng(Arrays.asList(45.2, 10.2));
        shortStartActivity.setName("Short Start Activity");

        StravaActivity shortEndActivity = new StravaActivity();
        shortEndActivity.setStartLatlng(Arrays.asList(45.1, 10.1));
        shortEndActivity.setEndLatlng(Collections.singletonList(45.2));
        shortEndActivity.setName("Short End Activity");

        when(stravaService.getActivities("access_token")).thenReturn(
                Arrays.asList(validActivity, nullStartActivity, nullEndActivity, shortStartActivity, shortEndActivity)
        );

        when(geocodingService.distancePointToSegment(eq(45.0), eq(10.0), eq(45.1), eq(10.1), eq(45.2), eq(10.2)))
                .thenReturn(1.5);

        String result = checkLocationCommand.execute(input);
        assertTrue(result.contains("Valid Activity"));
        assertFalse(result.contains("Null Start Activity"));
        assertFalse(result.contains("Null End Activity"));
        assertFalse(result.contains("Short Start Activity"));
        assertFalse(result.contains("Short End Activity"));
    }



    @Test
    void testPolylinePointSizeCheck() {
        List<List<Double>> points = new ArrayList<>();
        points.add(Arrays.asList(45.1, 10.1));
        points.add(Collections.singletonList(45.2));
        points.add(Collections.emptyList());
        ActivitySegment segment = spy(new ActivitySegment(0, 0, 0, 0, ""));
        for (List<Double> point : points) {
            if (point.size() >= 2) {
                segment.addPoint(point.get(0), point.get(1));
            }
        }
        verify(segment, times(1)).addPoint(anyDouble(), anyDouble());
    }

    private TelegramCommandInput createTelegramCommandInput(String[] args) {
        TelegramCommandInput input = mock(TelegramCommandInput.class);
        when(input.getArgs()).thenReturn(args);
        return input;
    }


}
