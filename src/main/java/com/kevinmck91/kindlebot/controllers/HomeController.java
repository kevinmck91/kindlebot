package com.kevinmck91.kindlebot.controllers;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import com.kevinmck91.kindlebot.dto.response.AppDetails;

@Controller
public class HomeController {

	@GetMapping("/")
	public String loadHomepage(Model model) {

		List<AppDetails> availableApplications = new ArrayList<>();

		availableApplications.add(new AppDetails(1, "Magazines", "/magazinebot", "📰"));

		availableApplications.add(new AppDetails(2, "Highlights & Clippings", "/kindlebot/home", "📚"));

		model.addAttribute("apps", availableApplications);

		return "index.html";
	}
}