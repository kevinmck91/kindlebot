package com.kevinmck91.kindlebot.controllers;

import java.io.IOException;
import java.net.URL;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.kevinmck91.kindlebot.dto.GithubItem;
import com.kevinmck91.kindlebot.service.MagazineService;

@Controller
@RequestMapping("/magazinebot")
public class MagazineWebController {

	@Autowired
	private MagazineService magazineService;

	private static final Logger logger = LoggerFactory.getLogger(MagazineWebController.class);

	// Constructor
	// The constructor is needed because of how Spring Boot does Dependency
	// Injection (DI).
	// To provide (or "inject") the HighlightsService into your controller, Spring
	// needs a constructor.
	public MagazineWebController(MagazineService magazineService) {
		this.magazineService = magazineService;
	}

	@GetMapping("")
	public String fetchAllMagazines(Model model) throws IOException {

		logger.info(" - ");

		List<GithubItem> githubItems = magazineService.fetchAllMagazines();

		model.addAttribute("githubItems", githubItems);

		return "magazinebot/magazines";
	}

	@PostMapping("/magazine/years")
	public String fetchMagazineYears(Model model, @RequestParam(required = false) String name, @RequestParam(required = false) String url) throws IOException {

		logger.info("Selected name: {}, url: {}", name, url);

		List<GithubItem> githubItems = magazineService.fetchData(url);

		for (GithubItem githubItem : githubItems) {
			githubItem.setDescription(githubItem.getName());
		}

		model.addAttribute("githubItems", githubItems);

		return "magazinebot/years";
	}

	@PostMapping("/magazine/issues")
	public String fetchMagazineIssues(Model model, @RequestParam(required = false) String name, @RequestParam(required = false) String url) throws IOException {

		logger.info("Selected name: {}, url: {}", name, url);

		List<GithubItem> githubItems = magazineService.fetchData(url);

		for (GithubItem githubItem : githubItems) {
			githubItem.setDescription(githubItem.getName());
		}

		model.addAttribute("githubItems", githubItems);

		return "magazinebot/issues";
	}

	@PostMapping("/magazine/formats")
	public String fetchMagazineFormats(Model model, @RequestParam(required = false) String name, @RequestParam(required = false) String url) throws IOException {

		logger.info("Selected name: {}, url: {}", name, url);

		List<GithubItem> githubItems = magazineService.fetchData(url);

		String imageUrl = null;

		for (GithubItem githubItem : githubItems) {

			githubItem.setDescription(githubItem.getName());

			// Find the cover image
			if ("cover.jpg".equalsIgnoreCase(githubItem.getName())) {
				imageUrl = githubItem.getDownload_url();
			}
		}

		model.addAttribute("githubItems", githubItems);
		model.addAttribute("imageUrl", imageUrl);

		return "magazinebot/formats";
	}

	@PostMapping("/magazine/formats/download")
	public ResponseEntity<Resource> downloadformat(@RequestParam(required = false) String name, @RequestParam(required = false) String download_url) throws Exception {

		logger.info("Downloading PDF from {}", download_url);

		URL url = new URL(download_url);

		InputStreamResource resource = new InputStreamResource(url.openStream());

		if (name.contains("pdf"))
			return ResponseEntity.ok().header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + name + ".pdf\"").contentType(MediaType.APPLICATION_PDF).body(resource);
		else if (name.contains("epub"))
			return ResponseEntity.ok().header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + name + ".epub\"").contentType(MediaType.parseMediaType("application/epub+zip")).body(resource);
		else if (name.contains("azw3"))
			return ResponseEntity.ok().header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + name + ".epub\"").contentType(MediaType.parseMediaType("application/epub+zip")).body(resource);
		else
			return null;
	}

}