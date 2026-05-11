package com.kevinmck91.kindlebot.controllers;

import java.util.ArrayList;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import com.kevinmck91.kindlebot.dto.response.AppDetails;

import jakarta.servlet.http.HttpServletRequest;

@RestController
public class HomeController {

	@GetMapping("/")
	public ResponseEntity<List<AppDetails>> loadHomepage(HttpServletRequest request) {

		List<AppDetails> availableApplications = new ArrayList<>();

		availableApplications.add(new AppDetails(1, "Kindle Highlights & Excerpts", "localhost:8080/web/home"));

		return ResponseEntity.ok(availableApplications);
	}
}
