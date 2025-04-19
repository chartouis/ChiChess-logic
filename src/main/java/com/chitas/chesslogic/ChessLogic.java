package com.chitas.chesslogic;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
public class ChessLogic {

	public static void main(String[] args) {
		SpringApplication.run(ChessLogic.class, args);
	}

}
