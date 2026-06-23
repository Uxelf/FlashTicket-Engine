package com.uxelf.dev.ticketDealer;

import org.springframework.boot.SpringApplication;

public class TestTicketDealerApplication {

	public static void main(String[] args) {
		SpringApplication.from(TicketDealerApplication::main).with(TestcontainersConfiguration.class).run(args);
	}

}
