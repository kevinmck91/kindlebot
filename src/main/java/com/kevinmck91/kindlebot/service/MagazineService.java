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

		GithubItem[] gitHubItem = githubDownloader.getIndividualEdition(request.getEndpoint());

		return gitHubItem;

	}

	/**
	 * Calls the Github API Endpoint, download the PDF, return the PATH of the
	 * downloaded PDF.
	 * 
	 * @param MagazineRequest
	 * @return Path
	 * @throws IOException
	 */
	public Path getPdfPath(MagazineRequest request) throws IOException, EndpointException {

		MagazineUtils.validateMagazineRequest(request);
		
		String endpoint = request.getEndpoint();
		String editionName = request.getEditionName();
		String magazineName = request.getMagazineName();
		
		String beautifulFilename = magazineName + "_" + editionName;
		
		Path folderPath = Paths.get("src/main/resources/static/downloads/" + editionName);
		Path pdfPath = Paths.get("src/main/resources/static/downloads/" + editionName + "/" + beautifulFilename + ".pdf");

		Boolean folderExists = Files.exists(folderPath);
		Boolean pdfExists = Files.exists(pdfPath);

		if (!endpoint.contains("pdf"))
			throw new EndpointException("Attempting to call PDF endpoint with invalid URL");

		// Create the folder, if it does not exist
		if (!folderExists) {
			logger.info("MagazineService.getPdfPath() - Creating new folder = [" + folderPath + "]");
			Files.createDirectories(folderPath);
		}

		// If it hasnt been downloaded before, check the file and download it
		if (!pdfExists) {

			logger.info("MagazineService.getPdfPath() - Pdf file does not exist. Downloading from = [" + endpoint + "]");

			// TODO: move the validation to the Utils class
			Path tempFile = Files.createTempFile("downloaded", ".pdf");

			Boolean validFile = MagazineUtils.isFileValid(tempFile.toFile(), "pdf");

			// check the PDF comes from a trusted source
			if (!endpoint.startsWith("https://raw.githubusercontent.com/Monkfishare/")) {
				logger.warn("MagazineService.getPdfPath() - Untrusted URL endpoint");
				throw new SecurityException("Untrusted URL endpoint");
			}

			try (InputStream in = new URL(endpoint).openStream()) {
				Files.copy(in, tempFile, StandardCopyOption.REPLACE_EXISTING);
			}

			try (RandomAccessFile raf = new RandomAccessFile(tempFile.toFile(), "r")) {
				long length = raf.length();
				int checkLength = (int) Math.min(1024, length); // last 1KB
				raf.seek(length - checkLength);
				byte[] tail = new byte[checkLength];
				raf.readFully(tail);

				String tailStr = new String(tail, StandardCharsets.US_ASCII);
				if (!tailStr.contains("%%EOF")) {
					logger.warn("MagazineService.getPdfPath() - Missing EOF marker, file may be truncated");
					throw new SecurityException("Missing EOF marker, file may be truncated");
				}
			}

			// Save to project folder
			Path targetDir = Paths.get("src/main/resources/static/downloads", editionName);
			Files.createDirectories(targetDir); // Make sure directory exists

			Path targetFile = targetDir.resolve(beautifulFilename + ".pdf"); 
			Files.move(tempFile, targetFile, StandardCopyOption.REPLACE_EXISTING);

			logger.info("MagazineService.getPdfPath() - PDF Downloaded successfully");

			return targetFile;
		}

		logger.info("MagazineService.getPdfPath() - PDF location Path = [" + pdfPath + "]");
		logger.info("MagazineService.getPdfPath() - serving PDF");

		return pdfPath;

	}

	/**
	 * Calls the Github API Endpoint, download the EPUB, return the PATH of the
	 * downloaded EPUB.
	 * 
	 * @param MagazineRequest
	 * @return Path
	 * @throws IOException
	 * @throws EndpointException
	 */
	public Path getEpubPath(MagazineRequest request) throws IOException, EndpointException {

		String endpoint = request.getEndpoint();
		String editionName = request.getEditionName();
		String magazineName = request.getMagazineName();
		
		String beautifulFilename = magazineName + "_" + editionName;
		
		Path folderPath = Paths.get("src/main/resources/static/downloads/" + editionName);
		Path epubPath = Paths.get("src/main/resources/static/downloads/" + editionName + "/" + beautifulFilename + ".epub");

		Boolean folderExists = Files.exists(folderPath);
		Boolean epubExists = Files.exists(epubPath);

		if (!endpoint.contains("epub"))
			throw new EndpointException("Attempting to call EPUB endpoint with invalid URL");

		// Create the folder, if it does not exist
		if (!folderExists) {
			logger.info("MagazineService.getEpubPath() - Creating new folder = [" + folderPath + "]");
			Files.createDirectories(folderPath);
		}
		// If it hasnt been downloaded before, check the file and download it
		if (!epubExists) {

			logger.info("MagazineService.getEpubPath() - EPub file does not exist. Downloading from = [" + endpoint + "]");

			// Download file
			try (InputStream in = new URL(endpoint).openStream()) {
				// TODO: Check the EPUB is valid before downloading
				Files.copy(in, epubPath, StandardCopyOption.REPLACE_EXISTING);
			}

		}

		logger.info("MagazineService.getEpubPath() - Serving EPUB Path = [" + epubPath + "]");

		return epubPath;
	}

	/**
	 * Calls the Github API Endpoint, download the JPG, return the PATH of the
	 * downloaded JPG.
	 * 
	 * @param MagazineRequest
	 * @return Path
	 * @throws IOException
	 * @throws EndpointException
	 */
	public Path getJpgPath(MagazineRequest request) throws IOException, EndpointException {

		String endpoint = request.getEndpoint();
		String name = request.getEditionName();

		Path folderPath = Paths.get("src/main/resources/static/downloads/" + name);
		Path jpgPath = Paths.get("src/main/resources/static/downloads/" + name + "/" + name + ".jpg");

		Boolean folderExists = Files.exists(folderPath);
		Boolean jpgExists = Files.exists(jpgPath);

		if (!endpoint.contains("jpg"))
			throw new EndpointException("Attempting to call JPG endpoint with invalid URL");

		// Create the folder, if it does not exist
		if (!folderExists) {
			logger.info("MagazineService.getJpgPath() - Creating new folder = [" + folderPath + "]");
			Files.createDirectories(folderPath);
		}
		// If it hasnt been downloaded before, check the file and download it
		if (!jpgExists) {

			logger.info("MagazineService.getJpgPath() - Jpg file does not exist. Downloading from = [" + endpoint + "]");

			// Download file
			try (InputStream in = new URL(endpoint).openStream()) {
				// TODO: Check the JPG is valid before downloading
				Files.copy(in, jpgPath, StandardCopyOption.REPLACE_EXISTING);
			}

		}

		logger.info("MagazineService.getJpgPath() - Serving JPG Path = [" + jpgPath + "]");

		return jpgPath;
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

}
