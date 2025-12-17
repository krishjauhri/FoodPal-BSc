package server.api;

import static java.util.concurrent.TimeUnit.SECONDS;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.messaging.converter.MappingJackson2MessageConverter;
import org.springframework.messaging.simp.stomp.StompSession;
import org.springframework.messaging.simp.stomp.StompSessionHandlerAdapter;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.messaging.WebSocketStompClient;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class WebSocketTest {

    @LocalServerPort
    private Integer port;

    private WebSocketStompClient stompClient;

    @BeforeEach
    public void setup() {
        stompClient = new WebSocketStompClient(new StandardWebSocketClient());
        //message converter is for subscribes for JSON
        stompClient.setMessageConverter(new MappingJackson2MessageConverter());
    }

    //Test only works when main server isn't running
    //As main server locks h2 db
    @Test
    public void verifyConnection() throws Exception {
        String url = "ws://localhost:" + port + "/websocket";

        // Attempt to connect
        StompSession session = stompClient
                .connect(url, new StompSessionHandlerAdapter() {})
                .get(1, SECONDS);

        // Assert we have a connected session
        assertTrue(session.isConnected());
    }
}