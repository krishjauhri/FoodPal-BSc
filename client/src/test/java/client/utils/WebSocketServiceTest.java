package client.utils;

import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import java.util.function.Consumer;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.messaging.simp.stomp.StompSession;

public class WebSocketServiceTest {

    private WebSocketService sut; // System Under Test

    @BeforeEach
    public void setup() {
        sut = new WebSocketService();
    }

    @Test
    public void subscribeWithoutConnectionReturnsNull() {
        // We haven't called connect(), so session is null
        var sub = sut.subscribe("/topic/test", String.class, str -> {});
        assertNull(sub, "Should return null if not connected");
    }

    @Test
    public void subscribeWithConnectionWorks() {
        // 1. Mock the StompSession
        StompSession mockSession = mock(StompSession.class);
        when(mockSession.isConnected()).thenReturn(true);

        // 2. Inject the mock session manually (Requires setSession method in WebSocketService)
        sut.setSession(mockSession);

        // 3. Call subscribe
        Consumer<String> callback = str -> {};
        sut.subscribe("/topic/test", String.class, callback);

        // 4. Verify the service actually passed the subscription to the session
        verify(mockSession).subscribe(eq("/topic/test"), any());
    }

    @Test
    public void disconnectClosesSession() {
        // 1. Mock the StompSession
        StompSession mockSession = mock(StompSession.class);
        when(mockSession.isConnected()).thenReturn(true);

        sut.setSession(mockSession);

        sut.disconnect();

        verify(mockSession).disconnect();
    }
}