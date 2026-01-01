package com.gameclub.service;

import com.gameclub.entity.Room;
import com.gameclub.mapper.RoomMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Random;
import java.util.UUID;

@Service
public class RoomService {
    private static final Logger logger = LoggerFactory.getLogger(RoomService.class);
    private final Random random = new Random();

    @Autowired
    private RoomMapper roomMapper;

    @Transactional
    public Room createRoom(String gameType, String hostId, String hostName) {
        Room room = new Room();
        room.setRoomCode(generateRoomCode());
        room.setGameType(gameType);
        room.setHostId(hostId);
        room.setHostName(hostName);
        room.setStatus("waiting");
        room.setCreatedAt(LocalDateTime.now());
        room.setUpdatedAt(LocalDateTime.now());

        roomMapper.insert(room);
        logger.info("创建房间: {} - 游戏类型: {}", room.getRoomCode(), gameType);
        return room;
    }

    @Transactional
    public Room joinRoom(String roomCode, String guestId, String guestName) {
        Room room = roomMapper.findByRoomCode(roomCode);
        if (room == null) {
            throw new RuntimeException("房间不存在");
        }
        if (!"waiting".equals(room.getStatus())) {
            throw new RuntimeException("房间已满或已开始");
        }
        if (room.getHostId().equals(guestId)) {
            throw new RuntimeException("不能加入自己创建的房间");
        }

        room.setGuestId(guestId);
        room.setGuestName(guestName);
        room.setStatus("playing");
        room.setUpdatedAt(LocalDateTime.now());
        roomMapper.updateById(room);

        logger.info("加入房间: {} - 玩家: {}", roomCode, guestName);
        return room;
    }

    public Room getRoom(String roomCode) {
        return roomMapper.findByRoomCode(roomCode);
    }

    public List<Room> getWaitingRooms() {
        return roomMapper.findWaitingRooms();
    }

    public List<Room> getUserRooms(String userId) {
        return roomMapper.findUserRooms(userId);
    }

    @Transactional
    public void leaveRoom(String roomCode, String userId) {
        Room room = roomMapper.findByRoomCode(roomCode);
        if (room == null) {
            return;
        }

        if (room.getHostId().equals(userId)) {
            // 房主离开，删除房间
            roomMapper.deleteById(room.getId());
            logger.info("房主离开，删除房间: {}", roomCode);
        } else if (userId.equals(room.getGuestId())) {
            // 客人离开，重置房间状态
            room.setGuestId(null);
            room.setGuestName(null);
            room.setStatus("waiting");
            room.setUpdatedAt(LocalDateTime.now());
            roomMapper.updateById(room);
            logger.info("客人离开，重置房间: {}", roomCode);
        }
    }

    private String generateRoomCode() {
        // 生成6位随机房间代码
        return String.format("%06d", random.nextInt(1000000));
    }
}

