package com.example.demo.handler;

import lombok.extern.java.Log;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

@Component
@Slf4j
public class MyHandler extends TextWebSocketHandler {

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        log.info("ESTABLISHED");
    }

    @Override
    public void handleTextMessage(WebSocketSession session, TextMessage textMessage) {
        String message = textMessage.getPayload();
        System.out.println(message);
        System.out.println(textMessage);
    }
}
