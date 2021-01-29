package com.example.demo.repository;

import com.example.demo.gameHistory.GameHistory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface GamesHistoryRepository extends JpaRepository<GameHistory, UUID> {
}
