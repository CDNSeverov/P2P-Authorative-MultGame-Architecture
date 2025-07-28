package org.example;

public enum MessageType {
    // For chat
    CHAT,
    PEER_DISCOVERY_REQUEST,
    PEER_DISCOVERY_RESPONSE,
    SHARE_INFO,

    // Game system
    PLAY_REQUEST,
    PLAY_RESPONSE,
    GAME_START,
    GAME_MOVE,
    GAME_STATE,
    GAME_END,
    VERIFICATION_REQUEST,
    VERIFICATION_RESPONSE
}