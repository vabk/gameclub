package com.gameclub.config;

import com.gameclub.service.CrawlerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
@org.springframework.core.annotation.Order(2) // 在 DatabaseInitializer 之后执行
public class InitialDataLoader implements CommandLineRunner {
    private static final Logger logger = LoggerFactory.getLogger(InitialDataLoader.class);

    @Autowired
    private CrawlerService crawlerService;

    @Override
    public void run(String... args) throws Exception {
        logger.info("应用启动，检查数据库数据...");
        // 首次启动时初始化默认数据
        try {
            crawlerService.crawlYjwujian();
            crawlerService.crawlDeltaForce();
            logger.info("初始数据加载完成");
        } catch (Exception e) {
            logger.warn("初始数据加载失败，将在首次使用时初始化", e);
        }
    }
}







