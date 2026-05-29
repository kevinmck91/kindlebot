package com.kevinmck91.kindlebot.controllers;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import com.kevinmck91.kindlebot.config.FirebaseStatusService;
import com.kevinmck91.kindlebot.dto.response.AppDetails;

@Controller
public class HomeController {

	private final FirebaseStatusService firebaseStatusService;

	public HomeController(FirebaseStatusService firebaseStatusService) {
		this.firebaseStatusService = firebaseStatusService;
	}

	@GetMapping("/")
	public String loadHomepage(Model model) {

		boolean firebaseAvailable = firebaseStatusService.isAvailable();

		List<AppDetails> availableApplications = new ArrayList<>();

		availableApplications.add(new AppDetails(1, "Magazines", "/magazinebot", "📰", true));

		availableApplications.add(new AppDetails(2, "Highlights & Clippings - User", "/kindlebot/home", "📚", firebaseAvailable));

		availableApplications.add(new AppDetails(3, "Highlights & Clippings - Admin", "/kindlebot/admin", "⚙️", firebaseAvailable));

		model.addAttribute("apps", availableApplications);

		return "index.html";
	}
}