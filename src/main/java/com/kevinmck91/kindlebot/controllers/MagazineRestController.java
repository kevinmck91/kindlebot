package com.kevinmck91.kindlebot.controllers;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.kevinmck91.kindlebot.dto.GithubItem;
import com.kevinmck91.kindlebot.dto.request.MagazineRequest;
import com.kevinmck91.kindlebot.dto.response.MagazineResponse;
import com.kevinmck91.kindlebot.service.MagazineService;
import com.kevinmck91.kindlebot.utils.MagazineUtils;

@RestController
@RequestMapping("/api/magazine")
public class MagazineRestController {

	@Autowired
	private MagazineService magazineService;

	private static final Logger logger = LoggerFactory.getLogger(MagazineRestController.class);

	// Constructor
	// The constructor is needed because of how Spring Boot does Dependency
	// Injection (DI).
	// To provide (or "inject") the HighlightsService into your controller, Spring
	// needs a constructor.
	public MagazineRestController(MagazineService magazineService) {
		this.magazineService = magazineService;
	}

	@GetMapping("")
	public List<MagazineResponse> fetchAllMagazines() throws IOException {

		logger.info(" - ");

		List<MagazineResponse> magazineResponseList = new ArrayList<MagazineResponse>();
		
		magazineResponseList.add(new MagazineResponse("The New Yorker"));
		magazineResponseList.add(new MagazineResponse("The Economist"));
		magazineResponseList.add(new MagazineResponse("Time Magazine"));
																																																																																																						
		return magazineResponseList;
	}

	@PostMapping("/editions")
	public List<MagazineResponse> fetchMagazineEditions(@RequestBody MagazineRequest request) throws IOException {

		logger.info(" - ");

		GithubItem[] githubItems = magazineService.fetchAllEditionsData(request);

		List<MagazineResponse> magazineResponseList = MagazineUtils.listAvailableMagazines(request, githubItems);

		// magazineService.createFolderStructure(editionOverviewList);

		return magazineResponseList;
	}

	@PostMapping("/edition")
	public List<MagazineResponse> fetchIndividualEdition(@RequestBody MagazineRequest request) throws Exception {

		logger.info(" - ");

		GithubItem[] githubItems = magazineService.fetchIndividualEditionData(request);

		List<MagazineResponse> magazineResponseList = MagazineUtils.listMagazineFormats(request, githubItems);

		return magazineResponseList;

	}

	@PostMapping("/edition/pdf")
	public ResponseEntity<Resource> servePdf(@RequestBody MagazineRequest request) throws Exception {

		logger.info(" - ");

		/*
		 * Path pdfPath = magazineService.getPdfPath(request);
		 * 
		 * Resource resource = new FileSystemResource(pdfPath.toFile());
		 * 
		 * return ResponseEntity.ok().header(HttpHeaders.CONTENT_DISPOSITION,
		 * "inline; filename=\"" + request.getEditionName() +
		 * ".pdf\"").contentType(MediaType.APPLICATION_PDF).body(resource);
		 */
		
		return null;
	}

	@PostMapping("/edition/epub")
	public ResponseEntity<Resource> serveEpub(@RequestBody MagazineRequest request) throws Exception {

		logger.info(" - ");
		/*
		 * Path epubPath = magazineService.getEpubPath(request);
		 * 
		 * Resource resource = new FileSystemResource(epubPath.toFile());
		 * 
		 * return ResponseEntity.ok().header(HttpHeaders.CONTENT_DISPOSITION,
		 * "inline; filename=\"" + request.getEditionName() +
		 * ".epub\"").contentType(MediaType.parseMediaType("application/epub+zip")).body
		 * (resource);
		 */
		return null;
		
	}

	@PostMapping("/edition/jpg")
	public ResponseEntity<Resource> serveJpg(@RequestBody MagazineRequest request) throws Exception {

		logger.info(" - ");

		/*
		 * Path jpgPath = magazineService.getJpgPath(request);
		 * 
		 * Resource resource = new FileSystemResource(jpgPath.toFile());
		 * 
		 * return ResponseEntity.ok().header(HttpHeaders.CONTENT_DISPOSITION,
		 * "inline; filename=\"" + resource.getFilename() +
		 * "\"").contentType(MediaType.IMAGE_JPEG).body(resource);
		 */
		
		return null;
	}

}