package com.basicsecurity.util;

import com.basicsecurity.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class DbConnectionChecker implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(DbConnectionChecker.class);

    private final UserRepository userRepository;

    public DbConnectionChecker(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public void run(String... args) throws Exception {
        try {
            // Attempt a simple database operation
            long userCount = userRepository.count();
            logger.info("*************😁😁😁😁😁😁😁😁😁😁😁😁😁😁*******************************");
            logger.info("             Database Connected Successfully!     ");
            logger.info("             Current user count: {}", userCount);
            logger.info("**************************************************");

        } catch (Exception e) {
            logger.error("!!!!!!!!!!!!!😈😈😈😈😈😈😈😈😈😈😈😈!!!!!!!!!!");
            logger.error("             Database Connection FAILED!          ");
            logger.error("             Error: {}", e.getMessage());
            logger.error("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
        }
    }
}
