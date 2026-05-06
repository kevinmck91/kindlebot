package com.kevinmck91.kindlebot.service;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutionException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.CollectionReference;
import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.FieldValue;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.QueryDocumentSnapshot;
import com.google.cloud.firestore.QuerySnapshot;
import com.google.cloud.firestore.WriteBatch;
import com.google.cloud.firestore.WriteResult;
import com.google.firebase.cloud.FirestoreClient;
import com.kevinmck91.kindlebot.dto.Highlight;
import com.kevinmck91.kindlebot.dto.request.BulkUpdateBooleanRequest;
import com.kevinmck91.kindlebot.dto.request.BulkUpdateStringRequest;
import com.kevinmck91.kindlebot.dto.request.HighlightRequest;
import com.kevinmck91.kindlebot.dto.request.InputStringRequest;
import com.kevinmck91.kindlebot.dto.response.FileUploadResponse;
import com.kevinmck91.kindlebot.exceptions.HighlightNotFoundException;
import com.kevinmck91.kindlebot.exceptions.ParseUploadFileException;
import com.kevinmck91.kindlebot.utils.HighlightUtils;

@Service
public class HighlightService {

	private final String not_found = "not_found";

	private static final Logger logger = LoggerFactory.getLogger(HighlightService.class);

	HighlightUtils highlightUtils = new HighlightUtils();

	/**
	 * Retrieves all highlights from the database.
	 *
	 * <p>
	 * Logs the operation and returns a list containing all stored {@link Highlight}
	 * entities.
	 * </p>
	 *
	 * @return a {@code List} of all {@link Highlight} objects in the database;
	 *         returns an empty list if none are found
	 */
	public List<Highlight> getAllHighlights() {
		
		//TODO: Validate each input before writing to DB
		
		logger.info("[HighlightService.getAllHighlights] - [Attempting getting all highlights from database]");

		Firestore dbFireStore = FirestoreClient.getFirestore();

		try {
			ApiFuture<QuerySnapshot> future = dbFireStore.collection("highlights").get();

			QuerySnapshot querySnapshot = future.get();

			List<Highlight> highlights = new ArrayList<>();

			int count = 0;

			for (DocumentSnapshot document : querySnapshot.getDocuments()) {

				Highlight highlight = document.toObject(Highlight.class);

				highlights.add(highlight);
				count++;
				
				if (count == 27) {
					//break;
				}
			}

			return highlights;

		} catch (Exception e) {
			throw new RuntimeException("Failed to fetch highlights", e);
		}
	}

	/**
	 * Retrieves Unique highlight from the database.
	 *
	 * <p>
	 * Logs the operation and returns a list containing one document.
	 * </p>
	 *
	 * @return a {@code List} of all {@link Highlight} objects in the database;
	 *         returns an empty list if none are found
	 */
	public List<Highlight> getUniqueHighlight(String timestampHash) {

		logger.info("[HighlightService.getUniqueHighlight] - Attempting to get unique highlight from database. timestampHash = [{}]", timestampHash);

		List<Highlight> highlights = new ArrayList<>();

		try {
			Firestore db = FirestoreClient.getFirestore();
			DocumentReference docRef = db.collection("highlights").document(timestampHash);

			// Get the document snapshot
			DocumentSnapshot snapshot = docRef.get().get(); // Call get() on ApiFuture

			if (!snapshot.exists()) {
				throw new HighlightNotFoundException("Highlight not found with timestampHash: [" + timestampHash + "]");
			}

			// Convert to Highlight object
			highlights.add(snapshot.toObject(Highlight.class));

		} catch (Exception e) {
			throw new RuntimeException("Failed to fetch highlight from Firestore", e);
		}

		return highlights;
	}

