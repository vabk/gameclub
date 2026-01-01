package com.gameclub.service;

import com.gameclub.entity.GameData;
import com.gameclub.mapper.GameDataMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
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
                logger.debug("查询到 {} 个三角洲干员", characters.size());
                if (!characters.isEmpty()) {
                    GameData selected = characters.get(random.nextInt(characters.size()));
                    logger.debug("选中的干员: {}, 图片URL: {}", selected.getName(), selected.getImageUrl());
                    result.setCharacter(selected.getName());
                    result.setCharacterImage(getImageUrl(selected));
                } else {
                    logger.warn("未找到三角洲干员数据");
                }

                List<GameData> maps = gameDataMapper.findByGameTypeAndDataType("delta", "map");
                if (!maps.isEmpty()) {
                    GameData selected = maps.get(random.nextInt(maps.size()));
                    result.setMap(selected.getName());
                    result.setMapImage(getImageUrl(selected));
                }

                List<GameData> weapons = gameDataMapper.findByGameTypeAndDataType("delta", "weapon");
                if (!weapons.isEmpty()) {
                    GameData selected = weapons.get(random.nextInt(weapons.size()));
                    result.setWeapon(selected.getName());
                    result.setWeaponImage(getImageUrl(selected));
                }
            } else if ("yjwujian".equals(gameType)) {
                // 永劫无间：英雄、地图、武器
                List<GameData> heroes = gameDataMapper.findByGameTypeAndDataType("yjwujian", "hero");
                if (!heroes.isEmpty()) {
                    GameData selected = heroes.get(random.nextInt(heroes.size()));
                    result.setHero(selected.getName());
                    result.setHeroImage(getImageUrl(selected));
                }

                List<GameData> maps = gameDataMapper.findByGameTypeAndDataType("yjwujian", "map");
                if (!maps.isEmpty()) {
                    GameData selected = maps.get(random.nextInt(maps.size()));
                    result.setMap(selected.getName());
                    result.setMapImage(getImageUrl(selected));
                }

                List<GameData> weapons = gameDataMapper.findByGameTypeAndDataType("yjwujian", "weapon");
                if (!weapons.isEmpty()) {
                    GameData selected = weapons.get(random.nextInt(weapons.size()));
                    result.setWeapon(selected.getName());
                    result.setWeaponImage(getImageUrl(selected));
                }
            }
        } catch (Exception e) {
            logger.error("生成游戏玩法失败", e);
            throw new RuntimeException("生成游戏玩法失败: " + e.getMessage());
        }

        return result;
    }

    private String getImageUrl(GameData gameData) {
        if (gameData.getImageUrl() != null && !gameData.getImageUrl().isEmpty()) {
            logger.debug("使用数据库中的图片URL: {} for {}", gameData.getImageUrl(), gameData.getName());
            return gameData.getImageUrl();
        }
        // 如果没有设置图片URL，生成一个占位图片URL
        // URL编码中文名称
        String encodedName = "";
        try {
            encodedName = URLEncoder.encode(gameData.getName(), StandardCharsets.UTF_8.toString());
        } catch (Exception e) {
            logger.warn("URL编码失败: {}", gameData.getName(), e);
            encodedName = gameData.getName();
        }
        String placeholderUrl = "https://via.placeholder.com/300x300?text=" + encodedName;
        logger.debug("生成占位图片URL: {} for {}", placeholderUrl, gameData.getName());
        return placeholderUrl;
    }

    public static class GamePlayResult {
        private String game;
        private String character;
        private String characterImage;
        private String hero;
        private String heroImage;
        private String map;
        private String mapImage;
        private String weapon;
        private String weaponImage;

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

        public String getCharacterImage() {
            return characterImage;
        }

        public void setCharacterImage(String characterImage) {
            this.characterImage = characterImage;
        }

        public String getHero() {
            return hero;
        }

        public void setHero(String hero) {
            this.hero = hero;
        }

        public String getHeroImage() {
            return heroImage;
        }

        public void setHeroImage(String heroImage) {
            this.heroImage = heroImage;
        }

        public String getMap() {
            return map;
        }

        public void setMap(String map) {
            this.map = map;
        }

        public String getMapImage() {
            return mapImage;
        }

        public void setMapImage(String mapImage) {
            this.mapImage = mapImage;
        }

        public String getWeapon() {
            return weapon;
        }

        public void setWeapon(String weapon) {
            this.weapon = weapon;
        }

        public String getWeaponImage() {
            return weaponImage;
        }

        public void setWeaponImage(String weaponImage) {
            this.weaponImage = weaponImage;
        }
    }
}

