package com.kevinmck91.kindlebot.utils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.kevinmck91.kindlebot.dto.GithubItem;
import com.kevinmck91.kindlebot.dto.request.MagazineRequest;
import com.kevinmck91.kindlebot.dto.response.MagazineResponse;
import com.kevinmck91.kindlebot.exceptions.InvalidMagazineRequestException;

public class MagazineUtils {

	private static final Logger logger = LoggerFactory.getLogger(MagazineUtils.class);

	/**
	 * Change the input date yyyy-MM-dd into a readable date
	 * 
	 * @param input - format : yyyy-MM-dd
	 * @return String - Formatted date : dd MMM, yyyy
	 */
	public static String formatDisplayName(String input) {

		// Define the input and output format
		DateTimeFormatter inputFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
		DateTimeFormatter outputFormatter = DateTimeFormatter.ofPattern("dd MMM, yyyy");

		// Parse the input string to a LocalDate
		LocalDate date = LocalDate.parse(input, inputFormatter);

		// Format the date to the desired output
		String formattedDate = date.format(outputFormatter);

		return formattedDate;
	}

	public static Boolean isFileValid(File file, String extension) {

		boolean valid = true;

		if (extension.equals("epub")) {

			// Check ZIP structure and required files
			try (ZipFile zipFile = new ZipFile(file)) {

				ZipEntry mimetypeEntry = zipFile.getEntry("mimetype");
				if (mimetypeEntry == null) {
					throw new SecurityException("Missing 'mimetype' file in EPUB");
				}

				// 'mimetype' must be uncompressed and first entry in ZIP
				if (mimetypeEntry.getMethod() != ZipEntry.STORED) {
					return false;
					// throw new SecurityException("'mimetype' must be stored (not compressed)");
				}

				// Contents of 'mimetype' must be exactly "application/epub+zip"
				try (InputStream is = zipFile.getInputStream(mimetypeEntry)) {

					byte[] mimetypeBytes = is.readAllBytes();

					String mimetype = new String(mimetypeBytes, StandardCharsets.US_ASCII).trim();

					if (!"application/epub+zip".equals(mimetype)) {
						return false;
						// throw new SecurityException("Invalid mimetype content in EPUB: " + mimetype);
					}
				}

				// Check for required container.xml
				ZipEntry containerEntry = zipFile.getEntry("META-INF/container.xml");

				// TODO: clean up the exception handling
				if (containerEntry == null) {
					return false;
					// throw new SecurityException("Missing container.xml in EPUB (not a valid
					// EPUB)");

				}

			} catch (IOException e) {
				// TODO: clean up the exception handling
				return false;
				// throw new SecurityException("Failed to read EPUB file", e);
			}

		}

		if (extension.equals("PDF")) {

		}
		return true;

	}

	public static boolean isBookmark(String string) {

		String[] lines = string.split("\n");

		if (lines.length > 1 && lines[1].contains("Your Bookmark"))
			return true;

		return false;
	}

	public static String formatDate(String dateString) {
		// Define formatter for input string
		DateTimeFormatter inputFormatter = DateTimeFormatter.ofPattern("EEEE, d MMMM yyyy HH:mm:ss", Locale.ENGLISH);

		// Parse input string to LocalDateTime
		LocalDateTime dateTime = LocalDateTime.parse(dateString, inputFormatter);

		// Define output formatter for desired pattern: YYYYmmddHHmmss
		DateTimeFormatter outputFormatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

		// Format the LocalDateTime to output string
		return dateTime.format(outputFormatter);
	}

	/**
	 * Transfer a set of AvailbaleEdition githubItems into an array of
	 * EditionOverview objects
	 * 
	 * @param name         - The Name of the Edition
	 * @param githubItem[] - The array of githubItems (3 or 4) one for each
	 *                     available download
	 * @return EditionDetail - A set of download endpoints for PDF, Epub, Jpg etc.
	 */
	public static List<MagazineResponse> listAvailableMagazines(MagazineRequest request, GithubItem[] availableItems) {

		List<MagazineResponse> magazineResponseList = new ArrayList<>();

		// Create the TreeMap of available edition
		for (GithubItem githubItem : availableItems) {

			MagazineResponse magazineResponse = new MagazineResponse();

			magazineResponse.setEditionName(githubItem.getName());
			magazineResponse.setDisplayName(MagazineUtils.formatDisplayName(githubItem.getName()));
			magazineResponse.setEndpoint(githubItem.getUrl());
			magazineResponse.setYear(request.getYear());
			magazineResponse.setMagazineName(request.getMagazineName());

			magazineResponseList.add(magazineResponse);
		}

		// Sort the list
		magazineResponseList.sort(Comparator.comparing(MagazineResponse::getEditionName).reversed());

		return magazineResponseList;
	}

