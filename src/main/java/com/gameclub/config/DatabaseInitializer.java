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
            // 检查是否需要添加 image_url 列
            addImageUrlColumnIfNotExists();
        }

        // 检查 rooms 表是否存在
        boolean roomsTableExists = checkTableExists("rooms");
        if (!roomsTableExists) {
            logger.info("rooms 表不存在，正在创建...");
            createRoomsTable();
            logger.info("rooms 表创建完成");
        } else {
            logger.info("rooms 表已存在");
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
                "image_url TEXT, " +
                "created_at TEXT, " +
                "updated_at TEXT" +
                ")";
        
        jdbcTemplate.execute(createTableSql);
    }

    private void addImageUrlColumnIfNotExists() {
        try (Connection connection = dataSource.getConnection()) {
            DatabaseMetaData metaData = connection.getMetaData();
            ResultSet columns = metaData.getColumns(null, null, "game_data", "image_url");
            boolean columnExists = columns.next();
            columns.close();
            
            if (!columnExists) {
                logger.info("game_data 表缺少 image_url 列，正在添加...");
                jdbcTemplate.execute("ALTER TABLE game_data ADD COLUMN image_url TEXT");
                logger.info("image_url 列添加完成");
            }
        } catch (Exception e) {
            logger.error("检查或添加 image_url 列时出错", e);
        }
    }

    private void createRoomsTable() {
        String createTableSql = "CREATE TABLE IF NOT EXISTS rooms (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "room_code TEXT NOT NULL UNIQUE, " +
                "game_type TEXT NOT NULL, " +
                "host_id TEXT NOT NULL, " +
                "host_name TEXT NOT NULL, " +
                "guest_id TEXT, " +
                "guest_name TEXT, " +
                "status TEXT NOT NULL DEFAULT 'waiting', " +
                "created_at TEXT, " +
                "updated_at TEXT" +
                ")";
        
        jdbcTemplate.execute(createTableSql);
    }
}

