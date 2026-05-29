package com.kevinmck91.kindlebot.config;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;

@Configuration
public class FirebaseConfig {

	private static final Logger log = LoggerFactory.getLogger(FirebaseConfig.class);

	@Value("${spring.firebase.credentialsPath}")
	private String credentialsPath;

	@Value("${spring.firebase.projectId}")
	private String projectId;

	@Value("${spring.firebase.storageBucket}")
	private String storageBucket;

	@Value("${spring.firebase.databaseUrl}")
	private String databaseUrl;

	private final FirebaseStatusService firebaseStatusService;

	public FirebaseConfig(FirebaseStatusService firebaseStatusService) {
		this.firebaseStatusService = firebaseStatusService;
	}

	@Bean
	FirebaseApp firebaseApp() throws IOException {

		try {
			log.info("🔥 Firebase initialization starting...");
			log.info("📄 Raw credentials path from config: {}", credentialsPath);

			if (credentialsPath == null || credentialsPath.isBlank()) {
				throw new IllegalStateException("spring.firebase.credentials is NOT set");
			}

			File file = new File(credentialsPath);

			log.info("📍 Absolute path resolved to: {}", file.getAbsolutePath());
			log.info("📁 File exists: {}", file.exists());
			log.info("📁 File readable: {}", file.canRead());

			if (!file.exists()) {
				throw new IllegalStateException("Firebase credentials file not found at: " + file.getAbsolutePath());
			}

			FileInputStream serviceAccount = new FileInputStream(file);

			FirebaseOptions options = FirebaseOptions.builder().setProjectId(projectId).setCredentials(GoogleCredentials.fromStream(serviceAccount)).setStorageBucket(storageBucket).setDatabaseUrl(databaseUrl).build();

			FirebaseApp app;

			if (FirebaseApp.getApps().isEmpty()) {
				app = FirebaseApp.initializeApp(options);
			} else {
				app = FirebaseApp.getInstance();
			}

			firebaseStatusService.setAvailable(true);

			log.info("✅ Firebase initialized successfully");

			return app;

		} catch (Exception e) {

			firebaseStatusService.setAvailable(false);

			log.error("❌ Firebase failed to initialize", e);

			return null;
		}
	}
}