	/**
	 * Parses a Kindle clippings file from the uploaded {@link MultipartFile} and
	 * saves new highlights to the database.
	 *
	 * <p>
	 * The method reads the file line-by-line, splitting the content into individual
	 * clippings separated by "==========" lines. It ignores bookmarks and only
	 * processes highlight entries. Duplicate highlights (based on timestamp hash)
	 * are skipped to prevent redundant database entries.
	 * </p>
	 *
	 * <p>
	 * The method returns an {@link FileUploadResponse} object containing counts of
	 * highlights added, highlights skipped, and bookmarks skipped during
	 * processing.
	 * </p>
	 *
	 * @param file the uploaded file containing Kindle clippings
	 * @return an {@code UploadResults} object summarizing the outcome of the
	 *         parsing operation
	 * @throws Exception
	 */
	public FileUploadResponse parseInputFile(MultipartFile file) throws Exception {

		logger.info("HighlightService.parseInputFile() - Start parsing the input file.");

		if (file.isEmpty()) {
			logger.info("HighlightService.parseInputFile() - Input File is Empty.");
			throw new ParseUploadFileException("Input file is empty");
		}

		InputStream inputStream = file.getInputStream();

		StringBuilder segmentBuilder = new StringBuilder();
		ArrayList<Highlight> highlightArray = new ArrayList<>();
		String line;

		int segmentsProcessed = 0;
		int newHighlightsAdded = 0;
		int errorHighlightsSkipped = 0;
		int duplicateHighlightsSkipped = 0;
		int nonHighlightsSkipped = 0;
		int lineNumber = 0;

		// Read the file line by line
		try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {

			while ((line = reader.readLine()) != null) {

				lineNumber++;

				if (line.equals("==========")) {

					// A Segment / SegmentBuilder is complete (now extract meta data and save it)

					segmentsProcessed++;

					Boolean isHighlight = segmentBuilder.toString().contains("Highlight");

					// If its a highlight, extract the meta data
					if (isHighlight) {

						Highlight highlight = highlightUtils.parseHighlightSegment(segmentBuilder.toString(), lineNumber);

						// If the highlight is invalid format, log it, move onto next segment
						if (highlight == null) {
							errorHighlightsSkipped++;
							segmentBuilder.setLength(0); // Reset builder
							continue;

						} else {

							highlightArray.add(highlight);

						}

						Firestore db = FirestoreClient.getFirestore();
						DocumentReference docRef = db.collection("highlights").document(highlight.getTimestampHash());

						try {
							ApiFuture<DocumentSnapshot> future = docRef.get();
							DocumentSnapshot document = future.get();

							//TODO: This is not threadsafe
							
							if (!document.exists()) {
								// Document ID doesn't exist → add new highlight
								docRef.set(highlight);
								newHighlightsAdded++;
								logger.info("HighlightService.parseInputFile() - New highlight added to Firestore. timestampHash = [{}]", highlight.getTimestampHash());
							} else {
								// Duplicate
								duplicateHighlightsSkipped++;
							}
						} catch (Exception e) {
							logger.error("Error adding highlight to Firestore", e);
						}

						// not a Highlight, skipping segment
					} else {
						nonHighlightsSkipped++;
					}

					segmentBuilder.setLength(0); // Reset builder

				} else {
					segmentBuilder.append(line).append("\n");
				}

			}

		}

		catch (Exception e) {
			throw new ParseUploadFileException("Error parsing the file on segment ending line : [" + lineNumber + "] - " + e.getMessage());
		}

		FileUploadResponse fileUploadResponse = new FileUploadResponse();

		fileUploadResponse.setDuplicateHighlightsSkipped(duplicateHighlightsSkipped);
		fileUploadResponse.setErrorHighlightsSkipped(errorHighlightsSkipped);
		fileUploadResponse.setNewHighlightsAdded(newHighlightsAdded);
		fileUploadResponse.setNonHighlightsSkipped(nonHighlightsSkipped);
		fileUploadResponse.setSegmentsProcessed(segmentsProcessed);

		logger.info("HighlightService.parseFile() - End parsing the input file. " + fileUploadResponse.toString());
		return fileUploadResponse;

	}

