package com.gameclub.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;

@Component
@Order(1) // 确保在 InitialDataLoader 之前执行
public class DatabaseInitializer implements CommandLineRunner {
    private static final Logger logger = LoggerFactory.getLogger(DatabaseInitializer.class);

    @Autowired
    private DataSource dataSource;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Override
    public void run(String... args) throws Exception {
        logger.info("检查数据库表结构...");
        
        // 检查 game_data 表是否存在
        boolean tableExists = checkTableExists("game_data");
        
        if (!tableExists) {
            logger.info("game_data 表不存在，正在创建...");
            createGameDataTable();
            logger.info("game_data 表创建完成");
        } else {
            logger.info("game_data 表已存在");
        }
    }

    private boolean checkTableExists(String tableName) {
        try (Connection connection = dataSource.getConnection()) {
            DatabaseMetaData metaData = connection.getMetaData();
            ResultSet tables = metaData.getTables(null, null, tableName, null);
            boolean exists = tables.next();
            tables.close();
            return exists;
        } catch (Exception e) {
            logger.error("检查表是否存在时出错", e);
            return false;
        }
    }

    private void createGameDataTable() {
        String createTableSql = "CREATE TABLE IF NOT EXISTS game_data (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "game_type TEXT NOT NULL, " +
                "data_type TEXT NOT NULL, " +
                "name TEXT NOT NULL, " +
                "created_at TEXT, " +
                "updated_at TEXT" +
                ")";
        
        jdbcTemplate.execute(createTableSql);
    }
}

