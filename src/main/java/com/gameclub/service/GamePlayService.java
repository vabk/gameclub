package com.gameclub.service;

import com.gameclub.entity.GameData;
import com.gameclub.mapper.GameDataMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Random;

@Service
public class GamePlayService {
    private static final Logger logger = LoggerFactory.getLogger(GamePlayService.class);
    private final Random random = new Random();

    @Autowired
    private GameDataMapper gameDataMapper;

    public GamePlayResult generateGamePlay(String gameType) {
        GamePlayResult result = new GamePlayResult();
        result.setGame(gameType);

        try {
            if ("delta".equals(gameType)) {
                // 三角洲：干员、地图、武器
                List<GameData> characters = gameDataMapper.findByGameTypeAndDataType("delta", "character");
                if (!characters.isEmpty()) {
                    result.setCharacter(characters.get(random.nextInt(characters.size())).getName());
                }

                List<GameData> maps = gameDataMapper.findByGameTypeAndDataType("delta", "map");
                if (!maps.isEmpty()) {
                    result.setMap(maps.get(random.nextInt(maps.size())).getName());
                }

                List<GameData> weapons = gameDataMapper.findByGameTypeAndDataType("delta", "weapon");
                if (!weapons.isEmpty()) {
                    result.setWeapon(weapons.get(random.nextInt(weapons.size())).getName());
                }
            } else if ("yjwujian".equals(gameType)) {
                // 永劫无间：英雄、地图、武器
                List<GameData> heroes = gameDataMapper.findByGameTypeAndDataType("yjwujian", "hero");
                if (!heroes.isEmpty()) {
                    result.setHero(heroes.get(random.nextInt(heroes.size())).getName());
                }

                List<GameData> maps = gameDataMapper.findByGameTypeAndDataType("yjwujian", "map");
                if (!maps.isEmpty()) {
                    result.setMap(maps.get(random.nextInt(maps.size())).getName());
                }

                List<GameData> weapons = gameDataMapper.findByGameTypeAndDataType("yjwujian", "weapon");
                if (!weapons.isEmpty()) {
                    result.setWeapon(weapons.get(random.nextInt(weapons.size())).getName());
                }
            }
        } catch (Exception e) {
            logger.error("生成游戏玩法失败", e);
            throw new RuntimeException("生成游戏玩法失败: " + e.getMessage());
        }

        return result;
    }

    public static class GamePlayResult {
        private String game;
        private String character;
        private String hero;
        private String map;
        private String weapon;

        public String getGame() {
            return game;
        }

        public void setGame(String game) {
            this.game = game;
        }

        public String getCharacter() {
            return character;
        }

        public void setCharacter(String character) {
            this.character = character;
        }

        public String getHero() {
            return hero;
        }

        public void setHero(String hero) {
            this.hero = hero;
        }

        public String getMap() {
            return map;
        }

        public void setMap(String map) {
            this.map = map;
        }

        public String getWeapon() {
            return weapon;
        }

        public void setWeapon(String weapon) {
            this.weapon = weapon;
        }
    }
}

