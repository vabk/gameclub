package com.gameclub.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.gameclub.entity.Room;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface RoomMapper extends BaseMapper<Room> {
    @Select("SELECT * FROM rooms WHERE room_code = #{roomCode}")
    Room findByRoomCode(String roomCode);

    @Select("SELECT * FROM rooms WHERE status = 'waiting' ORDER BY created_at DESC")
    List<Room> findWaitingRooms();

    @Select("SELECT * FROM rooms WHERE (host_id = #{userId} OR guest_id = #{userId}) AND status != 'finished'")
    List<Room> findUserRooms(String userId);
}

