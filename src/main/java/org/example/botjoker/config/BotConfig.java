package org.example.botjoker.config;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

@Configuration
@Data
@EnableScheduling
public class BotConfig {
    @Value("${bot.name}")
    String botUserName;
    @Value("${bot.token}")
    String token;
}
