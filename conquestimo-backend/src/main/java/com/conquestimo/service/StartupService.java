package com.conquestimo.service;

import com.conquestimo.repository.GameRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class StartupService implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(StartupService.class);

    private final GameRepository gameRepository;

    @Value("${app.delete-old-games:false}")
    private boolean deleteOldGames;

    public StartupService(GameRepository gameRepository) {
        this.gameRepository = gameRepository;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (deleteOldGames) {
            long count = gameRepository.count();
            gameRepository.deleteAll();
            log.info("app.delete-old-games=true: deleted {} game(s) on startup", count);
        }
    }
}
