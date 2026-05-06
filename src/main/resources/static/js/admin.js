$(document).ready(function() {

	// =========================
	// 1. VARIABLES / SETUP
	// =========================
	
	console.log('🚀 admin.js loaded - initializing page...');
	
	// Map to store selected highlights: key = timestampHash, value = { id, title, author, source, excerpt, visibility, highlightTagsString }
	// This persists selections across pagination and page redraws
	const selectedHighlights = new Map();
	selectedHighlights.clear();
	console.log('✅ selectedHighlights Map initialized');

	// Reset all checkboxes on page load
	$('input[name="selectedHighlights"]').prop('checked', false);
	console.log('✅ All checkboxes reset to unchecked');

	// Reset the "select all" checkbox in the header
	$("#selectAll").prop('checked', false);
	console.log('✅ Select-all checkbox reset');

	// Update the selection counter display
	updateSelectionCount();
	console.log('✅ Selection count updated');

	// Initialize DataTable with pagination and sorting
	const table = $('#highlightsTable').DataTable({
		pageLength: 50,                              // Show 50 rows per page
		columnDefs: [
			{ orderable: false, targets: 0 }           // Column 0 (checkboxes) is not sortable
		],
		order: [[1, 'desc']]                         // Default sort by ID (column 1) in descending order
	});
	console.log('✅ DataTable initialized');

	// Restore checkbox state every time the table is redrawn (e.g., pagination, sorting)
	// This ensures selections persist across page changes
	table.on('draw', function() {
		console.log('📄 Table redrawn - restoring checkbox state');

		// Loop through all visible checkboxes and check/uncheck based on the selectedHighlights map
		$('input[name="selectedHighlights"]').each(function() {
			this.checked = selectedHighlights.has(this.value);
		});

		// Update the selection counter display
		updateSelectionCount();
	});

	// Setup event handlers for individual edit buttons (single-row updates)
	// Each button triggers a modal with the selected row's data
	setupEditButtonHandler('a.editTitleButton', 'updateTitleModal', '#updateTitleTable', 'title');
	setupEditButtonHandler('a.editAuthorButton', 'updateAuthorModal', '#updateAuthorTable', 'author');
	setupEditButtonHandler('a.editSourceButton', 'updateSourceModal', '#updateSourceTable', 'source');
	setupEditButtonHandler('a.addTagButton', 'addTagModal', '#addTagTable', 'highlightTagsString');
	setupEditButtonHandler('a.removeTagButton', 'removeTagModal', '#removeTagTable', 'highlightTagsString');
	setupEditButtonHandler('a.editVisibilityButton', 'updateVisibilityModal', '#updateVisibilityTable', 'visibility');

	// =========================
	// CHECKBOX SELECTION LISTENERS
	// =========================
	// These handlers manage which rows are selected for bulk operations

	// Individual checkbox: Toggle row selection in the map when user clicks a checkbox
	$('#highlightsTable').on('change', 'input[name="selectedHighlights"]', function() {
		console.log('☑️  Checkbox clicked - ID:', this.value, 'Checked:', this.checked);

		// Extract all data about this highlight from the checkbox's data attributes
		const id = this.value;
		const title = this.dataset.title;
		const author = this.dataset.author;
		const excerpt = this.dataset.excerpt;
		const source = this.dataset.source;
		const visibility = this.dataset.visibility;
		const highlightTagsString = this.dataset.highlightTagsString || '';

		// Add to map if checked, remove if unchecked
		if (this.checked) {
			// Store all highlight info in the map for later use in bulk operations
			selectedHighlights.set(id, {
				id: id,
				title: title,
				author: author,
				source: source,
				visibility: visibility,
				excerpt: excerpt,
				highlightTagsString: highlightTagsString
			});
		} else {
			// Remove from map if unchecked
			selectedHighlights.delete(id);
		}

		// Update the counter and toggle bulk action buttons
		updateSelectionCount();
		console.log('📊 Current selection:', Array.from(selectedHighlights.keys()));
	}
	);

	// Select All checkbox: Toggle all visible rows on the current page when user clicks the header checkbox
	$("#selectAll").on("change", function() {
		console.log('🔲 Select All clicked - Checked:', this.checked);

		const isChecked = this.checked;

		// Get only the rows visible on the CURRENT PAGE (respects pagination)
		const rows = table.rows({ page: 'current' }).nodes();

		$('input[name="selectedHighlights"]', rows).each(function() {

			this.checked = isChecked;

			const id = this.value;
			const title = this.dataset.title;
			const author = this.dataset.author;
			const excerpt = this.dataset.excerpt;
			const source = this.dataset.source;
			const visibility = this.dataset.visibility;
			const highlightTagsString = this.dataset.highlightTagsString || '';

			if (isChecked) {
				selectedHighlights.set(id, {
					id: id,
					title: title,
					author: author,
					source: source,
					excerpt: excerpt,
					visibility: visibility,
					highlightTagsString: highlightTagsString
				});
			} else {
				selectedHighlights.delete(id);
			}
		});

		updateSelectionCount();
	});

	// Deselect All button: Clear all selections across ALL pages (not just current page)
	document.getElementById("deselectAll").addEventListener("click", function() {
		 deselectAllHighlights();
	});

	document.getElementById("showAll").addEventListener("click", function() {
		console.log('👁️ Show All Excerpts clicked');

		table.rows().every(function() {
			const row = this;
			const tr = $(row.node());
			const excerpt = tr.find('a.toggleExcerpt').data('excerpt');
			const visibility = tr.data('visibility');

			// Only open if not already shown
			if (!row.child.isShown()) {
				row.child(formatExcerpt(excerpt, visibility)).show();
				tr.addClass('shown');
			}
		});
	});
	
	document.getElementById("hideAll").addEventListener("click", function() {
		console.log('🙈 Hide All Excerpts clicked');

		table.rows().every(function() {
			const row = this;
			const tr = $(row.node());

			// Only hide if currently shown
			if (row.child.isShown()) {
				row.child.hide();
				tr.removeClass('shown');
			}
		});
	});


	// =========================
	// BULK UPDATE BUTTON LISTENERS
	// =========================
	// These buttons open modals for bulk editing selected rows
	
	// Bulk Update Title button: Display selected rows' titles in modal
	document.getElementById("openUpdateTitleModal").addEventListener("click", function() {
		console.log('📝 Opening Update Title modal with', selectedHighlights.size, 'row(s)');
		populateModalTable("#updateTitleTable", "title");
	});

	document.getElementById("openUpdateAuthorModal").addEventListener("click", function() {
		console.log('👤 Opening Update Author modal with', selectedHighlights.size, 'row(s)');
		populateModalTable("#updateAuthorTable", "author");
	});

	document.getElementById("openUpdateSourceModal").addEventListener("click", function() {
		console.log('📚 Opening Update Source modal with', selectedHighlights.size, 'row(s)');
		populateModalTable("#updateSourceTable", "source");
	});

	document.getElementById("openAddTagModal").addEventListener("click", function() {
		console.log('🏷️  Opening Add Tag modal with', selectedHighlights.size, 'row(s)');
		populateModalTable("#addTagTable", "highlightTagsString");
	});

	document.getElementById("openRemoveTagModal").addEventListener("click", function() {
		console.log('🗑️  Opening Remove Tag modal with', selectedHighlights.size, 'row(s)');
		populateModalTable("#removeTagTable", "highlightTagsString");
	});

	document.getElementById("openVisibilityModal").addEventListener("click", function() {
		console.log('👁️  Opening Update Visibility modal with', selectedHighlights.size, 'row(s)');
		populateModalTable("#updateVisibilityTable", "visibility");
	});


	// =========================
	// SINGLE UPDATE BUTTON LISTENERS
	// =========================
	// These handlers manage the excerpt editing modal (single-row update)

	// Excerpt modal: Populate form when user clicks the edit excerpt button
	const modal = document.getElementById('updateExcerptModal');

	modal.addEventListener('show.bs.modal', function(event) {
		console.log('📝 Excerpt edit modal opening');
		const trigger = event.relatedTarget;

		const timestampHash = trigger.getAttribute('data-timestampHash');
		const title = trigger.getAttribute('data-title');
		const author = trigger.getAttribute('data-author');
		const excerpt = trigger.getAttribute('data-excerpt');

		// Fill table
		const row = modal.querySelector('#updateExcerptTable tbody tr');
		row.children[0].textContent = timestampHash;
		row.children[1].textContent = title;
		row.children[2].textContent = author;

		// Fill textarea
		document.getElementById('newExcerpt').value = excerpt;

		// Get form
		const form = modal.querySelector('.singleUpdateForm');

		// Remove old hidden input (important if modal is reused)
		form.querySelectorAll('input[name="timestampHashes"]').forEach(el => el.remove());

		// Create + inject hidden input immediately
		const input = document.createElement("input");
		input.type = "hidden";
		input.name = "timestampHashes";
		input.value = timestampHash;

		form.appendChild(input);
		console.log('📝 Excerpt form prepared with ID:', timestampHash);
	});


	// =========================
	// SHOW/HIDE ROW DETAILS
	// =========================

	// Toggle Excerpt visibility: Expand/collapse the excerpt text below each row
	$('#highlightsTable tbody').on('click', 'a.toggleExcerpt', function(e) {
		e.preventDefault();
		console.log('⬇️  Excerpt toggle clicked');

		const tr = $(this).closest('tr');
		const row = table.row(tr);
		const excerpt = $(this).data('excerpt');
		const visibility = $(this).data('visibility');

		if (row.child.isShown()) {
			// Close: hide the excerpt details row
			console.log('⬆️  Hiding excerpt');
			row.child.hide();
			tr.removeClass('shown');
		} else {
			// Open: show the formatted excerpt details row
			console.log('⬇️  Showing excerpt');
			row.child(formatExcerpt(excerpt, visibility)).show();
			tr.addClass('shown');
		}
	});

	// =========================
	// SUBMISSIONS VIA AJAX
	// =========================

	// Setup AJAX submission for all bulk update forms
	document.querySelectorAll(".bulkUpdateForm").forEach(form => {
		console.log('📋 Setting up bulk form handler');

		form.addEventListener("submit", function(e) {
			e.preventDefault();
			console.log('📤 Bulk update form submitted to:', this.getAttribute("action"));

			// Capture the IDs being updated BEFORE clearing selection
			// We need this to know which rows to refresh in the table
			const updatedIds = Array.from(selectedHighlights.keys());
			console.log('📤 Updating', updatedIds.length, 'row(s):', updatedIds);

			// Get the target URL from the form
			const actionUrl = form.getAttribute("action");
			console.log('📤 Sending request to:', actionUrl);

			// Build JSON request body with the field name and value
			// Extract the form field name and value (e.g., newAuthor, newTitle, etc.)
			const formElements = form.querySelectorAll('input[type="text"], textarea, select');
			const updateData = {};
			
			// Get the form field that contains the new value to apply
			let fieldValue = '';
			
			for (let element of formElements) {
				if (element.name && element.name !== 'timestampHashes') {
					fieldValue = element.value;
					break;
				}
			}

			console.log('📤 Value being sent:', fieldValue);

			// Create the request body with IDs and the field value
			// Backend expects "newString" field name regardless of which field is being updated
			const requestBody = {
				timestampHashes: updatedIds,
				newString: fieldValue
			};

			console.log('📤 Request payload:', requestBody);

			// Send data to server via AJAX using JSON
			fetch(actionUrl, {
				method: "POST",
				headers: {
					'Content-Type': 'application/json'
				},
				body: JSON.stringify(requestBody)
			})
			.then(response => {
				// Check if the request was successful
				if (!response.ok) {
					throw new Error("Network response was not ok");
				}
				return response.text();
			})
			.then(data => {
				console.log('✅ Bulk update successful, response:', data);
				// Close the modal immediately after successful update
				const modalId = form.closest(".modal").id;
				console.log('🔒 Closing modal:', modalId);
				const modalElement = document.getElementById(modalId);
				const modalInstance = bootstrap.Modal.getOrCreateInstance(modalElement);
				modalInstance.hide();

				// Fetch fresh data from API and update only the changed rows in the table
				// This is more efficient than reloading the entire table
				console.log('🔄 Fetching updated data for', updatedIds.length, 'row(s)...');
				updateTableRows(updatedIds);

				// Show success message (optional)
				console.log("Update successful:", data);
			})
			.catch(error => {
				console.error("❌ Error during bulk update:", error);
				alert("An error occurred while updating. Please try again.");
			});
		});

	});
	
	// Handle single update form submission with AJAX
	document.querySelector('.singleUpdateForm').addEventListener('submit', function(e) {
		e.preventDefault();
		console.log('📤 Submitting single excerpt update form');

		const form = this;
		const actionUrl = form.getAttribute("action");
		const timestampHash = form.querySelector('input[name="timestampHashes"]').value;
		
		// Get the timestampHash being updated from the hidden input
		const payload = {
				timestampHashes: [form.querySelector('input[name="timestampHashes"]').value],
				newString: form.querySelector('textarea[name="newString"]').value
			};
			
		console.log('📤 Sending request to:', actionUrl, 'for ID:', timestampHash);

		// Send AJAX request
		fetch(actionUrl, {
			method: "POST",
			headers: {
				"Content-Type": "application/json"
			},
			body: JSON.stringify(payload)
		})
		.then(response => {
			if (!response.ok) {
				throw new Error("Network response was not ok");
			}
			return response.text();
		})
		.then(data => {
			console.log('✅ Single update successful, response:', data);
			// Close the modal
			const modalElement = document.getElementById('updateExcerptModal');
			const modalInstance = bootstrap.Modal.getOrCreateInstance(modalElement);
			modalInstance.hide();
			console.log('🔒 Modal closed');

			// Update only the specific row that was changed
			console.log('🔄 Updating changed row(s)...');
			updateTableRows([timestampHash]);

			// Log success for debugging
			console.log("Update successful:", data);
		})
		.catch(error => {
			// Handle network or server errors
			console.error("❌ Error during single update:", error);
			alert("An error occurred while updating. Please try again.");
		});
	});


	// Auto-resize textarea: Adjust height as user types
	const textarea = document.getElementById('newExcerpt');
	textarea.addEventListener('input', function() {
		autoResize(this);
	});

	// Also resize when modal is shown (in case content changed)
	document.getElementById('updateExcerptModal')
		.addEventListener('shown.bs.modal', function() {
			autoResize(textarea);
		});

	// Initialize selection counter display
	document.getElementById("selectionCount").textContent = selectedHighlights.size;

	// =========================
	// HELPER FUNCTIONS
	// =========================

	/**
	 * Fetch only the changed rows from the API and update them in the table
	 * This is more efficient than reloading the entire table
	 * @param {Array<string>} timestampHashes - Array of IDs that were updated
	 */
	function updateTableRows(timestampHashes) {
		// Exit early if no rows to update
		if (timestampHashes.length === 0) {
			console.log('⏭️  No rows to update');
			return;
		}

		console.log('🔄 updateTableRows() - Fetching', timestampHashes.length, 'row(s) from API');

		// Call the /api/highlights/batch endpoint with the list of IDs
		// This returns only the highlights that were changed
		fetch('/api/highlights/batch', {
			method: 'POST',
			headers: {
				'Content-Type': 'application/json'
			},
			body: JSON.stringify(timestampHashes)
		})
		.then(response => {
			if (!response.ok) throw new Error('API returned status: ' + response.status);
			console.log('✅ API response received');
			return response.json();
		})
		.then(updatedHighlights => {
			console.log('📦 Received', updatedHighlights.length, 'updated highlight(s)');
			// Update each row with the fresh data from the server
			updatedHighlights.forEach(h => {
				console.log('⚙️  Updating row:', h.timestampHash);
				updateOrReplaceTableRow(h);
			});

			// Clear the selection since update is complete
			selectedHighlights.clear();
			updateSelectionCount();
			deselectAllHighlights();

			console.log('✅ Updated ' + updatedHighlights.length + ' row(s)');
		})
		.catch(error => {
			// If batch update fails, fall back to full table reload
			console.error("❌ Error updating specific rows:", error);
			console.log('⚠️  Falling back to full table reload...');
			reloadTableData();
		});
	}

	/**
	 * Find a row by ID and update its content with fresh data
	 * Only updates rows that are currently visible in the table
	 * @param {Object} highlight - The updated highlight object from the API
	 */
	function updateOrReplaceTableRow(highlight) {
		console.log('🔎 Searching for row ID:', highlight.timestampHash);
		let rowFound = false;

		// Search through all visible rows to find the matching one by ID
		// Use DataTables .every() API instead of .indexes() for proper iteration
		table.rows().every(function() {
			const rowNode = this.node();
			const checkbox = rowNode.querySelector('input[name="selectedHighlights"]');
			
			if (checkbox && checkbox.value === highlight.timestampHash) {
				// Found the row - update its content with fresh data from the server
				console.log('✏️ Found row, updating content for ID:', highlight.timestampHash);
				updateRowData(rowNode, highlight);
				rowFound = true;
				return false; // Break out of the .every() loop
			}
		});

		// If row not found, it might be on a different page
		// Just redraw the current page without changing pagination
		if (!rowFound) {
			console.log('⚠️  Row not found in current page, redrawing...');
			table.draw(false);
		}
	}

	/**
	 * Update all cells in a row with fresh data from the server
	 * @param {HTMLElement} rowNode - The table row to update
	 * @param {Object} highlight - The updated highlight object with new data
	 */
	function updateRowData(rowNode, highlight) {
		console.log('🎨 Refreshing row cells for ID:', highlight.timestampHash);
		const row = $(rowNode);

		// Update ID cell (column 1)
		row.find('td').eq(1).text(highlight.timestampHash);
		
		// Update Title
		const titleCells = row.find('td').eq(3);
		titleCells.html(`
			<a class="editTitleButton" href="#">
				<span>✏️</span>
			</a>
			<span>${escapeHtml(highlight.title)}</span>
		`);

		// Update Author
		const authorCells = row.find('td').eq(4);
		authorCells.html(`
			<a class="editAuthorButton" href="#">
				<span>🖊️</span>
			</a>
			<span>${escapeHtml(highlight.author)}</span>
		`);

		// Update Source
		const sourceCells = row.find('td').eq(5);
		sourceCells.html(`
			<a class="editSourceButton" href="#">
				<span>🖋️</span>
			</a>
			<span>${escapeHtml(highlight.source)}</span>
		`);

		// Update Tags
		const tagsCells = row.find('td').eq(6);
		tagsCells.html(`
			<a class="addTagButton" href="#">
				<span>➕</span>
			</a>
			<a class="removeTagButton" href="#">
				<span>❌</span>
			</a>
			<span>${escapeHtml(highlight.highlightTagsString || '')}</span>
		`);

		// Update Visibility
		const visibilityCells = row.find('td').eq(7);
		const visibilityClass = highlight.visibility === 'shown' ? 'text-success' : 'text-danger';
		visibilityCells.html(`
			<a class="editVisibilityButton" href="#">
				<span>✏️</span>
			</a>
			<span class="${visibilityClass}">${highlight.visibility.charAt(0).toUpperCase() + highlight.visibility.slice(1)}</span>
		`);

		// Update Excerpt (if on current page)
		const excerptCells = row.find('td').eq(2);
		excerptCells.html(`
			<a id="openUpdateExcerptModal" href="#" style="text-decoration: none; color: inherit;" 
				data-timestampHash="${escapeHtml(highlight.timestampHash)}"
				data-title="${escapeHtml(highlight.title)}"
				data-author="${escapeHtml(highlight.author)}" 
				data-excerpt="${escapeHtml(highlight.excerpt)}"
				data-bs-toggle="modal" 
				data-bs-target="#updateExcerptModal">
				<span title="${escapeHtml(highlight.excerpt)}">&nbsp;&nbsp;📝</span>
			</a>
			<a class="toggleExcerpt" href="#" style="text-decoration: none; color: inherit;" 
				data-excerpt="${escapeHtml(highlight.excerpt)}"
				data-visibility="${escapeHtml(highlight.visibility)}">
				<span title="${escapeHtml(highlight.excerpt)}">⬇️</span>
			</a>
		`);

		// Update the checkbox's data attributes so they reflect the fresh data
		// These attributes are used if the user selects this row again
		const checkbox = rowNode.querySelector('input[name="selectedHighlights"]');
		if (checkbox) {
			checkbox.dataset.title = highlight.title;
			checkbox.dataset.author = highlight.author;
			checkbox.dataset.source = highlight.source;
			checkbox.dataset.excerpt = highlight.excerpt;
			checkbox.dataset.visibility = highlight.visibility;
			checkbox.dataset.highlightTagsString = highlight.highlightTagsString || '';
		}
	}

	/**
	 * Reload the ENTIRE table from the API (fallback function)
	 * Used when an error occurs during updates, ensures data is in sync
	 * Less efficient than updateTableRows() but guarantees accuracy
	 */
	function reloadTableData() {
		console.log('🔄🔄🔄 FULL TABLE RELOAD - Fetching all highlights from API');
		// Fetch ALL highlights from the server
		fetch('/api/highlights')
			.then(response => {
				if (!response.ok) throw new Error('API returned status: ' + response.status);
				console.log('✅ API response received');
				return response.json();
			})
			.then(highlights => {
				console.log('📦 Received', highlights.length, 'total highlights');
				// Clear all existing rows from the DataTable
				table.clear();
				console.log('🧹 Cleared existing rows');

				// Build new rows from the fresh API data
				highlights.forEach(h => {
					// Create a new table row element
					const tr = document.createElement('tr');

					// Column 0: Checkbox for bulk selection
					const tdCheckbox = document.createElement('td');
					const checkbox = document.createElement('input');
					checkbox.type = 'checkbox';
					checkbox.name = 'selectedHighlights';
					checkbox.value = h.timestampHash;  // Store the ID as the value
					checkbox.dataset.title = h.title;
					checkbox.dataset.author = h.author;
					checkbox.dataset.source = h.source;
					checkbox.dataset.excerpt = h.excerpt;
					checkbox.dataset.visibility = h.visibility;
					checkbox.dataset.highlightTagsString = h.highlightTagsString || '';
					tdCheckbox.appendChild(checkbox);
					tr.appendChild(tdCheckbox);

					// Column 1: ID (timestampHash)
					const tdId = document.createElement('td');
					tdId.textContent = h.timestampHash;
					tr.appendChild(tdId);

					// Column 2: Excerpt toggles and editor
					const tdExcerpt = document.createElement('td');
					
					// Edit excerpt button - opens modal
					const excerptEditLink = document.createElement('a');
					excerptEditLink.id = 'openUpdateExcerptModal';
					excerptEditLink.href = '#';
					excerptEditLink.style.textDecoration = 'none';
					excerptEditLink.style.color = 'inherit';
					// Store highlight data in data attributes for modal to use
					excerptEditLink.dataset.timestampHash = h.timestampHash;
					excerptEditLink.dataset.title = h.title;
					excerptEditLink.dataset.author = h.author;
					excerptEditLink.dataset.excerpt = h.excerpt;
					excerptEditLink.dataset.bsToggle = 'modal';
					excerptEditLink.dataset.bsTarget = '#updateExcerptModal';
					excerptEditLink.innerHTML = '<span title="' + escapeHtml(h.excerpt) + '">&nbsp;&nbsp;📝</span>';
					tdExcerpt.appendChild(excerptEditLink);

					// Toggle excerpt visibility button
					const excerptToggleLink = document.createElement('a');
					excerptToggleLink.className = 'toggleExcerpt';
					excerptToggleLink.href = '#';
					excerptToggleLink.style.textDecoration = 'none';
					excerptToggleLink.style.color = 'inherit';
					excerptToggleLink.dataset.excerpt = h.excerpt;  // Excerpt text to expand
					excerptToggleLink.innerHTML = '<span title="' + escapeHtml(h.excerpt) + '">⬇️</span>';
					tdExcerpt.appendChild(excerptToggleLink);
					tr.appendChild(tdExcerpt);

					// Column 3: Title with edit button
					const tdTitle = document.createElement('td');
					const editTitleLink = document.createElement('a');
					editTitleLink.className = 'editTitleButton';
					editTitleLink.href = '#';
					editTitleLink.innerHTML = '<span>✏️</span>';
					tdTitle.appendChild(editTitleLink);
					const titleSpan = document.createElement('span');
					titleSpan.textContent = h.title;
					tdTitle.appendChild(titleSpan);
					tr.appendChild(tdTitle);

					// Column 4: Author with edit button
					const tdAuthor = document.createElement('td');
					const editAuthorLink = document.createElement('a');
					editAuthorLink.className = 'editAuthorButton';
					editAuthorLink.href = '#';
					editAuthorLink.innerHTML = '<span>🖊️</span>';
					tdAuthor.appendChild(editAuthorLink);
					const authorSpan = document.createElement('span');
					authorSpan.textContent = h.author;
					tdAuthor.appendChild(authorSpan);
					tr.appendChild(tdAuthor);

					// Column 5: Source with edit button
					const tdSource = document.createElement('td');
					const editSourceLink = document.createElement('a');
					editSourceLink.className = 'editSourceButton';
					editSourceLink.href = '#';
					editSourceLink.innerHTML = '<span>🖋️</span>';
					tdSource.appendChild(editSourceLink);
					const sourceSpan = document.createElement('span');
					sourceSpan.textContent = h.source;
					tdSource.appendChild(sourceSpan);
					tr.appendChild(tdSource);

					// Column 6: Tags with add/remove buttons
					const tdTags = document.createElement('td');
					
					const addTagLink = document.createElement('a');
					addTagLink.className = 'addTagButton';
					addTagLink.href = '#';
					addTagLink.innerHTML = '<span>➕</span>';
					tdTags.appendChild(addTagLink);

					const removeTagLink = document.createElement('a');
					removeTagLink.className = 'removeTagButton';
					removeTagLink.href = '#';
					removeTagLink.innerHTML = '<span>❌</span>';
					tdTags.appendChild(removeTagLink);

					const tagsSpan = document.createElement('span');
					tagsSpan.textContent = h.highlightTagsString || '';
					tdTags.appendChild(tagsSpan);
					tr.appendChild(tdTags);

					// Column 7: Visibility status with edit button
					const tdVisibility = document.createElement('td');
					
					const editVisibilityLink = document.createElement('a');
					editVisibilityLink.className = 'editVisibilityButton';
					editVisibilityLink.href = '#';
					editVisibilityLink.innerHTML = '<span>✏️</span>';
					tdVisibility.appendChild(editVisibilityLink);

					// Color-code visibility: green for 'shown', red for 'hidden'
					const visibilitySpan = document.createElement('span');
					const visibilityClass = h.visibility === 'shown' ? 'text-success' : 'text-danger';
					visibilitySpan.className = visibilityClass;
					visibilitySpan.textContent = h.visibility.charAt(0).toUpperCase() + h.visibility.slice(1);
					tdVisibility.appendChild(visibilitySpan);
					tr.appendChild(tdVisibility);

					// Add the new row to the DataTable
					table.row.add(tr);
				});

				// Redraw the table to display all new rows
				table.draw();
				console.log('✅ Table redrawn with', highlights.length, 'rows');

				// Clear any selections since we've reloaded
				selectedHighlights.clear();
				updateSelectionCount();

				console.log("Table reloaded successfully");
			})
			.catch(error => {
				console.error("❌ Error reloading table:", error);
				alert("Failed to reload table data");
			});
	}

	/**
	 * Setup event listeners for individual edit buttons
	 * When clicked, these buttons open a modal with the selected row's data
	 * @param {string} buttonSelector - CSS selector for the button (e.g., 'a.editTitleButton')
	 * @param {string} modalId - ID of the modal to open
	 * @param {string} tableSelector - CSS selector for the modal table
	 * @param {string} dataKey - Which field to display/edit (e.g., 'title', 'author')
	 */
	function setupEditButtonHandler(buttonSelector, modalId, tableSelector, dataKey) {
		console.log('📋 Setting up edit button handler for:', buttonSelector);
		// Use event delegation to handle clicks on dynamically-created elements
		$('#highlightsTable tbody').on('click', buttonSelector, function(e) {
			e.preventDefault();
			console.log('✏️  Edit button clicked -', buttonSelector);

			// Get the row and its checkbox containing the highlight data
			const tr = $(this).closest('tr');
			const checkbox = tr.find('input[name="selectedHighlights"]')[0];

			const id = checkbox.value;
			const title = checkbox.dataset.title;
			const author = checkbox.dataset.author;
			const excerpt = checkbox.dataset.excerpt;
			const source = checkbox.dataset.source;
			const visibility = checkbox.dataset.visibility;
			const highlightTagsString = checkbox.dataset.highlightTagsString || '';

			$('input[name="selectedHighlights"]').prop('checked', false);
			selectedHighlights.clear();
			updateSelectionCount();

			selectedHighlights.set(id, {
				id: id,
				title: title,
				author: author,
				source: source,
				excerpt: excerpt,
				visibility: visibility,
				highlightTagsString: highlightTagsString
			});

			populateModalTable(tableSelector, dataKey);
			const modal = new bootstrap.Modal(document.getElementById(modalId));
			modal.show();
		});
	}

	/**
	 * Populate the modal preview table with selected highlights
	 * Shows which rows will be updated when the user submits the form
	 * @param {string} tableSelector - CSS selector for the modal table
	 * @param {string} dataKey - Which field to display (e.g., 'title', 'author')
	 */
	function populateModalTable(tableSelector, dataKey) {
		const tableBody = document.querySelector(`${tableSelector} tbody`);
		tableBody.innerHTML = "";  // Clear existing rows

		// Add a row for each selected highlight
		selectedHighlights.forEach(item => {
			const row = document.createElement("tr");
			row.innerHTML = `
				<td>${escapeHtml(item.id)}</td>
				<td>${escapeHtml(item[dataKey])}</td>
				<td title="${escapeHtml(item.excerpt)}">🔍</td>
			`;
			tableBody.appendChild(row);
		});
	}

	/**
	 * Escape HTML special characters to prevent XSS attacks
	 * Ensures user data is safely displayed in the DOM
	 * @param {string} text - The text to escape
	 * @returns {string} Escaped HTML-safe text
	 */
	function escapeHtml(text) {
		const div = document.createElement('div');
		div.textContent = text;
		return div.innerHTML;
	}

	/**
	 * Auto-resize a textarea to fit its content
	 * Grows as the user types more text
	 * @param {HTMLElement} el - The textarea element to resize
	 */
	function autoResize(el) {
		el.style.height = 'auto';              // Reset height first
		el.style.height = el.scrollHeight + 'px'; // Grow to content height
	}

	/**
	 * Update the selection counter and enable/disable bulk action buttons
	 * Called whenever the selection changes (checkbox checked/unchecked)
	 */
	function updateSelectionCount() {
		// Update the counter display with the number of selected rows
		document.getElementById("selectionCount").textContent = selectedHighlights.size;
		console.log('📊 Selection count updated:', selectedHighlights.size);
		// Enable/disable bulk action buttons based on selection
		toggleBulkButtons();
	}

	/**
	 * Enable/disable bulk action buttons based on selection state
	 * Buttons are disabled when no rows are selected
	 */
	function toggleBulkButtons() {
		const hasSelection = selectedHighlights.size > 0;
		console.log('🔘 Toggling bulk buttons - enabled:', hasSelection);

		// Disable buttons if no selection, enable if something is selected
		document.querySelectorAll(".bulk-action").forEach(btn => {
			btn.disabled = !hasSelection;
		});
	}

	/**
	 * Format an excerpt for display in an expanded row
	 * Preserves line breaks and applies styling
	 * @param {string} text - The excerpt text to format
	 * @returns {string} HTML string with formatted excerpt
	 */
	function formatExcerpt(text, visibility) {

		// Escape the text to prevent XSS attacks
		const safe = $('<div>').text(text.trim()).html();
		const formatted = safe
		  .replace(/\n\n+/g, '<div style="height:0.5em"></div>') // smaller gap
		  .replace(/\n/g, '<br>');
		const color = visibility === 'shown' ? 'inherit' : 'red';


		return `
			<div style="
			    padding:15px;
			    background:#f7f7f7;
			    border-left:4px solid #007bff;
			    white-space: pre-line;
			    color: ${color};">
			    ${formatted}
			</div>`;
	}
	
	/**
	 * Deslect all the checkboxes across all the pages
	 */
	function deselectAllHighlights() {
	    console.log('❌ Deselect All triggered');

	    // Get ALL rows in the table (across all pages)
	    const rows = table.rows().nodes();

	    // Untick all checkboxes
	    $('input[name="selectedHighlights"]', rows).each(function () {
	        const id = this.value;

	        this.checked = false;
	        selectedHighlights.delete(id);
	    });

	    // Untick header checkbox (do this once, not inside loop)
	    const headerCheckbox = document.getElementById("selectAll");
	    if (headerCheckbox) {
	        headerCheckbox.checked = false;
	    }

	    updateSelectionCount();

	    console.log('❌ All selections cleared');
	}

});  // End of $(document).ready()
