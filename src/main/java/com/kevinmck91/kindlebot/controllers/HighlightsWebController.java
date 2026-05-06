package com.kevinmck91.kindlebot.controllers;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.concurrent.ExecutionException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kevinmck91.kindlebot.dto.Highlight;
import com.kevinmck91.kindlebot.dto.request.BulkUpdateStringRequest;
import com.kevinmck91.kindlebot.dto.request.HighlightRequest;
import com.kevinmck91.kindlebot.dto.response.FileUploadResponse;
import com.kevinmck91.kindlebot.service.HighlightService;

@Controller
@RequestMapping("/web")
public class HighlightsWebController {

	private final HighlightService highlightService;
	private static final Logger logger = LoggerFactory.getLogger(HighlightsWebController.class);

	@Autowired
	public HighlightsWebController(HighlightService highlightService) {
		this.highlightService = highlightService;
	}

	@GetMapping("/home")
	public String showHomePage(Model model) {

		logger.info("[Get] - [/web/home] - [showHomePage]");

		return "home";
	}

	@GetMapping("/admin")
	public String showAdminPage(Model model) {
		try {

			logger.info("[Get] - [/web/admin] - [showAdminPage]");

			// Fetch all highlights from the service and sort them by getTimestampHash
			List<Highlight> highlights = highlightService.getAllHighlights();

			model.addAttribute("highlights", highlights);

		} catch (Exception e) {

			logger.error("Error fetching highlights", e);
			model.addAttribute("errorMessage", "Could not load highlights: " + e.getMessage());
		}

		// maps to src/main/resources/templates/admin.html
		return "admin";
	}

	@GetMapping("/admin/exportJson")
	public ResponseEntity<List<Highlight>> exportJson() {

		logger.info("[Get] - [/web/admin/exportJson] - [exportJson]");

		List<Highlight> highlights = highlightService.getAllHighlights();

		String today = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));

		String fileName = today + "_highlight.json";

		return ResponseEntity.ok().header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + fileName).header(HttpHeaders.CONTENT_TYPE, "application/json").body(highlights);
	}

	@PostMapping("/admin/importJson")
	public String importJson(@RequestParam("file") MultipartFile file) {

		logger.info("[POST] - [/web/admin/importJson]");

		if (file.isEmpty()) {
			return "redirect:/web/admin";
		}

		try {
			ObjectMapper objectMapper = new ObjectMapper();

			List<Highlight> highlights = objectMapper.readValue(file.getInputStream(), new TypeReference<List<Highlight>>() {
			});

			// TODO: Remove this loop and do a Bulk update instead
			for (Highlight highlight : highlights) {
				highlightService.saveIndividualHighlight(highlight);
			}

			return "redirect:/web/admin";

		} catch (Exception e) {
			logger.error("Import failed", e);
			return "redirect:/web/admin";
		}
	}

	@PostMapping("/admin/updateExcerpt")
	public ResponseEntity<String> updateExcerpt(@RequestBody BulkUpdateStringRequest updateRequest) {

		logger.info("[Post] - [/web/admin/updateExcerpt] - [updateExcerpt]");

		highlightService.bulkUpdateExcerpt(updateRequest);

		return ResponseEntity.ok("Excerpt updated successfully");
	}

	@PostMapping("/admin/updateTitle")
	public ResponseEntity<String> updateTitle(@RequestBody BulkUpdateStringRequest updateRequest) {

		logger.info("[Post] - [/web/admin/updateTitle] - [updateTitle]");

		highlightService.bulkUpdateTitle(updateRequest);

		return ResponseEntity.ok("Title updated successfully");
	}

	@PostMapping("/admin/updateAuthor")
	public ResponseEntity<String> updateAuthor(@RequestBody BulkUpdateStringRequest updateRequest) {

		logger.info("[Post] - [/web/admin/updateAuthor] - [updateAuthor]");

		highlightService.bulkUpdateAuthor(updateRequest);

		return ResponseEntity.ok("Author updated successfully");
	}

	@PostMapping("/admin/updateSource")
	public ResponseEntity<String> updateSource(@RequestBody BulkUpdateStringRequest updateRequest) {

		logger.info("[Post] - [/web/admin/updateSource] - [updateSource]");

		highlightService.bulkUpdateSource(updateRequest);

		return ResponseEntity.ok("Source updated successfully");
	}

	@PostMapping("/admin/addTag")
	public ResponseEntity<String> addTag(@RequestBody BulkUpdateStringRequest updateRequest) {

		logger.info("[Post] - [/web/admin/addTag] - [addTag]");

		highlightService.bulkAddTag(updateRequest);

		return ResponseEntity.ok("Tag added successfully");
	}

	@PostMapping("/admin/removeTag")
	public ResponseEntity<String> removeTag(@RequestBody BulkUpdateStringRequest updateRequest) {

		logger.info("[Post] - [/web/admin/removeTag] - [removeTag]");

		highlightService.bulkRemoveTag(updateRequest);

		return ResponseEntity.ok("Tag removed successfully");
	}

	@PostMapping("/admin/updateVisibility")
	public ResponseEntity<String> updateVisibility(@RequestBody BulkUpdateStringRequest updateRequest) {

		logger.info("[Post] - [/web/admin/updateVisibility] - [updateVisibility]");

		highlightService.bulkUpdateVisibility(updateRequest);

		return ResponseEntity.ok("Visibility updated successfully");
	}

	@PostMapping("/admin/manualUpload")
	public String manualUpload(HighlightRequest highlightRequest) throws InterruptedException, ExecutionException {

		logger.info("[Post] - [/web/admin/manualUpload] - [manualUpload]");

		List<Highlight> highlights = highlightService.addManualHightlight(highlightRequest);

		return "redirect:/web/admin";
	}

	@PostMapping("/admin/fileUpload")
	public String fileUpload(@RequestParam("file") MultipartFile file) throws Exception {

		logger.info("[Post] - [/web/admin/fileUpload] - [fileUpload]");

		FileUploadResponse results = highlightService.parseInputFile(file);

		return "redirect:/web/admin";
	}
}
