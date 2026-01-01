package com.gameclub.controller;

import com.gameclub.service.CrawlerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/crawler")
@CrossOrigin(origins = "http://localhost:3000")
public class CrawlerController {

    @Autowired
    private CrawlerService crawlerService;

    @PostMapping("/trigger")
    public ResponseEntity<Map<String, String>> triggerCrawl() {
        try {
            crawlerService.triggerCrawl();
            Map<String, String> response = new HashMap<>();
            response.put("message", "爬取任务已启动");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> response = new HashMap<>();
            response.put("error", "爬取任务启动失败: " + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }
}