	/**
	 * Transfer a set of Edition githubItems into and EditionDetail object
	 * 
	 * @param name         - The Name of the Edition
	 * @param githubItem[] - The array of githubItems (3 or 4) one for each
	 *                     available download
	 * @return EditionDetail - A set of download endpoints for PDF, Epub, Jpg etc.
	 */
	public static List<MagazineResponse> listMagazineFormats(MagazineRequest request, GithubItem[] githubItem) {

		List<MagazineResponse> magazineResponseList = new ArrayList<MagazineResponse>();

		for (GithubItem item : githubItem) {

			if (item.getName().endsWith(".epub")) {
				magazineResponseList.add(new MagazineResponse(request.getEditionName(), item.getDownload_url(), request.getMagazineName(), request.getYear(), ""));
			} else if (item.getName().endsWith(".pdf")) {
				magazineResponseList.add(new MagazineResponse(request.getEditionName(), item.getDownload_url(), request.getMagazineName(), request.getYear(), ""));
			} else if (item.getName().endsWith(".jpg")) {
				magazineResponseList.add(new MagazineResponse(request.getEditionName(), item.getDownload_url(), request.getMagazineName(), request.getYear(), ""));
			}

		}

		return magazineResponseList;
	}

	public static void validateMagazineRequest(MagazineRequest request) {

		logger.info("MagazineUtils.validateMagazineRequest() - Validating the input MagazineRequest JSON");

		// TODO: Get these from an ENUM
		ArrayList<String> validMagazines = new ArrayList<String>();
		validMagazines.add("theEconomist");
		validMagazines.add("theNewYorker");
		validMagazines.add("scientificAmerican");
		validMagazines.add("timeMagazine");

		// Validate year if provided
		if (request.getYear() != null && !request.getYear().isBlank()) {
			int year = Integer.parseInt(request.getYear());
			if (year < 2025 || year > 2028) {
				logger.error("MagazineUtils.validateMagazineRequest() - Invalid input year = [" + request.getYear() + "]");
				throw new InvalidMagazineRequestException("Year must be between 2025 and 2028");
			}
		}

		// Validate magazineName if provided
		if (request.getMagazineName() != null && !request.getMagazineName().isBlank()) {
			if (!validMagazines.contains(request.getMagazineName())) {
				logger.error("MagazineUtils.validateMagazineRequest() - invalid Magazine Name = [" + request.getMagazineName() + "]");
				throw new InvalidMagazineRequestException("Invalid magazine name: " + request.getMagazineName());
			}
		}

		// Validate editionName format if provided
		if (request.getEditionName() != null && !request.getEditionName().isBlank()) {
			try {
				DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
				LocalDate.parse(request.getEditionName(), formatter);
			} catch (DateTimeParseException e) {
				logger.error("MagazineUtils.validateMagazineRequest() - invalid editionName format = [" + request.getEditionName() + "]");
				throw new InvalidMagazineRequestException("editionName must have format YYYY-MM-DD");
			}
		}

		// Validate endpoint if provided
		if (request.getEndpoint() != null && !request.getEndpoint().isBlank()) {
			String requiredPrefix1 = "https://api.github.com/repos/Monkfishare";
			String requiredPrefix2 = "https://raw.githubusercontent.com/Monkfishare";
			if (!request.getEndpoint().startsWith(requiredPrefix1) && !request.getEndpoint().startsWith(requiredPrefix2)) {
				logger.error("MagazineUtils.validateMagazineRequest() - invalid endpoint = [" + request.getEndpoint() + "]");
				throw new InvalidMagazineRequestException("endpoint must begin with [" + requiredPrefix1 + "] or [" + requiredPrefix2 + "]");
			}
			if (!request.getEndpoint().contains(request.getEditionName())) {
				logger.error("MagazineUtils.validateMagazineRequest() - endpoint/editionName mismatch = [" + request.getEndpoint() + "] - [" + request.getEditionName() + "]");
				throw new InvalidMagazineRequestException("endpoint/editionName mismatch = [" + request.getEndpoint() + "] - [" + request.getEditionName() + "]");
			}
		}

		logger.info("MagazineUtils.validateMagazineRequest() - Validation successful");

	}

}
