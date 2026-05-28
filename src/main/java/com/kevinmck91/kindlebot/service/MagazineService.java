package com.kevinmck91.kindlebot.service;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import com.kevinmck91.kindlebot.dto.GithubItem;
import com.kevinmck91.kindlebot.dto.request.MagazineRequest;
import com.kevinmck91.kindlebot.dto.response.MagazineResponse;
import com.kevinmck91.kindlebot.exceptions.EndpointException;
import com.kevinmck91.kindlebot.utils.GithubDownloader;
import com.kevinmck91.kindlebot.utils.MagazineUtils;

@Service
public class MagazineService {

	private static final Logger logger = LoggerFactory.getLogger(MagazineService.class);

	private final GithubDownloader githubDownloader;
	private final MagazineService self;

	// Constructor
	public MagazineService(GithubDownloader githubDownloader, @Lazy MagazineService self) {
		this.githubDownloader = githubDownloader;
		this.self = self;
	}

	/**
	 * Calls the Github API to get the JSON of all the available magazine Editions.
	 * 
	 * Turns all the GitHubItem response array into a map
	 *
	 * @return GithubItem[] of each available Edition
	 * @throws IOException
	 */
	public GithubItem[] fetchAllEditionsData(MagazineRequest request) throws IOException {

		MagazineUtils.validateMagazineRequest(request);

		logger.info("MagazineService.fetchAllEditionsData() - Fetching Available Editions JSON from API");

		// Call the endpoint to get the set of available Editions
		GithubItem[] availableItems = githubDownloader.getAvailableEditions(request.getMagazineName(), request.getYear());

		return availableItems;

	}

	/**
	 * Calls the Github API to get the JSON of a single New Yorker Edition.
	 * 
	 * @param request - type InputEndpointRequest
	 * @return GithubItem[] - All the available formats for that single Edition
	 * @throws IOException
	 */
	public GithubItem[] fetchIndividualEditionData(MagazineRequest request) throws IOException {

		MagazineUtils.validateMagazineRequest(request);

		logger.info("MagazineService.fetchIndividualEditionData() - Fetching Individual Data Edition from API. Edition name = [" + request.getEditionName() + "]");

		// GithubItem[] gitHubItem =
		// githubDownloader.getIndividualEdition(request.getEndpoint());

		return null;

	}
	
	/**
	 * Generate a folder for each editionOverviewList item.
	 * 
	 * @param List<EditionOverview>
	 * @throws IOException
	 */
	public void createFolderStructure(List<MagazineResponse> editionOverviewList) throws IOException {

		// Base downloads folder
		File baseFolder = new File("src/main/resources/static/downloads");
		if (!baseFolder.exists()) {
			baseFolder.mkdirs();
		}

		int foldersCreated = 0;

		for (MagazineResponse item : editionOverviewList) {

			// Create a folder for each available edition
			File dateFolder = new File(baseFolder, item.getEditionName());

			// If the folder does not exists, create it.
			if (!dateFolder.exists()) {
				dateFolder.mkdirs();
				foldersCreated++;
			}
		}

		logger.info("MagazineService.createFolderStructure() - Creating folder structure for Editions. Folders created = [" + foldersCreated + "]");

	}

	public List<GithubItem> fetchData(String endpoint) throws IOException {

		// Call the endpoint to get the set of available Editions
		GithubItem[] githubItems = githubDownloader.fetchData(endpoint);

		List<GithubItem> githubItemsList = new ArrayList<GithubItem>();

		for (GithubItem githubItem : githubItems) {
			githubItemsList.add(githubItem);
		}

		return githubItemsList;
	}

	public List<GithubItem> fetchAllMagazines() {
		
		List<GithubItem> githubItems = new ArrayList<GithubItem>();
		
		GithubItem githubItem1 = new GithubItem();
		githubItem1.setName("NY");
		githubItem1.setUrl("https://api.github.com/repositories/769113642/contents/NY");
		githubItem1.setDescription("The New Yorker");

		GithubItem githubItem2 = new GithubItem();
		githubItem2.setName("TE");
		githubItem2.setUrl("https://api.github.com/repositories/737724859/contents/TE");
		githubItem2.setDescription("The Economist");

		GithubItem githubItem3 = new GithubItem();
		githubItem3.setName("TM");
		githubItem3.setUrl("https://api.github.com/repositories/769109109/contents/TM");
		githubItem3.setDescription("Time Magazine");

		githubItems.add(githubItem1);
		githubItems.add(githubItem2);
		githubItems.add(githubItem3);
		
		return githubItems;
	}

}