	/**
	 * Toggles the hidden flag between true and false. The front end can then use
	 * this to show/hide highlights.
	 * 
	 * This is better than deleting a highlight, which means it may get recreated
	 * again
	 *
	 * @param request        - The timestampHash of the highlight to which the
	 *                             tag should be added.
	 * 
	 * @param OutputStringResponse - The tag to add, provided in the request body.
	 * 
	 * @return message - indicating whether the tag was added, already existed, or
	 *         the highlight was not found.
	 */
	public int bulkUpdateHiddenFlag(BulkUpdateBooleanRequest request) {

		logger.info("[HighlightService.bulkUpdateHiddenFlag] - Attempting to bulk update [{}] highlights to Hidden-Flag = [{}]", request.getTimestampHashes().size(), request.getNewBoolean());

		List<String> timestampHashes = request.getTimestampHashes();
		Boolean newHiddenValue = request.getNewBoolean();

		Firestore db = FirestoreClient.getFirestore();
		WriteBatch batch = db.batch();

		int updatedCount = 0;

		try {

			for (String timestampHash : timestampHashes) {

				DocumentReference docRef = db.collection("highlights").document(timestampHash);

				// update only the field (better than set)
				batch.update(docRef, "hidden", newHiddenValue);

				updatedCount++;
			}

			// execute all updates atomically
			batch.commit().get();

			logger.info("HighlightService.bulkUpdateHiddenFlag() - Updated [{}] highlights to hidden = [{}]", updatedCount, newHiddenValue);

			return updatedCount;

		} catch (Exception e) {
			throw new RuntimeException("Failed to update hidden property", e);
		}
	}

	/**
	 * Adds tags to multiple highlights in bulk.
	 * <p>
	 * This method processes a list of timestamp hashes and adds the specified tags
	 * to each corresponding highlight's highlightTags array. It then updates the
	 * highlightTagsString field with a sorted, comma-separated string of all tags.
	 * </p>
	 *
	 * @param request the bulk update request containing timestamp hashes and the tag string
	 * @return the number of highlights successfully updated
	 * @throws IllegalArgumentException if the request or its fields are invalid
	 */
	public int bulkAddTag(BulkUpdateStringRequest request) {
		
		if (request == null || request.getTimestampHashes() == null || request.getNewString() == null) {
			throw new IllegalArgumentException("Invalid request: request, timestampHashes, and newString cannot be null");
		}

		List<String> timestampHashes = request.getTimestampHashes();
		
		if (timestampHashes.isEmpty()) {
			logger.warn("[HighlightService.bulkAddTag] - No timestamp hashes provided");
			return 0;
		}

		String tagInput = request.getNewString().trim();
		if (tagInput.isEmpty()) {
			logger.warn("[HighlightService.bulkAddTag] - No tags provided to add");
			return 0;
		}

		// Process and clean tags: split, trim, filter empty, remove duplicates
		List<String> tagsToAdd = Arrays.stream(tagInput.split(","))
				.map(String::trim)
				.filter(tag -> !tag.isEmpty())
				.distinct()
				.toList();

		if (tagsToAdd.isEmpty()) {
			logger.warn("[HighlightService.bulkAddTag] - No valid tags to add after processing");
			return 0;
		}

		logger.info("[HighlightService.bulkAddTag] - Attempting to add tags {} to [{}] highlights", tagsToAdd, timestampHashes.size());

		Firestore db = FirestoreClient.getFirestore();
		int updatedCount = 0;

		try {
			// Phase 1: Batch add tags to highlightTags array
			WriteBatch addTagsBatch = db.batch();
			for (String timestampHash : timestampHashes) {
				DocumentReference docRef = db.collection("highlights").document(timestampHash);
				for (String tag : tagsToAdd) {
					addTagsBatch.update(docRef, "highlightTags", FieldValue.arrayUnion(tag));
				}
			}
			addTagsBatch.commit().get();
			logger.info("[HighlightService.bulkAddTag] - Successfully added tags to [{}] highlights", timestampHashes.size());

			// Phase 2: Fetch each document, sort tags, and update highlightTagsString
			WriteBatch updateStringBatch = db.batch();
			for (String timestampHash : timestampHashes) {
				try {
					DocumentSnapshot snapshot = db.collection("highlights")
							.document(timestampHash)
							.get()
							.get();

					if (snapshot.exists()) {
						@SuppressWarnings("unchecked")
						List<String> highlightTags = (List<String>) snapshot.get("highlightTags");

						if (highlightTags != null && !highlightTags.isEmpty()) {
							// Clean, sort, and deduplicate tags
							List<String> sortedTags = highlightTags.stream()
									.map(String::trim)
									.filter(tag -> !tag.isEmpty())
									.distinct()
									.sorted(String.CASE_INSENSITIVE_ORDER)
									.toList();

							// Create comma-separated string
							String tagString = String.join(", ", sortedTags);

							// Batch update the string field
							updateStringBatch.update(snapshot.getReference(), "highlightTagsString", tagString);
							logger.debug("[HighlightService.bulkAddTag] - Updated tag string for [{}]: [{}]", timestampHash, tagString);
						}
					} else {
						logger.warn("[HighlightService.bulkAddTag] - Document not found for timestampHash: [{}]", timestampHash);
					}
				} catch (Exception e) {
					logger.error("[HighlightService.bulkAddTag] - Error processing document [{}]: {}", timestampHash, e.getMessage(), e);
					// Continue with other documents
				}
			}
			updateStringBatch.commit().get();
			updatedCount = timestampHashes.size(); // Assuming all were processed, but in reality, some might have failed
			logger.info("[HighlightService.bulkAddTag] - Successfully updated tag strings for [{}] highlights", updatedCount);

		} catch (Exception e) {
			logger.error("[HighlightService.bulkAddTag] - Error during bulk tag addition: {}", e.getMessage(), e);
			throw new RuntimeException("Failed to bulk add tags", e);
		}

		return updatedCount;
	}

