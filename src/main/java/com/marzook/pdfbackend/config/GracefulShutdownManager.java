package com.marzook.pdfbackend.config;

import com.zaxxer.hikari.HikariDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.context.event.EventListener;

import jakarta.annotation.PreDestroy;
import javax.sql.DataSource;

@Configuration
public class GracefulShutdownManager {

    private static final Logger log = LoggerFactory.getLogger(GracefulShutdownManager.class);

    private final DataSource dataSource;

    @Autowired
    public GracefulShutdownManager(DataSource dataSource) {
        this.dataSource = dataSource;
    }


    @EventListener(ContextClosedEvent.class)
    public void onApplicationShutdown() {
        log.info("Application shutting down - closing database connections...");
        closeConnections();
    }


    @PreDestroy
    public void preDestroy() {
        log.info("PreDestroy hook triggered - ensuring connections are closed...");
        closeConnections();
    }

    private void closeConnections() {
        if (dataSource instanceof HikariDataSource hikariDataSource) {

            if (!hikariDataSource.isClosed()) {
                log.info("Closing HikariCP connection pool...");
                log.info("Active connections before close: {}",
                        hikariDataSource.getHikariPoolMXBean().getActiveConnections());
                log.info("Total connections before close: {}",
                        hikariDataSource.getHikariPoolMXBean().getTotalConnections());

                try {
                    hikariDataSource.close();
                    log.info("HikariCP connection pool closed successfully");
                } catch (Exception e) {
                    log.error("Error closing HikariCP connection pool", e);
                }
            } else {
                log.info("HikariCP connection pool already closed");
            }
        }
    }
}