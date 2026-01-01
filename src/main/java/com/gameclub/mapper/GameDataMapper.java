package com.gameclub.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.gameclub.entity.GameData;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface GameDataMapper extends BaseMapper<GameData> {
    @Select("SELECT * FROM game_data WHERE game_type = #{gameType} AND data_type = #{dataType}")
    List<GameData> findByGameTypeAndDataType(String gameType, String dataType);

    @Select("SELECT * FROM game_data WHERE game_type = #{gameType}")
    List<GameData> findByGameType(String gameType);
}







