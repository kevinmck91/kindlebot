package com.kevinmck91.kindlebot.utils;

import java.io.File;
import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kevinmck91.kindlebot.dto.GithubItem;

@Service
public class GithubDownloader {

	private static final Logger logger = LoggerFactory.getLogger(GithubDownloader.class);

	private final RestTemplate restTemplate = new RestTemplate();

	private final String mockAvailableEditionsEndpoint = "src/main/resources/mocks/mock-available-editions-response.json";
	private final String mockIndividualEditionEndpoint = "src/main/resources/mocks/mock-individual-edition-response.json";

	@Value("${spring.api.theNewYorker}")
	private String NEW_YORKER_ENDPOINT;
	
	@Value("${spring.api.theEconomist}")
	private String ECONOMIST_ENDPOINT;
	
	@Value("${spring.api.scientificAmerican}")
	private String SCIENTIFIC_AMERICAN_ENDPOINT;
	
	@Value("${spring.api.timeMagazine}")
	private String TIME_MAGAZINE_ENDPOINT;

	public GithubItem[] getAvailableEditions(String magazineName, String year) throws IOException {

		String endpoint = "";

		//TODO: Get the endpoints from somehwere else - property files 
		if (magazineName.equals("theNewYorker"))
			endpoint = NEW_YORKER_ENDPOINT + year;
		if (magazineName.equals("theEconomist"))
			endpoint = ECONOMIST_ENDPOINT + year;
		if (magazineName.equals("scientificAmerican"))
			endpoint = SCIENTIFIC_AMERICAN_ENDPOINT + year;
		if (magazineName.equals("timeMagazine"))
			endpoint = TIME_MAGAZINE_ENDPOINT + year;

		GithubItem[] response;

		logger.info("GithubDownloader.getAvailableEditions() - Endpoint = " + endpoint);

		ResponseEntity<GithubItem[]> responseEntity = restTemplate.getForEntity(endpoint, GithubItem[].class);

		response = responseEntity.getBody();

		logger.info("GithubDownloader.getAvailableEditions() - Success calling endpoint");

		return response;
	}

	public GithubItem[] getIndividualEdition(String endpoint) throws IOException {

		GithubItem[] response;

		logger.info("GithubDownloader.getIndividualEdition() - Endpoint =  [" + endpoint + "]");

		ResponseEntity<GithubItem[]> responseEntity = restTemplate.getForEntity(endpoint, GithubItem[].class);
		response = responseEntity.getBody();

		logger.info("GithubDownloader.getIndividualEdition() - Success calling endpoint");

		return response;

	}

}