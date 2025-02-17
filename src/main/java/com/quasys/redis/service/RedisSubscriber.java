package com.quasys.redis.service;

import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

@Service
public class RedisSubscriber implements MessageListener {

    private static final Set<WebSocketSession> sessions = new HashSet<>();

    public static void addSession(WebSocketSession session) {
        sessions.add(session);
    }

    public static void removeSession(WebSocketSession session) {
        sessions.remove(session);
    }
    @Override
    public void onMessage(Message message, byte[] pattern) {
        System.out.println("hola");
        System.out.println(message.toString());
        for (WebSocketSession session : sessions) {
            try {
                session.sendMessage(new TextMessage(message.toString()));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
