package com.hybe.devops;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Component
public class ShutdownHandler implements ApplicationRunner {
    private static final Logger logger = LoggerFactory.getLogger(ShutdownHandler.class);

    @Override
    public void run(ApplicationArguments args) throws Exception {
        // Register a shutdown hook
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            // Perform any necessary cleanup tasks or graceful shutdown logic here
            logger.info("Shutting down gracefully...");
        }));
    }
}
