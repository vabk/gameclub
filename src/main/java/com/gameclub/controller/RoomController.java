package com.gameclub.controller;

import com.gameclub.entity.Room;
import com.gameclub.service.RoomService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/rooms")
@CrossOrigin(origins = "http://localhost:3000")
public class RoomController {

    @Autowired
    private RoomService roomService;

    @PostMapping("/create")
    public ResponseEntity<Map<String, Object>> createRoom(@RequestBody Map<String, String> request) {
        String gameType = request.get("gameType");
        String hostId = request.get("hostId");
        String hostName = request.get("hostName");

        if (gameType == null || hostId == null || hostName == null) {
            return ResponseEntity.badRequest().build();
        }

        try {
            Room room = roomService.createRoom(gameType, hostId, hostName);
            Map<String, Object> response = new HashMap<>();
            response.put("room", room);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    @PostMapping("/join")
    public ResponseEntity<Map<String, Object>> joinRoom(@RequestBody Map<String, String> request) {
        String roomCode = request.get("roomCode");
        String guestId = request.get("guestId");
        String guestName = request.get("guestName");

        if (roomCode == null || guestId == null || guestName == null) {
            return ResponseEntity.badRequest().build();
        }

        try {
            Room room = roomService.joinRoom(roomCode, guestId, guestName);
            Map<String, Object> response = new HashMap<>();
            response.put("room", room);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    @GetMapping("/{roomCode}")
    public ResponseEntity<Room> getRoom(@PathVariable String roomCode) {
        Room room = roomService.getRoom(roomCode);
        if (room == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(room);
    }

    @GetMapping("/waiting")
    public ResponseEntity<List<Room>> getWaitingRooms() {
        List<Room> rooms = roomService.getWaitingRooms();
        return ResponseEntity.ok(rooms);
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<Room>> getUserRooms(@PathVariable String userId) {
        List<Room> rooms = roomService.getUserRooms(userId);
        return ResponseEntity.ok(rooms);
    }

    @PostMapping("/leave")
    public ResponseEntity<Map<String, String>> leaveRoom(@RequestBody Map<String, String> request) {
        String roomCode = request.get("roomCode");
        String userId = request.get("userId");

        if (roomCode == null || userId == null) {
            return ResponseEntity.badRequest().build();
        }

        roomService.leaveRoom(roomCode, userId);
        Map<String, String> response = new HashMap<>();
        response.put("message", "成功离开房间");
        return ResponseEntity.ok(response);
    }
}