	/**
	 * Removes a tag from the specified highlight.
	 * 
	 * @param timestampHash - The timestampHash of the highlight to update
	 * 
	 * @param tag           - The tag to remove (case-insensitive)
	 * 
	 * @return - {@code true} if the tag was removed; {@code false} if not found or
	 *         highlight does not exist
	 */
	/**
	 * Removes tags from multiple highlights in bulk.
	 * <p>
	 * This method processes a list of timestamp hashes and removes the specified tags
	 * from each corresponding highlight's highlightTags array. It then updates the
	 * highlightTagsString field with a sorted, comma-separated string of the remaining tags.
	 * </p>
	 *
	 * @param request the bulk update request containing timestamp hashes and the tag string
	 * @return the number of highlights successfully updated
	 * @throws IllegalArgumentException if the request or its fields are invalid
	 */
	public int bulkRemoveTag(BulkUpdateStringRequest request) {
		
		if (request == null || request.getTimestampHashes() == null || request.getNewString() == null) {
			throw new IllegalArgumentException("Invalid request: request, timestampHashes, and newString cannot be null");
		}

		List<String> timestampHashes = request.getTimestampHashes();
		
		if (timestampHashes.isEmpty()) {
			logger.warn("[HighlightService.bulkRemoveTag] - No timestamp hashes provided");
			return 0;
		}

		String tagInput = request.getNewString().trim();
		if (tagInput.isEmpty()) {
			logger.warn("[HighlightService.bulkRemoveTag] - No tags provided to remove");
			return 0;
		}

		// Process and clean tags: split, trim, filter empty, remove duplicates
		List<String> tagsToRemove = Arrays.stream(tagInput.split(","))
				.map(String::trim)
				.filter(tag -> !tag.isEmpty())
				.distinct()
				.toList();

		if (tagsToRemove.isEmpty()) {
			logger.warn("[HighlightService.bulkRemoveTag] - No valid tags to remove after processing");
			return 0;
		}

		logger.info("[HighlightService.bulkRemoveTag] - Attempting to remove tags {} from [{}] highlights", tagsToRemove, timestampHashes.size());

		Firestore db = FirestoreClient.getFirestore();
		int updatedCount = 0;

		try {
			// Phase 1: Batch remove tags from highlightTags array
			WriteBatch removeTagsBatch = db.batch();
			for (String timestampHash : timestampHashes) {
				DocumentReference docRef = db.collection("highlights").document(timestampHash);
				for (String tag : tagsToRemove) {
					removeTagsBatch.update(docRef, "highlightTags", FieldValue.arrayRemove(tag));
				}
			}
			removeTagsBatch.commit().get();
			logger.info("[HighlightService.bulkRemoveTag] - Successfully removed tags from [{}] highlights", timestampHashes.size());

			// Phase 2: Fetch each document, sort remaining tags, and update highlightTagsString
			WriteBatch updateStringBatch = db.batch();
			for (String timestampHash : timestampHashes) {
				try {
					DocumentSnapshot snapshot = db.collection("highlights")
							.document(timestampHash)
							.get()
							.get();

					if (snapshot.exists()) {
						@SuppressWarnings("unchecked")
						List<String> highlightTags = (List<String>) snapshot.get("highlightTags");

						if (highlightTags != null && !highlightTags.isEmpty()) {
							// Clean, sort, and deduplicate remaining tags
							List<String> sortedTags = highlightTags.stream()
									.map(String::trim)
									.filter(tag -> !tag.isEmpty())
									.distinct()
									.sorted(String.CASE_INSENSITIVE_ORDER)
									.toList();

							// Create comma-separated string
							String tagString = String.join(", ", sortedTags);

							// Batch update the string field
							updateStringBatch.update(snapshot.getReference(), "highlightTagsString", tagString);
							logger.debug("[HighlightService.bulkRemoveTag] - Updated tag string for [{}]: [{}]", timestampHash, tagString);
						} else {
							// If no tags left, set empty string
							updateStringBatch.update(snapshot.getReference(), "highlightTagsString", "");
							logger.debug("[HighlightService.bulkRemoveTag] - Cleared tag string for [{}]", timestampHash);
						}
					} else {
						logger.warn("[HighlightService.bulkRemoveTag] - Document not found for timestampHash: [{}]", timestampHash);
					}
				} catch (Exception e) {
					logger.error("[HighlightService.bulkRemoveTag] - Error processing document [{}]: {}", timestampHash, e.getMessage(), e);
					// Continue with other documents
				}
			}
			updateStringBatch.commit().get();
			updatedCount = timestampHashes.size(); // Assuming all were processed, but in reality, some might have failed
			logger.info("[HighlightService.bulkRemoveTag] - Successfully updated tag strings for [{}] highlights", updatedCount);

		} catch (Exception e) {
			logger.error("[HighlightService.bulkRemoveTag] - Error during bulk tag removal: {}", e.getMessage(), e);
			throw new RuntimeException("Failed to bulk remove tags", e);
		}

		return updatedCount;
	}

