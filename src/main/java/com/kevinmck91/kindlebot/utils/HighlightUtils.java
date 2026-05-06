package com.kevinmck91.kindlebot.utils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.kevinmck91.kindlebot.dto.Highlight;
import com.kevinmck91.kindlebot.dto.request.HighlightRequest;
import com.kevinmck91.kindlebot.service.HighlightService;

/**
 * 
 */
public class HighlightUtils {

	private static final Logger logger = LoggerFactory.getLogger(HighlightService.class);

	/**
	 * Parses a single Kindle clipping entry from a text block and converts it into
	 * a {@link Highlight} object.
	 *
	 * <p>
	 * The input text is expected to follow the standard Kindle clipping format:
	 * <ul>
	 * <li>Line 1: Book title and optionally the author in parentheses</li>
	 * <li>Line 2: Metadata line containing page, location, and added date</li>
	 * <li>Line 3: (usually empty)</li>
	 * <li>Line 4+: The actual highlighted text</li>
	 * </ul>
	 *
	 * <p>
	 * If the format is invalid or the input is too short, an
	 * {@link IllegalArgumentException} is thrown.
	 * </p>
	 *
	 * @param text       - the raw clipping text block from the Kindle clippings
	 *                   file
	 * @param lineNumber
	 * @return a {@code Highlight} object populated with parsed title, author,
	 *         metadata, and excerpt
	 * @throws IllegalArgumentException if the input format is invalid or too short
	 */
	public Highlight parseHighlightSegment(String text, int lineNumber) {

		Highlight highlight = new Highlight();

		// Split input text into lines
		String[] lines = text.split("\\r?\\n");

		try {

			if (lines.length < 3)
				throw new Exception();

			// 1. Parse header line: contains title and optionally author
			String headerLine = lines[0].trim();
			highlight.setHighlightHeader(headerLine);

			int openBracket = headerLine.indexOf('(');
			if (openBracket != -1) {
				// Title is before '('
				String title = headerLine.substring(0, openBracket).trim();
				highlight.setTitle(title);

				// Author is inside parentheses
				int closeBracket = headerLine.indexOf(')', openBracket);
				if (closeBracket != -1) {
					String author = headerLine.substring(openBracket + 1, closeBracket).trim();
					highlight.setAuthor(author);
				}
			} else {
				// If no author bracket found, whole header is the title
				highlight.setTitle(headerLine);
			}

			// 2. Parse metadata line: page, location, date
			String metadataLine = lines[1].trim();

			// Extract page number
			String pagePrefix = "page ";
			int pagePos = metadataLine.indexOf(pagePrefix);
			if (pagePos != -1) {
				int pageStart = pagePos + pagePrefix.length();
				int pageEnd = metadataLine.indexOf(' ', pageStart);
				if (pageEnd == -1)
					pageEnd = metadataLine.indexOf('|', pageStart);
				if (pageEnd == -1)
					pageEnd = metadataLine.length();

				String page = metadataLine.substring(pageStart, pageEnd).trim();
				highlight.setPage(page);
			}

			// Extract location
			String locationPrefix = "Location ";
			int locPos = metadataLine.indexOf(locationPrefix);
			if (locPos != -1) {
				int locStart = locPos + locationPrefix.length();
				int locEnd = metadataLine.indexOf('|', locStart);
				if (locEnd == -1)
					locEnd = metadataLine.length();

				String location = metadataLine.substring(locStart, locEnd).trim();
				highlight.setLocation(location);
			}

			// Extract added date
			String addedPrefix = "Added on ";
			int addedPos = metadataLine.indexOf(addedPrefix);
			if (addedPos != -1) {
				String dateString = metadataLine.substring(addedPos + addedPrefix.length()).trim();
				highlight.setDateString(dateString);
				highlight.setTimestampHash(MagazineUtils.formatDate(dateString));
			} else {
				throw new Exception("Timestamp not found");
			}

			// 3. Extract excerpt text: lines after the empty line (starting at line 3)
			StringBuilder excerptBuilder = new StringBuilder();
			for (int i = 3; i < lines.length; i++) {
				excerptBuilder.append(lines[i]);
				if (i < lines.length - 1) {
					excerptBuilder.append("\n");
				}
			}

			highlight.setExcerpt(excerptBuilder.toString().trim());

			// Set the other default
			highlight.setSource("");
			highlight.setHighlightTagsString("");	//Tags are added later, not part of clipping.txt upload
			highlight.setVisibility("shown");

			return highlight;

		} catch (Exception e) {

			// Catch all exception, log them, then continue on to the next segment
			logger.error("HighlightUtils.parseHighlightSegment() - Error parsing the Segment ending line : [" + lineNumber + "] - [" + e.getMessage() + "]");
			return null;

		}

	}

	/**
	 * Map an input HighlightRequest object to a Highlight object
	 * 
	 * @param highlightRequest
	 * @return Highlight
	 */
	public Highlight getHighlightFromRequest(HighlightRequest highlightRequest) {

		Highlight highlight = new Highlight();

		LocalDateTime now = LocalDateTime.now();
		DateTimeFormatter formatterReadable = DateTimeFormatter.ofPattern("EEEE, dd MMMM yyyy HH:mm:ss");
		DateTimeFormatter formatterHash = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

		// Set inputs from the request
		highlight.setAuthor(highlightRequest.getAuthor());
		highlight.setExcerpt(highlightRequest.getExcerpt());
		highlight.setTitle(highlightRequest.getTitle());
		highlight.setNote(highlightRequest.getNote());
		highlight.setHighlightTags(highlightRequest.getHighlightTags());
		highlight.setSource(highlightRequest.getSource());
		
		// Set other variable for the highlight
		highlight.setVisibility("shown");
		highlight.setHighlightHeader(highlightRequest.getTitle() + " (" + highlightRequest.getAuthor() + ")");
		highlight.setLocation("");
		highlight.setDateString(now.format(formatterReadable));
		highlight.setTimestampHash(now.format(formatterHash));

		// Set Custom/edited variables in the for the highlight
		if (highlightRequest.getPage().equals(null) || highlightRequest.getPage().equals(""))
			highlight.setPage("");
		else
			highlight.setPage(highlightRequest.getPage());

		// HighlightTags & HighlightTagsString are the same, and are kept in sync
		String tagString = "";
		List<String> tags = new ArrayList<>(highlightRequest.getHighlightTags());
		Collections.sort(tags);

		for (String tag : tags) {
		    tagString += tag + ", ";
		}
		
		highlight.setHighlightTagsString(tagString);
		
		return highlight;

	}

}
