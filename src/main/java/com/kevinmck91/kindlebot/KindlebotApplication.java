package com.kevinmck91.kindlebot;

import java.io.IOException;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@EnableCaching
@SpringBootApplication
public class KindlebotApplication {

	public static void main(String[] args) throws IOException {

		SpringApplication.run(KindlebotApplication.class, args);

		System.out.println();
		System.out.println("╔═══════════════════════════════════════════════════════════╗");
		System.out.println("║                 🚀 APPLICATION STARTED						║");
		System.out.println("╠═══════════════════════════════════════════════════════════╣");
		System.out.println("║  🌐 Homepage  →  http://localhost:8080						║");
		System.out.println("╚═══════════════════════════════════════════════════════════╝");
		System.out.println();

	}

}
