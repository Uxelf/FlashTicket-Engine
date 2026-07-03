package com.uxelf.dev.ticketDealer.component;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

import java.util.List;

@ConfigurationProperties (prefix = "app")
@Component
@Getter @Setter
public class AppConfig {
    private List<RoomProperties> rooms;

    @Getter @Setter
    public static class RoomProperties {
        private int number;
        private int rows;
        private int seatsPerRow;
    }

}
