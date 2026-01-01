package com.gameclub.controller;

import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.util.HashMap;
import java.util.Map;

@Controller
public class GameWebSocketController {

    private final SimpMessagingTemplate messagingTemplate;

    public GameWebSocketController(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    @MessageMapping("/game/move")
    @SendTo("/topic/game/{roomCode}")
    public Map<String, Object> handleGameMove(Map<String, Object> message) {
        // 转发游戏移动消息到房间
        Map<String, Object> response = new HashMap<>();
        response.put("type", "move");
        response.put("data", message);
        return response;
    }

    @MessageMapping("/game/join")
    public void handleGameJoin(Map<String, Object> message) {
        String roomCode = (String) message.get("roomCode");
        // 通知房间内其他玩家有新玩家加入
        Map<String, Object> response = new HashMap<>();
        response.put("type", "player_joined");
        response.put("data", message);
        messagingTemplate.convertAndSend("/topic/room/" + roomCode, response);
    }

    // 发送消息到特定房间
    public void sendToRoom(String roomCode, Map<String, Object> message) {
        messagingTemplate.convertAndSend("/topic/room/" + roomCode, message);
    }
}

