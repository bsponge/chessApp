package com.example.demo.service;

import chessLib.GameSession;
import com.example.demo.gameHistory.GameHistory;
import com.example.demo.repository.GamesHistoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class GamesHistoryService {
    private GamesHistoryRepository gamesHistoryRepository;

    @Autowired
    public GamesHistoryService(GamesHistoryRepository gamesHistoryRepository) {
        this.gamesHistoryRepository = gamesHistoryRepository;
    }

    public void saveGameHistory(GameSession gameSession) {
        gamesHistoryRepository.save(new GameHistory(gameSession));
    }
}
