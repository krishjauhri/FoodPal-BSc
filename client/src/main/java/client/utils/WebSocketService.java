package client.utils;

import org.springframework.messaging.converter.MappingJackson2MessageConverter;
import org.springframework.messaging.simp.stomp.StompFrameHandler;
import org.springframework.messaging.simp.stomp.StompHeaders;
import org.springframework.messaging.simp.stomp.StompSession;
import org.springframework.messaging.simp.stomp.StompSessionHandlerAdapter;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.messaging.WebSocketStompClient;

import java.lang.reflect.Type;
import java.util.concurrent.ExecutionException;
import java.util.function.Consumer;

public class WebSocketService {

    private StompSession session;

    public WebSocketService() {
        connect();
    }

    private void connect() {
        // 1. Create the Low-Level client (Tyrus)
        StandardWebSocketClient client = new StandardWebSocketClient();

        // 2. Wrap it in the STOMP client
        WebSocketStompClient stompClient = new WebSocketStompClient(client);

        // 3. Configure the JSON converter (so we receive Objects, not raw Strings)
        stompClient.setMessageConverter(new MappingJackson2MessageConverter());

        // 4. Connect to the URL we defined in the Server (Issue 1)
        String url = "ws://localhost:8080/websocket";
        try {
            this.session = stompClient.connect(url, new StompSessionHandlerAdapter() {}).get();
        } catch (InterruptedException | ExecutionException e) {
            System.err.println("Failed to connect to WebSocket: " + e.getMessage());
        }
    }

    // This method allows Controllers to say "I want to listen to topic X"
    public <T> StompSession.Subscription subscribe(String topic, Class<T> type, Consumer<T> callback) {
        if (session == null || !session.isConnected()) {
            System.err.println("Cannot subscribe, no active session.");
            return null;
        }

        return session.subscribe(topic, new StompFrameHandler() {
            @Override
            public Type getPayloadType(StompHeaders headers) {
                // Tells the converter what Java class to create (e.g., RecipeEvent.class)
                return type;
            }

            @Override
            public void handleFrame(StompHeaders headers, Object payload) {
                // When a message arrives, run the callback function provided by the Controller
                callback.accept(type.cast(payload));
            }
        });
    }

    /**
     * For testing purposes only.
     * Allows injecting a mock session to test subscription logic without a real server.
     */
    public void setSession(StompSession session) {
        this.session = session;
    }
}