	/**
	 * Updates the note field in a unique highlight.
	 * 
	 * @param timestampHash        - The timestampHash of the highlight where the
	 *                             note will be updated
	 * 
	 * @param OutputStringResponse - The note to update, provided in the request
	 *                             body
	 * 
	 * @return message - indicating whether the note was added
	 */
	public void updateNote(String timestampHash, InputStringRequest noteRequest) {

		String note = noteRequest.getInputString().trim();

		logger.info("HighlightService.updateNote() - HighlightId = [{}] Note = [{}]", timestampHash, note);

		Firestore db = FirestoreClient.getFirestore();
		DocumentReference docRef = db.collection("highlights").document(timestampHash);

		try {
			// Atomic update of single field
			docRef.update("note", note).get();

		} catch (Exception e) {
			throw new RuntimeException("Failed to update note for highlight: " + timestampHash, e);
		}
	}

	/**
	 * Updates the source field in a unique highlight.
	 * 
	 * @param timestampHash        - The timestampHash of the highlight where the
	 *                             source will be updated
	 * 
	 * @param OutputStringResponse - The source to update, provided in the request
	 *                             body
	 * 
	 * @return message - indicating whether the note was added
	 */
	public int bulkUpdateSource(BulkUpdateStringRequest request) {

		logger.info("[HighlightService.bulkUpdateSource] - Attempting to bulk update [{}] highlights to update Source = [{}]", request.getTimestampHashes().size(), request.getNewString());

		List<String> ids = request.getTimestampHashes();
		String newSource = request.getNewString();

		Firestore db = FirestoreClient.getFirestore();

		try {

			if (ids == null || ids.isEmpty()) {
				throw new IllegalArgumentException("IDs list cannot be empty");
			}

			WriteBatch batch = db.batch();
			int updateCount = 0;

			for (String id : ids) {

				DocumentReference docRef = db.collection("highlights").document(id);

				batch.update(docRef, "source", newSource);
				updateCount++;
			}

			// Firestore limit: 500 writes per batch
			batch.commit().get();

			return updateCount;

		} catch (Exception e) {
			throw new RuntimeException("Failed to bulk update author", e);
		}
	}
	

