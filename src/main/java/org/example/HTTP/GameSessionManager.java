package org.example.HTTP;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class GameSessionManager {
    private final ConcurrentHashMap<String, GameSession> sessions = new ConcurrentHashMap<>();

    public GameSession createGameSession(boolean humanGoesFirst) {
        String id = UUID.randomUUID().toString();
        GameSession session = new GameSession(id, humanGoesFirst);
        sessions.put(id, session);
        return session;
    }

    public GameSession getSession(String id) {
        return sessions.get(id);
    }

    public void removeSession(String id) {
        sessions.remove(id);
    }
}
