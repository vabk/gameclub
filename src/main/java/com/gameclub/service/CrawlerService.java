package com.gameclub.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.gameclub.entity.GameData;
import com.gameclub.mapper.GameDataMapper;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class CrawlerService {
    private static final Logger logger = LoggerFactory.getLogger(CrawlerService.class);

    @Autowired
    private GameDataMapper gameDataMapper;

    // 每三个月执行一次（90天 = 7776000000毫秒）
    @Scheduled(fixedRate = 7776000000L)
    public void scheduledCrawl() {
        logger.info("开始定时爬取游戏数据...");
        crawlDeltaForce();
        crawlYjwujian();
    }

    public void triggerCrawl() {
        logger.info("手动触发爬取游戏数据...");
        crawlDeltaForce();
        crawlYjwujian();
    }

    @Transactional
    public void crawlDeltaForce() {
        try {
            logger.info("开始爬取三角洲数据...");
            String url = "https://df.qq.com/index.shtml#part3";
            Document doc = Jsoup.connect(url)
                    .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36")
                    .timeout(10000)
                    .get();

            // 删除旧数据
            QueryWrapper<GameData> deleteWrapper = new QueryWrapper<>();
            deleteWrapper.eq("game_type", "delta");
            gameDataMapper.delete(deleteWrapper);

            // 爬取干员数据（包含图片）
            List<GameDataItem> characters = extractDeltaCharactersWithImages(doc);
            for (GameDataItem item : characters) {
                GameData data = new GameData();
                data.setGameType("delta");
                data.setDataType("character");
                data.setName(item.name);
                data.setImageUrl(item.imageUrl);
                data.setCreatedAt(LocalDateTime.now());
                data.setUpdatedAt(LocalDateTime.now());
                gameDataMapper.insert(data);
            }

            // 爬取地图数据（只保留烽火地带）
            List<GameDataItem> maps = extractDeltaMapsWithImages(doc);
            for (GameDataItem item : maps) {
                GameData data = new GameData();
                data.setGameType("delta");
                data.setDataType("map");
                data.setName(item.name);
                data.setImageUrl(item.imageUrl);
                data.setCreatedAt(LocalDateTime.now());
                data.setUpdatedAt(LocalDateTime.now());
                gameDataMapper.insert(data);
            }

            // 爬取武器数据（包含图片）
            List<GameDataItem> weapons = extractDeltaWeaponsWithImages(doc);
            for (GameDataItem item : weapons) {
                GameData data = new GameData();
                data.setGameType("delta");
                data.setDataType("weapon");
                data.setName(item.name);
                data.setImageUrl(item.imageUrl);
                data.setCreatedAt(LocalDateTime.now());
                data.setUpdatedAt(LocalDateTime.now());
                gameDataMapper.insert(data);
            }

            logger.info("三角洲数据爬取完成，共爬取 {} 条数据", 
                    characters.size() + maps.size() + weapons.size());
        } catch (Exception e) {
            logger.error("爬取三角洲数据失败", e);
            // 如果爬取失败，使用默认数据
            initDefaultDeltaData();
        }
    }

    @Transactional
    public void crawlYjwujian() {
        try {
            logger.info("开始爬取永劫无间数据...");
            String url = "https://www.yjwujian.cn/";
            Document doc = Jsoup.connect(url)
                    .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36")
                    .timeout(10000)
                    .get();

            // 删除旧数据
            QueryWrapper<GameData> deleteWrapper = new QueryWrapper<>();
            deleteWrapper.eq("game_type", "yjwujian");
            gameDataMapper.delete(deleteWrapper);

            // 爬取英雄数据（包含图片）
            List<GameDataItem> heroes = extractYjwujianHeroesWithImages(doc);
            for (GameDataItem item : heroes) {
                GameData data = new GameData();
                data.setGameType("yjwujian");
                data.setDataType("hero");
                data.setName(item.name);
                data.setImageUrl(item.imageUrl);
                data.setCreatedAt(LocalDateTime.now());
                data.setUpdatedAt(LocalDateTime.now());
                gameDataMapper.insert(data);
            }

            // 爬取地图数据（包含图片）
            List<GameDataItem> maps = extractYjwujianMapsWithImages(doc);
            for (GameDataItem item : maps) {
                GameData data = new GameData();
                data.setGameType("yjwujian");
                data.setDataType("map");
                data.setName(item.name);
                data.setImageUrl(item.imageUrl);
                data.setCreatedAt(LocalDateTime.now());
                data.setUpdatedAt(LocalDateTime.now());
                gameDataMapper.insert(data);
            }

            // 爬取武器数据（包含图片）
            List<GameDataItem> weapons = extractYjwujianWeaponsWithImages(doc);
            for (GameDataItem item : weapons) {
                GameData data = new GameData();
                data.setGameType("yjwujian");
                data.setDataType("weapon");
                data.setName(item.name);
                data.setImageUrl(item.imageUrl);
                data.setCreatedAt(LocalDateTime.now());
                data.setUpdatedAt(LocalDateTime.now());
                gameDataMapper.insert(data);
            }

            logger.info("永劫无间数据爬取完成，共爬取 {} 条数据", 
                    heroes.size() + maps.size() + weapons.size());
        } catch (Exception e) {
            logger.error("爬取永劫无间数据失败", e);
            // 如果爬取失败，使用默认数据
            initDefaultYjwujianData();
        }
    }

    // 辅助类用于存储名称和图片URL
    private static class GameDataItem {
        String name;
        String imageUrl;

        GameDataItem(String name, String imageUrl) {
            this.name = name;
            this.imageUrl = imageUrl;
        }
    }

    // 创建占位图片URL（URL编码中文）
    private String createPlaceholderUrl(String text) {
        try {
            String encoded = URLEncoder.encode(text, StandardCharsets.UTF_8.toString());
            return "https://via.placeholder.com/300x300?text=" + encoded;
        } catch (Exception e) {
            logger.warn("URL编码失败: {}", text, e);
            return "https://via.placeholder.com/300x300";
        }
    }

    // 在文本附近查找图片
    private String findImageNearText(Document doc, String text) {
        try {
            // 查找包含文本的元素
            Elements textElements = doc.select(String.format("*:containsOwn(%s)", text));
            for (Element textEl : textElements) {
                // 在父元素中查找图片
                Element parent = textEl.parent();
                if (parent != null) {
                    Elements imgs = parent.select("img");
                    if (!imgs.isEmpty()) {
                        String src = imgs.first().attr("src");
                        if (src != null && !src.isEmpty()) {
                            if (!src.startsWith("http")) {
                                URL baseUrl = new URL("https://df.qq.com");
                                src = new URL(baseUrl, src).toString();
                            }
                            return src;
                        }
                    }
                    // 也检查兄弟元素
                    Element nextSibling = textEl.nextElementSibling();
                    if (nextSibling != null) {
                        Elements siblingImgs = nextSibling.select("img");
                        if (!siblingImgs.isEmpty()) {
                            String src = siblingImgs.first().attr("src");
                            if (src != null && !src.isEmpty()) {
                                if (!src.startsWith("http")) {
                                    URL baseUrl = new URL("https://df.qq.com");
                                    src = new URL(baseUrl, src).toString();
                                }
                                return src;
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            logger.debug("查找文本附近的图片失败: {}", text, e);
        }
        return null;
    }

    private List<GameDataItem> extractDeltaCharactersWithImages(Document doc) {
        List<GameDataItem> characters = new ArrayList<>();
        Set<String> seenNames = new HashSet<>(); // 用于去重
        Set<String> seenUrls = new HashSet<>(); // 用于去重图片URL
        
        try {
            logger.info("开始提取干员信息...");
            
            // 根据提供的HTML结构：div.swiper.p4-thumbs .swiper-slide
            // 每个slide包含：<img src="..."> 和 <p>名称</p>
            // 尝试多种选择器以确保完整覆盖
            Elements slides = doc.select("div.swiper.p4-thumbs .swiper-slide, div.p4-thumbs .swiper-slide, .p4-thumbs .swiper-slide");
            
            logger.debug("找到 {} 个干员slide元素", slides.size());
            
            for (Element slide : slides) {
                // 跳过空的slide（如slide-none）
                if (slide.hasClass("slide-none")) {
                    continue;
                }
                
                // 查找图片
                Elements imgs = slide.select("img");
                if (!imgs.isEmpty()) {
                    Element img = imgs.first();
                    String src = img.attr("src");
                    
                    // 查找名称（在p标签中）
                    Elements nameElements = slide.select("p");
                    String name = null;
                    if (!nameElements.isEmpty()) {
                        name = nameElements.first().text().trim();
                        // 跳过空的slide
                        if (name.isEmpty() || name.equals(" ") || name.equals("&nbsp;")) {
                            continue;
                        }
                    }
                    
                    // 如果找到了图片和名称
                    if (src != null && !src.isEmpty() && name != null && !name.isEmpty()) {
                        // 转换为绝对URL（处理 //game.gtimg.cn 这种协议相对URL）
                        String finalSrc = src;
                        if (finalSrc.startsWith("//")) {
                            finalSrc = "https:" + finalSrc;
                        } else if (!finalSrc.startsWith("http")) {
                            try {
                                URL baseUrl = new URL("https://df.qq.com");
                                finalSrc = new URL(baseUrl, finalSrc).toString();
                            } catch (Exception e) {
                                logger.warn("转换图片URL失败: {}", src);
                                continue;
                            }
                        }
                        
                        // 去重：检查名称和URL是否已经存在
                        if (!seenNames.contains(name) && !seenUrls.contains(finalSrc)) {
                            characters.add(new GameDataItem(name, finalSrc));
                            seenNames.add(name);
                            seenUrls.add(finalSrc);
                            logger.debug("找到干员: {} - {}", name, finalSrc);
                        } else {
                            logger.debug("跳过重复干员: {} - {}", name, finalSrc);
                        }
                    }
                }
            }
            
            // 如果仍然没有提取到数据，使用默认的兵种数据
            if (characters.isEmpty()) {
                logger.warn("未能从网页提取干员数据，使用默认兵种数据");
                characters.add(new GameDataItem("突击兵", createPlaceholderUrl("突击兵")));
                characters.add(new GameDataItem("医疗兵", createPlaceholderUrl("医疗兵")));
                characters.add(new GameDataItem("工程兵", createPlaceholderUrl("工程兵")));
                characters.add(new GameDataItem("支援兵", createPlaceholderUrl("支援兵")));
            } else {
                logger.info("成功提取到 {} 个干员（已去重）", characters.size());
            }
        } catch (Exception e) {
            logger.error("提取干员数据失败", e);
            // 使用默认数据
            characters.add(new GameDataItem("突击兵", createPlaceholderUrl("突击兵")));
            characters.add(new GameDataItem("医疗兵", createPlaceholderUrl("医疗兵")));
            characters.add(new GameDataItem("工程兵", createPlaceholderUrl("工程兵")));
            characters.add(new GameDataItem("支援兵", createPlaceholderUrl("支援兵")));
        }
        return characters;
    }

    private List<GameDataItem> extractDeltaMapsWithImages(Document doc) {
        List<GameDataItem> maps = new ArrayList<>();
        Set<String> seenNames = new HashSet<>(); // 用于去重
        try {
            logger.info("开始提取烽火地带地图信息...");
            
            // 1. 查找"烽火地带"被激活时的状态（class="on"）
            Elements navLinks = doc.select("div.p7_nav a.on");
            boolean isFenghuoActive = false;
            for (Element link : navLinks) {
                String linkText = link.text().trim();
                if ("烽火地带".equals(linkText)) {
                    isFenghuoActive = true;
                    break;
                }
            }
            
            // 如果"烽火地带"未激活，尝试查找所有导航链接，看是否有"烽火地带"
            if (!isFenghuoActive) {
                Elements allNavLinks = doc.select("div.p7_nav a");
                for (Element link : allNavLinks) {
                    String linkText = link.text().trim();
                    if ("烽火地带".equals(linkText)) {
                        // 即使未激活，也尝试提取数据
                        logger.debug("烽火地带未激活，但仍尝试提取数据");
                        break;
                    }
                }
            }
            
            // 2. 查找对应的swiper组件（class包含p7_tab和p7_tab1）
            // 尝试多种选择器以确保找到正确的swiper
            Elements swipers = doc.select("div.swiper.p7_tab.p7_tab1, div.p7_tab.p7_tab1, .p7_tab1.swiper");
            if (swipers.isEmpty()) {
                // 如果找不到，尝试查找所有包含p7_tab的swiper
                swipers = doc.select("div.swiper[class*='p7_tab'], .p7_tab.swiper");
            }
            
            // 3. 提取swiper中所有地图名称
            List<String> mapNames = new ArrayList<>();
            for (Element swiper : swipers) {
                // 查找所有swiper-slide中的p标签文本
                Elements slides = swiper.select("div.swiper-slide p");
                for (Element slide : slides) {
                    String mapName = slide.text().trim();
                    if (mapName != null && !mapName.isEmpty() && !mapName.equals(" ") && !mapName.equals("&nbsp;")) {
                        // 跳过重复的地图名称
                        if (!seenNames.contains(mapName)) {
                            mapNames.add(mapName);
                            seenNames.add(mapName);
                            logger.debug("找到地图名称: {}", mapName);
                        }
                    }
                }
            }
            
            // 4. 提取map_bg图片的URL
            String imageUrl = null;
            Elements mapBgImages = doc.select("img.map_bg");
            if (!mapBgImages.isEmpty()) {
                // 优先使用data-pc-src，如果没有则使用src
                Element img = mapBgImages.first();
                imageUrl = img.attr("data-pc-src");
                if (imageUrl == null || imageUrl.isEmpty()) {
                    imageUrl = img.attr("src");
                }
                if (imageUrl == null || imageUrl.isEmpty()) {
                    imageUrl = img.attr("data-ipad-src");
                }
            }
            
            // 如果没找到map_bg，尝试查找其他相关图片
            if (imageUrl == null || imageUrl.isEmpty()) {
                Elements fallbackImages = doc.select("img[src*='p7-m1'], img[src*='p7'], img[class*='map']");
                if (!fallbackImages.isEmpty()) {
                    Element img = fallbackImages.first();
                    imageUrl = img.attr("data-pc-src");
                    if (imageUrl == null || imageUrl.isEmpty()) {
                        imageUrl = img.attr("src");
                    }
                }
            }
            
            // 转换为绝对URL
            if (imageUrl != null && !imageUrl.isEmpty()) {
                if (imageUrl.startsWith("//")) {
                    imageUrl = "https:" + imageUrl;
                } else if (!imageUrl.startsWith("http")) {
                    try {
                        URL baseUrl = new URL("https://df.qq.com");
                        imageUrl = new URL(baseUrl, imageUrl).toString();
                    } catch (Exception e) {
                        logger.warn("转换图片URL失败: {}", imageUrl);
                        imageUrl = null;
                    }
                }
            }
            
            // 5. 为每个地图名称创建GameDataItem，使用相同的图片URL
            if (!mapNames.isEmpty()) {
                // 如果没有找到图片，使用占位图
                if (imageUrl == null || imageUrl.isEmpty()) {
                    logger.warn("未能找到地图图片，使用占位图");
                }
                
                for (String mapName : mapNames) {
                    String finalImageUrl = (imageUrl != null && !imageUrl.isEmpty()) 
                        ? imageUrl 
                        : createPlaceholderUrl(mapName);
                    maps.add(new GameDataItem(mapName, finalImageUrl));
                    logger.debug("添加地图: {} - {}", mapName, finalImageUrl);
                }
                
                logger.info("成功提取 {} 个烽火地带地图", maps.size());
            } else {
                // 如果没有找到地图名称，使用默认数据
                logger.warn("未能从网页提取地图名称，使用默认数据");
                if (imageUrl == null || imageUrl.isEmpty()) {
                    imageUrl = createPlaceholderUrl("烽火地带");
                }
                maps.add(new GameDataItem("烽火地带", imageUrl));
            }
        } catch (Exception e) {
            logger.error("提取地图数据失败", e);
            maps.add(new GameDataItem("烽火地带", createPlaceholderUrl("烽火地带")));
        }
        return maps;
    }

    private List<GameDataItem> extractDeltaWeaponsWithImages(Document doc) {
        List<GameDataItem> weapons = new ArrayList<>();
        Set<String> seenNames = new HashSet<>(); // 用于去重
        Set<String> seenUrls = new HashSet<>(); // 用于去重图片URL
        
        try {
            logger.info("开始提取武器信息...");
            
            // 根据提供的HTML结构：div.swiper-slide.p5-bq
            // 每个slide包含：<img src="..."> 和 <p>武器名称</p>
            // 尝试多种选择器以确保完整覆盖所有武器
            Elements slides = doc.select(
                "div.swiper-slide.p5-bq, " +
                ".p5-bq.swiper-slide, " +
                ".swiper-slide[class*='p5-bq'], " +
                "div[class*='p5-bq'][class*='swiper-slide'], " +
                ".swiper .p5-bq, " +
                "[class*='p5'][class*='swiper-slide']"
            );
            
            logger.debug("找到 {} 个武器slide元素", slides.size());
            
            // 如果主要选择器没找到，尝试更宽泛的选择器
            if (slides.isEmpty()) {
                logger.debug("主要选择器未找到武器，尝试更宽泛的选择器");
                // 查找所有包含p5相关的slide，且包含img和p标签的
                Elements allSlides = doc.select(".swiper-slide");
                for (Element slide : allSlides) {
                    Elements imgs = slide.select("img");
                    Elements ps = slide.select("p");
                    // 如果这个slide包含图片和p标签，且图片src包含p5相关路径
                    if (!imgs.isEmpty() && !ps.isEmpty()) {
                        String src = imgs.first().attr("src");
                        if (src != null && (src.contains("p5") || src.contains("/p5_"))) {
                            slides.add(slide);
                        }
                    }
                }
                logger.debug("使用宽泛选择器找到 {} 个武器slide元素", slides.size());
            }
            
            for (Element slide : slides) {
                // 查找图片
                Elements imgs = slide.select("img");
                if (!imgs.isEmpty()) {
                    Element img = imgs.first();
                    String src = img.attr("src");
                    
                    // 查找名称（在p标签中）
                    Elements nameElements = slide.select("p");
                    String name = null;
                    if (!nameElements.isEmpty()) {
                        name = nameElements.first().text().trim();
                        // 跳过空的slide或只包含空格的slide
                        if (name.isEmpty() || name.equals(" ") || name.equals("&nbsp;")) {
                            continue;
                        }
                    }
                    
                    // 如果找到了图片和名称
                    if (src != null && !src.isEmpty() && name != null && !name.isEmpty()) {
                        // 转换为绝对URL（处理 //game.gtimg.cn 这种协议相对URL）
                        String finalSrc = src;
                        if (finalSrc.startsWith("//")) {
                            finalSrc = "https:" + finalSrc;
                        } else if (!finalSrc.startsWith("http")) {
                            try {
                                URL baseUrl = new URL("https://df.qq.com");
                                finalSrc = new URL(baseUrl, finalSrc).toString();
                            } catch (Exception e) {
                                logger.warn("转换图片URL失败: {}", src);
                                continue;
                            }
                        }
                        
                        // 去重：检查名称和URL是否已经存在
                        if (!seenNames.contains(name) && !seenUrls.contains(finalSrc)) {
                            weapons.add(new GameDataItem(name, finalSrc));
                            seenNames.add(name);
                            seenUrls.add(finalSrc);
                            logger.debug("找到武器: {} - {}", name, finalSrc);
                        } else {
                            logger.debug("跳过重复武器: {} - {}", name, finalSrc);
                        }
                    }
                }
            }
            
            // 如果没有提取到数据，使用默认数据
            if (weapons.isEmpty()) {
                logger.warn("未能从网页提取武器数据，使用默认数据");
                weapons.add(new GameDataItem("M4A1", createPlaceholderUrl("M4A1")));
                weapons.add(new GameDataItem("AK47", createPlaceholderUrl("AK47")));
                weapons.add(new GameDataItem("狙击步枪", createPlaceholderUrl("狙击步枪")));
                weapons.add(new GameDataItem("霰弹枪", createPlaceholderUrl("霰弹枪")));
            } else {
                logger.info("成功提取到 {} 个武器（已去重）", weapons.size());
            }
        } catch (Exception e) {
            logger.error("提取武器数据失败", e);
            weapons.add(new GameDataItem("M4A1", createPlaceholderUrl("M4A1")));
            weapons.add(new GameDataItem("AK47", createPlaceholderUrl("AK47")));
            weapons.add(new GameDataItem("狙击步枪", createPlaceholderUrl("狙击步枪")));
            weapons.add(new GameDataItem("霰弹枪", createPlaceholderUrl("霰弹枪")));
        }
        return weapons;
    }

    private List<GameDataItem> extractYjwujianHeroesWithImages(Document doc) {
        List<GameDataItem> heroes = new ArrayList<>();
        try {
            // 根据永劫无间官网实际结构提取英雄名称和图片
            Elements heroElements = doc.select("img[alt*='英雄'], img[alt*='角色'], a[href*='hero'] img, .hero img, [class*='hero'] img");
            
            for (Element imgElement : heroElements) {
                String name = imgElement.attr("alt");
                String imageUrl = imgElement.attr("src");
                
                if (name == null || name.isEmpty()) {
                    // 尝试从父元素获取名称
                    Element parent = imgElement.parent();
                    if (parent != null) {
                        name = parent.text().trim();
                    }
                }
                
                // 转换为绝对URL
                if (imageUrl != null && !imageUrl.isEmpty() && !imageUrl.startsWith("http")) {
                    try {
                        URL baseUrl = new URL("https://www.yjwujian.cn");
                        imageUrl = new URL(baseUrl, imageUrl).toString();
                    } catch (Exception e) {
                        logger.warn("转换图片URL失败: {}", imageUrl);
                    }
                }
                
                if (name != null && !name.isEmpty() && name.length() < 20) {
                    heroes.add(new GameDataItem(name, imageUrl));
                }
            }
            
            // 如果提取失败，使用默认数据
            if (heroes.isEmpty()) {
                String[] defaultHeroes = {"宁红夜", "特木尔", "迦南", "季沧海", "天海", "胡桃", "妖刀姬", "崔三娘"};
                for (String hero : defaultHeroes) {
                    heroes.add(new GameDataItem(hero, createPlaceholderUrl(hero)));
                }
            }
        } catch (Exception e) {
            logger.warn("提取英雄数据失败，使用默认数据", e);
            String[] defaultHeroes = {"宁红夜", "特木尔", "迦南"};
            for (String hero : defaultHeroes) {
                heroes.add(new GameDataItem(hero, createPlaceholderUrl(hero)));
            }
        }
        return heroes;
    }

    private List<GameDataItem> extractYjwujianMapsWithImages(Document doc) {
        List<GameDataItem> maps = new ArrayList<>();
        try {
            Elements mapElements = doc.select("img[alt*='地图'], a[href*='map'] img, .map img, [class*='map'] img");
            
            for (Element imgElement : mapElements) {
                String name = imgElement.attr("alt");
                String imageUrl = imgElement.attr("src");
                
                if (name == null || name.isEmpty()) {
                    Element parent = imgElement.parent();
                    if (parent != null) {
                        name = parent.text().trim();
                    }
                }
                
                if (imageUrl != null && !imageUrl.isEmpty() && !imageUrl.startsWith("http")) {
                    try {
                        URL baseUrl = new URL("https://www.yjwujian.cn");
                        imageUrl = new URL(baseUrl, imageUrl).toString();
                    } catch (Exception e) {
                        logger.warn("转换图片URL失败: {}", imageUrl);
                    }
                }
                
                if (name != null && !name.isEmpty() && name.length() < 30) {
                    maps.add(new GameDataItem(name, imageUrl));
                }
            }
            
            if (maps.isEmpty()) {
                String[] defaultMaps = {"聚窟洲", "火罗国", "混沌神狱", "龙隐洞天"};
                for (String map : defaultMaps) {
                    maps.add(new GameDataItem(map, createPlaceholderUrl(map)));
                }
            }
        } catch (Exception e) {
            logger.warn("提取地图数据失败，使用默认数据", e);
            String[] defaultMaps = {"聚窟洲", "火罗国"};
            for (String map : defaultMaps) {
                maps.add(new GameDataItem(map, createPlaceholderUrl(map)));
            }
        }
        return maps;
    }

    private List<GameDataItem> extractYjwujianWeaponsWithImages(Document doc) {
        List<GameDataItem> weapons = new ArrayList<>();
        try {
            Elements weaponElements = doc.select("img[alt*='武器'], a[href*='weapon'] img, .weapon img, [class*='weapon'] img");
            
            for (Element imgElement : weaponElements) {
                String name = imgElement.attr("alt");
                String imageUrl = imgElement.attr("src");
                
                if (name == null || name.isEmpty()) {
                    Element parent = imgElement.parent();
                    if (parent != null) {
                        name = parent.text().trim();
                    }
                }
                
                if (imageUrl != null && !imageUrl.isEmpty() && !imageUrl.startsWith("http")) {
                    try {
                        URL baseUrl = new URL("https://www.yjwujian.cn");
                        imageUrl = new URL(baseUrl, imageUrl).toString();
                    } catch (Exception e) {
                        logger.warn("转换图片URL失败: {}", imageUrl);
                    }
                }
                
                if (name != null && !name.isEmpty()) {
                    weapons.add(new GameDataItem(name, imageUrl));
                }
            }
            
            // 如果提取失败，使用默认数据
            if (weapons.isEmpty()) {
                String[] defaultWeapons = {"长剑", "太刀", "阔刀", "枪", "双节棍", "匕首", "双刀", "双戟", "扇", "横刀", 
                                          "斩马刀", "棍", "链剑", "拳刃", "弓", "连弩", "鸟铳", "火炮", "喷火筒", "五眼铳", 
                                          "一窝蜂", "万刃轮"};
                for (String weapon : defaultWeapons) {
                    weapons.add(new GameDataItem(weapon, createPlaceholderUrl(weapon)));
                }
            }
        } catch (Exception e) {
            logger.warn("提取武器数据失败，使用默认数据", e);
            String[] defaultWeapons = {"长剑", "太刀", "阔刀"};
            for (String weapon : defaultWeapons) {
                weapons.add(new GameDataItem(weapon, createPlaceholderUrl(weapon)));
            }
        }
        return weapons;
    }

    private void initDefaultDeltaData() {
        logger.info("初始化三角洲默认数据...");
        
        // 默认干员（兵种）
        String[] characters = {"突击兵", "医疗兵", "工程兵", "支援兵"};
        for (String character : characters) {
            GameData data = new GameData();
            data.setGameType("delta");
            data.setDataType("character");
            data.setName(character);
            data.setImageUrl(createPlaceholderUrl(character));
            data.setCreatedAt(LocalDateTime.now());
            data.setUpdatedAt(LocalDateTime.now());
            gameDataMapper.insert(data);
        }

        // 只保留烽火地带地图
        GameData mapData = new GameData();
        mapData.setGameType("delta");
        mapData.setDataType("map");
        mapData.setName("烽火地带");
        mapData.setImageUrl(createPlaceholderUrl("烽火地带"));
        mapData.setCreatedAt(LocalDateTime.now());
        mapData.setUpdatedAt(LocalDateTime.now());
        gameDataMapper.insert(mapData);

        // 默认武器
        String[] weapons = {"M4A1", "AK47", "狙击步枪", "霰弹枪"};
        for (String weapon : weapons) {
            GameData data = new GameData();
            data.setGameType("delta");
            data.setDataType("weapon");
            data.setName(weapon);
            data.setImageUrl(createPlaceholderUrl(weapon));
            data.setCreatedAt(LocalDateTime.now());
            data.setUpdatedAt(LocalDateTime.now());
            gameDataMapper.insert(data);
        }
    }

    private void initDefaultYjwujianData() {
        logger.info("初始化永劫无间默认数据...");
        
        // 默认英雄
        String[] heroes = {"宁红夜", "特木尔", "迦南", "季沧海", "天海", "胡桃", "妖刀姬", "崔三娘", "无尘", "岳山"};
        for (String hero : heroes) {
            GameData data = new GameData();
            data.setGameType("yjwujian");
            data.setDataType("hero");
            data.setName(hero);
            data.setImageUrl(createPlaceholderUrl(hero));
            data.setCreatedAt(LocalDateTime.now());
            data.setUpdatedAt(LocalDateTime.now());
            gameDataMapper.insert(data);
        }

        // 默认地图
        String[] maps = {"聚窟洲", "火罗国", "混沌神狱", "龙隐洞天"};
        for (String map : maps) {
            GameData data = new GameData();
            data.setGameType("yjwujian");
            data.setDataType("map");
            data.setName(map);
            data.setImageUrl(createPlaceholderUrl(map));
            data.setCreatedAt(LocalDateTime.now());
            data.setUpdatedAt(LocalDateTime.now());
            gameDataMapper.insert(data);
        }

        // 默认武器
        String[] weapons = {"长剑", "太刀", "阔刀", "枪", "双节棍", "匕首", "双刀", "双戟", "扇", "横刀", 
                           "斩马刀", "棍", "链剑", "拳刃", "弓", "连弩", "鸟铳", "火炮", "喷火筒", "五眼铳", 
                           "一窝蜂", "万刃轮"};
        for (String weapon : weapons) {
            GameData data = new GameData();
            data.setGameType("yjwujian");
            data.setDataType("weapon");
            data.setName(weapon);
            data.setImageUrl(createPlaceholderUrl(weapon));
            data.setCreatedAt(LocalDateTime.now());
            data.setUpdatedAt(LocalDateTime.now());
            gameDataMapper.insert(data);
        }
    }
}
