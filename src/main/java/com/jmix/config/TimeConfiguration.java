package com.jmix.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;

@Configuration
public class TimeConfiguration {

    @Bean
    Clock applicationClock(@Value("${app.report.zone-id:Asia/Tehran}") String zoneId) {
        return Clock.system(java.time.ZoneId.of(zoneId));
    }
}
