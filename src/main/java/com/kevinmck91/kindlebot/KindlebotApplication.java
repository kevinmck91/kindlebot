package com.kevinmck91.kindlebot;

import java.io.FileInputStream;
import java.io.IOException;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;

@EnableCaching
@SpringBootApplication
public class KindlebotApplication {

	public static void main(String[] args) throws IOException {

		SpringApplication.run(KindlebotApplication.class, args);

		System.out.println();
		System.out.println("╔════════════════════════════════════════════════════════════╗");
		System.out.println("║                 🚀 APPLICATION STARTED                    ║");
		System.out.println("╠════════════════════════════════════════════════════════════╣");
		System.out.println("║  🔐 Admin Application  →  http://localhost:8080/web/admin ║");
		System.out.println("║  🌐 Main  Application  →  http://localhost:8080/web/home  ║");
		System.out.println("╚════════════════════════════════════════════════════════════╝");
		System.out.println();

	}

}