	/**
	 * Updates the source field in a unique highlight.
	 * 
	 * @param timestampHash        - The timestampHash of the highlight where the
	 *                             source will be updated
	 * 
	 * @param OutputStringResponse - The source to update, provided in the request
	 *                             body
	 * 
	 * @return message - indicating whether the note was added
	 */
	public int bulkUpdateVisibility(BulkUpdateStringRequest request) {

		logger.info("[HighlightService.bulkUpdateVisibility] - Attempting to bulk update [{}] highlights to update Visibility = [{}]", request.getTimestampHashes().size(), request.getNewString());
		
		List<String> ids = request.getTimestampHashes();
		
		String input = request.getNewString();

		Firestore db = FirestoreClient.getFirestore();

		try {

			if (ids == null || ids.isEmpty()) {
				throw new IllegalArgumentException("IDs list cannot be empty");
			}

			WriteBatch batch = db.batch();
			int updateCount = 0;

			for (String id : ids) {

				DocumentReference docRef = db.collection("highlights").document(id);

				batch.update(docRef, "visibility", input);
				updateCount++;
			}

			// Firestore limit: 500 writes per batch
			batch.commit().get();

			return updateCount;

		} catch (Exception e) {
			throw new RuntimeException("Failed to bulk update author", e);
		}
	}

	/**
	 * Updates ALL highlights with matching input Author string to the new Author
	 * string.
	 * 
	 * @param BulkUpdateStringRequest - An object with an oldString (oldAuthor) and
	 *                                a newString (newAuthor)
	 * 
	 * @return int - The number of documents (highlights) updated
	 */
	public int bulkUpdateAuthor(BulkUpdateStringRequest request) {

		logger.info("[HighlightService.bulkUpdateAuthor] - Attempting to bulk update [{}] highlights to update Author = [{}]", request.getTimestampHashes().size(), request.getNewString());
		
		List<String> ids = request.getTimestampHashes();
		String newAuthor = request.getNewString();

		Firestore db = FirestoreClient.getFirestore();

		try {

			if (ids == null || ids.isEmpty()) {
				throw new IllegalArgumentException("IDs list cannot be empty");
			}

			WriteBatch batch = db.batch();
			int updateCount = 0;

			for (String id : ids) {

				DocumentReference docRef = db.collection("highlights").document(id);

				batch.update(docRef, "author", newAuthor);
				updateCount++;
			}

			// Firestore limit: 500 writes per batch
			batch.commit().get();

			return updateCount;

		} catch (Exception e) {
			throw new RuntimeException("Failed to bulk update author", e);
		}
	}

	/**
	 * Updates ALL highlights with matching input Title string to the new Title
	 * string.
	 * 
	 * @param BulkUpdateStringRequest - An object with an oldString (oldTitle) and a
	 *                                newString (newTitle)
	 * 
	 * @return int - The number of documents (highlights) updated
	 */
	public int bulkUpdateTitle(BulkUpdateStringRequest request) {

		logger.info("[HighlightService.bulkUpdateTitle] - Attempting to bulk update [{}] highlights to update Title = [{}]", request.getTimestampHashes().size(), request.getNewString());
		
		List<String> ids = request.getTimestampHashes();
		String newTitle = request.getNewString().trim();

		Firestore db = FirestoreClient.getFirestore();

		try {

			if (ids == null || ids.isEmpty()) {
				throw new IllegalArgumentException("IDs list cannot be empty");
			}

			WriteBatch batch = db.batch();
			int updateCount = 0;

			for (String id : ids) {

				DocumentReference docRef = db.collection("highlights").document(id);

				batch.update(docRef, "title", newTitle);
				updateCount++;
			}

			// Firestore limit: 500 writes per batch
			batch.commit().get();

			return updateCount;

		} catch (Exception e) {
			throw new RuntimeException("Failed to bulk update author", e);
		}
	}

