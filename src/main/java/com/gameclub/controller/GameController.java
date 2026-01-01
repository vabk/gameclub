package com.gameclub.controller;

import com.gameclub.service.GamePlayService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/game")
@CrossOrigin(origins = "http://localhost:3000")
public class GameController {

    @Autowired
    private GamePlayService gamePlayService;

    @PostMapping("/generate")
    public ResponseEntity<GamePlayService.GamePlayResult> generateGamePlay(@RequestBody Map<String, String> request) {
        String game = request.get("game");
        if (game == null || (!game.equals("delta") && !game.equals("yjwujian"))) {
            return ResponseEntity.badRequest().build();
        }
        GamePlayService.GamePlayResult result = gamePlayService.generateGamePlay(game);
        return ResponseEntity.ok(result);
    }
}







