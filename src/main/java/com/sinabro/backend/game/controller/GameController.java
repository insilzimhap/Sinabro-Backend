package com.sinabro.backend.game.controller;

import com.sinabro.backend.game.dto.GameResultDto;
import com.sinabro.backend.game.service.GameService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/games")
@RequiredArgsConstructor
public class GameController {

    private final GameService gameService;

    @PostMapping("/listening/complete")
    public ResponseEntity<String> completeListeningGame(@RequestBody GameResultDto resultDto) {
        gameService.processListeningGameResult(resultDto);
        return ResponseEntity.ok("Listening game result processed successfully.");
    }
}