	/**
	 * @param highlightRequest
	 * @return - the newly added highlight (in a list format)
	 * @throws InterruptedException
	 * @throws ExecutionException
	 */
	public List<Highlight> addManualHightlight(HighlightRequest highlightRequest) throws InterruptedException, ExecutionException {

		// TODO: Validate the inputs before saving, handle exceptions
		
		Highlight highlight = highlightUtils.getHighlightFromRequest(highlightRequest);

		Firestore dbFireStore = FirestoreClient.getFirestore();

		// choose collection name
		CollectionReference highlightsCollection = dbFireStore.collection("highlights");

		// Use your own ID
		String customId = highlight.getTimestampHash();
		DocumentReference docRef = highlightsCollection.document(customId);

		// Save the document
		ApiFuture<WriteResult> future = docRef.set(highlight);

		List<Highlight> highlights = new ArrayList<>();

		highlights.add(highlight);

		return highlights;

	}

	public void deleteAll() {
		
		logger.info("[HighlightService.deleteAll] - Attempting to delete all database entries");
		
		Firestore db = FirestoreClient.getFirestore();
		CollectionReference highlightsCollection = db.collection("highlights");

		try {
			// Get all documents
			ApiFuture<QuerySnapshot> future = highlightsCollection.get();
			List<QueryDocumentSnapshot> documents = future.get().getDocuments();

			for (QueryDocumentSnapshot doc : documents) {
				// Delete each document
				doc.getReference().delete();
			}

			logger.info("HighlightService.updateNote() - All highlights deleted. Total deleted: {}", documents.size());
		} catch (Exception e) {
			logger.error("Error deleting all highlights from Firestore", e);
		}
	}

	public int bulkUpdateExcerpt(BulkUpdateStringRequest request) {
		
		logger.info("[HighlightService.bulkUpdateExcerpt] - Attempting to bulk update [{}] highlights to update Excerpt = [{}]", request.getTimestampHashes().size(), request.getNewString());
		
		List<String> ids = request.getTimestampHashes();
		String newExcerpt = request.getNewString();

		Firestore db = FirestoreClient.getFirestore();

		try {

			if (ids == null || ids.isEmpty()) {
				throw new IllegalArgumentException("IDs list cannot be empty");
			}

			WriteBatch batch = db.batch();
			int updateCount = 0;

			for (String id : ids) {

				DocumentReference docRef = db.collection("highlights").document(id);

				batch.update(docRef, "excerpt", newExcerpt);
				updateCount++;
			}

			// Firestore limit: 500 writes per batch
			batch.commit().get();

			return updateCount;

		} catch (Exception e) {
			throw new RuntimeException("Failed to bulk update excerpt", e);
		}
		
	}

	public void saveIndividualHighlight(Highlight highlight) {

	    Firestore dbFireStore = FirestoreClient.getFirestore();

	    CollectionReference highlightsCollection = dbFireStore.collection("highlights");

	    String customId = highlight.getTimestampHash();
	    DocumentReference docRef = highlightsCollection.document(customId);
	    
	    //TODO: This is not threadsafe
	    
		try {
			ApiFuture<DocumentSnapshot> future = docRef.get();
			DocumentSnapshot document = future.get();

			if (!document.exists()) {
				// Document ID doesn't exist → add new highlight
				docRef.set(highlight);
				logger.info("HighlightService.saveIndividualHighlight() - New highlight added to Firestore. timestampHash = [{}]", highlight.getTimestampHash());
			} else {
				logger.info("HighlightService.saveIndividualHighlight() - Existing highlight skipped. timestampHash = [{}]", highlight.getTimestampHash());
			}
		} catch (Exception e) {
			logger.error("Error adding highlight to Firestore", e);
		}
		
	}

}
