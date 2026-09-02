package com.uxelf.dev.ticketDealer;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class TicketDealerApplication {

	public static void main(String[] args) {
		SpringApplication.run(TicketDealerApplication.class, args);
	}

}
