package client.utils;

import org.springframework.messaging.converter.MappingJackson2MessageConverter;
import org.springframework.messaging.simp.stomp.*;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.messaging.WebSocketStompClient;

import java.lang.reflect.Type;
import java.util.concurrent.ExecutionException;
import java.util.function.Consumer;

public class WebSocketService {

    private StompSession session;

    public WebSocketService() {
    }

    //similar connect to the WebSocketTest in server
    public void connect(String url) {
        StandardWebSocketClient client = new StandardWebSocketClient();
        WebSocketStompClient stompClient = new WebSocketStompClient(client);
        stompClient.setMessageConverter(new MappingJackson2MessageConverter());

        try {
            this.session = stompClient.connect(url, new StompSessionHandlerAdapter() {}).get();
        } catch (InterruptedException | ExecutionException e) {
            System.err.println("Failed to connect to WebSocket: " + e.getMessage());
        }
    }

    //Disconnect method
    public void disconnect() {
        if (session != null && session.isConnected()) {
            session.disconnect();
            System.out.println("WebSocket Disconnected.");
        }
    }

    public boolean isConnected() {
        return session != null && session.isConnected();
    }

    /**This method is for subscribing clients to a topic
     * @param topic for where client subscribe to, eg: /topic/recipes
     * @param type to convert JSON to this class
     * @param callback for parsed events
     *
     * @return subsciption
     *
    */
    public <T> StompSession.Subscription subscribe(String topic, Class<T> type, Consumer<T> callback) {
        if (!isConnected()) {
            System.err.println("Cannot subscribe, no active session.");
            return null;
        }
        return session.subscribe(topic, new StompFrameHandler() {
            @Override
            public Type getPayloadType(StompHeaders headers) {
                return type;
            }
            @Override
            public void handleFrame(StompHeaders headers, Object payload) {
                callback.accept(type.cast(payload));
            }
        });
    }

    // For testing only
    public void setSession(StompSession session) {
        this.session = session;
    }
}