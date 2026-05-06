package com.kevinmck91.kindlebot.controllers;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.kevinmck91.kindlebot.dto.Highlight;
import com.kevinmck91.kindlebot.dto.request.BulkUpdateStringRequest;
import com.kevinmck91.kindlebot.dto.request.HighlightRequest;
import com.kevinmck91.kindlebot.dto.request.InputStringRequest;
import com.kevinmck91.kindlebot.dto.response.FileUploadResponse;
import com.kevinmck91.kindlebot.service.HighlightService;

@RestController
@RequestMapping("/api/highlights")
public class HighlightsRestController {

	@Autowired
	private final HighlightService highlightService;

	private static final Logger logger = LoggerFactory.getLogger(HighlightsRestController.class);

	// Constructor
	// The constructor is needed because of how Spring Boot does Dependency
	// Injection (DI).
	// To provide (or "inject") the HighlightsService into your controller, Spring
	// needs a constructor.
	public HighlightsRestController(HighlightService highlightService) {
		this.highlightService = highlightService;
	}

	@GetMapping("")
	public ResponseEntity<List<Highlight>> getAllHighlights() {

		logger.info("[Get] - [/api/highlights] - [getAllHighlights]");

		List<Highlight> highlights = highlightService.getAllHighlights();

		return ResponseEntity.ok(highlights);
	}

	@GetMapping("/{timestampHash}")
	public ResponseEntity<List<Highlight>> getUniqueHighlight(@PathVariable String timestampHash) {

		logger.info("[Get] - [/api/highlights/{timestampHash}] - [getUniqueHighlight()]");

		List<Highlight> highlights = highlightService.getUniqueHighlight(timestampHash);

		return ResponseEntity.ok(highlights);
	}

	@PostMapping("/batch")
	public ResponseEntity<List<Highlight>> getHighlightsBatch(@RequestBody List<String> timestampHashes) {

		logger.info("[Post] - [/api/highlights/batch] - [getHighlightsBatch()]");

		List<Highlight> highlights = new ArrayList<Highlight>();

		for (String timestampHash : timestampHashes)
			highlights.add(highlightService.getUniqueHighlight(timestampHash).get(0));

		return ResponseEntity.ok(highlights);
	}

	@PostMapping("/importFile")
	public ResponseEntity<FileUploadResponse> fileImport(@RequestParam("file") MultipartFile file) throws Exception {

		logger.info("[Post] - [/api/highlights/importFile] - [fileImport()]");

		FileUploadResponse results = highlightService.parseInputFile(file);

		return ResponseEntity.ok(results);
	}

	@PostMapping("/importManual")
	public ResponseEntity<List<Highlight>> addManualHightlight(@RequestBody HighlightRequest highlightRequest) throws Exception {

		logger.info("[Post] - [/api/highlights/importManual] - [addManualHightlight()]");

		List<Highlight> highlights = highlightService.addManualHightlight(highlightRequest);

		return ResponseEntity.ok(highlights);
	}

	@PutMapping("/bulkUpdateTitle")
	public ResponseEntity<Integer> bulkUpdateTitle(@RequestBody BulkUpdateStringRequest updateRequest) {

		logger.info("[Put] - [/api/highlights/bulkUpdateTitle] - [bulkUpdateTitle()]");

		int rowsUpdated = highlightService.bulkUpdateTitle(updateRequest);

		if (rowsUpdated == 0)
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body(rowsUpdated);
		else
			return ResponseEntity.ok(rowsUpdated);
	}

	@PutMapping("/bulkUpdateAuthor")
	public ResponseEntity<Integer> bulkUpdateAuthor(@RequestBody BulkUpdateStringRequest updateRequest) {

		logger.info("[Put] - [/api/highlights/bulkUpdateAuthor] - [bulkUpdateAuthor()]");

		int rowsUpdated = highlightService.bulkUpdateAuthor(updateRequest);

		if (rowsUpdated == 0)
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body(rowsUpdated);
		else
			return ResponseEntity.ok(rowsUpdated);
	}

	@PutMapping("/bulkUpdateSource")
	public ResponseEntity<Integer> bulkUpdateSource(@RequestBody BulkUpdateStringRequest updateRequest) {

		logger.info("[Put] - [/api/highlight/bulkUpdateSource] - [bulkUpdateSource()]");

		int rowsUpdated = highlightService.bulkUpdateSource(updateRequest);

		if (rowsUpdated == 0)
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body(rowsUpdated);
		else
			return ResponseEntity.ok(rowsUpdated);
	}

	@PutMapping("/bulkAddTag")
	public ResponseEntity<Integer> bulkAddTag(@RequestBody BulkUpdateStringRequest request) {

		logger.info("[Put] - [/api/highlights/bulkAddTag] - [bulkAddTag()]");

		int rowsUpdated = highlightService.bulkAddTag(request);

		if (rowsUpdated == 0)
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body(rowsUpdated);
		else
			return ResponseEntity.ok(rowsUpdated);
	}

	@PutMapping("/bulkRemoveTag")
	public ResponseEntity<Integer> bulkRemoveTag(@RequestBody BulkUpdateStringRequest request) {

		logger.info("[Put] - [/api/highlights/bulkRemoveTag] - [bulkRemoveTag()]");

		int rowsUpdated = highlightService.bulkRemoveTag(request);

		if (rowsUpdated == 0)
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body(rowsUpdated);
		else
			return ResponseEntity.ok(rowsUpdated);
	}

	@PutMapping("/bulkUpdateVisibility")
	public ResponseEntity<String> updateVisibility(@RequestBody BulkUpdateStringRequest updateRequest) {

		logger.info("[Put] - [/api/highlights/updateVisibility] - [updateVisibility]");

		highlightService.bulkUpdateVisibility(updateRequest);

		return ResponseEntity.ok("Visibility updated successfully");
	}

	@PutMapping("/{timestampHash}/note")
	public ResponseEntity<String> updateNote(@PathVariable String timestampHash, @RequestBody InputStringRequest noteRequest) {

		logger.info("[Put] - [/api/highlights/{timestampHash}/note] - [updateNote()]");

		highlightService.updateNote(timestampHash, noteRequest);

		return ResponseEntity.ok("Visibility updated successfully");
	}

	@PutMapping("/updateExcerpt")
	public ResponseEntity<String> updateExcerpt(@RequestBody BulkUpdateStringRequest updateRequest) {

		logger.info("[Post] - [/api/highlights/updateExcerpt] - [updateExcerpt]");

		highlightService.bulkUpdateExcerpt(updateRequest);

		return ResponseEntity.ok("Excerpt updated successfully");
	}

	@DeleteMapping("")
	public void deleteAll() {

		logger.info("[Delete] - [/api/highlights/] - [deleteAll()]");

		highlightService.deleteAll();
	}

}