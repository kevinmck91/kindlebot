package com.kevinmck91.kindlebot.dto.response;

public class FileUploadResponse {

	private int segmentsProcessed = 0;
	private int newHighlightsAdded = 0;
	private int nonHighlightsSkipped = 0;
	private int duplicateHighlightsSkipped = 0;
	private int errorHighlightsSkipped = 0;

	public int getSegmentsProcessed() {
		return segmentsProcessed;
	}

	public void setSegmentsProcessed(int segmentsProcessed) {
		this.segmentsProcessed = segmentsProcessed;
	}

	public int getNewHighlightsAdded() {
		return newHighlightsAdded;
	}

	public void setNewHighlightsAdded(int newHighlightsAdded) {
		this.newHighlightsAdded = newHighlightsAdded;
	}

	public int getErrorHighlightsSkipped() {
		return errorHighlightsSkipped;
	}

	public void setErrorHighlightsSkipped(int errorHighlightsSkipped) {
		this.errorHighlightsSkipped = errorHighlightsSkipped;
	}

	public int getDuplicateHighlightsSkipped() {
		return duplicateHighlightsSkipped;
	}

	public void setDuplicateHighlightsSkipped(int duplicateHighlightsSkipped) {
		this.duplicateHighlightsSkipped = duplicateHighlightsSkipped;
	}

	public int getNonHighlightsSkipped() {
		return nonHighlightsSkipped;
	}

	public void setNonHighlightsSkipped(int nonHighlightsSkipped) {
		this.nonHighlightsSkipped = nonHighlightsSkipped;
	}

	@Override
	public String toString() {
		return "FileUploadResponse [segmentsProcessed=" + segmentsProcessed + ", newHighlightsAdded=" + newHighlightsAdded + ", nonHighlightsSkipped=" + nonHighlightsSkipped + ", duplicateHighlightsSkipped=" + duplicateHighlightsSkipped + ", errorHighlightsSkipped=" + errorHighlightsSkipped + "]";
	}
	
	

}
