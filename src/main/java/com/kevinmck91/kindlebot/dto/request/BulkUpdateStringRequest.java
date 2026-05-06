package com.kevinmck91.kindlebot.dto.request;

import java.util.List;

public class BulkUpdateStringRequest {

	private List<String> timestampHashes;
	private String newString;

	public BulkUpdateStringRequest() {
		super();
	}

	public BulkUpdateStringRequest(List<String> timestampHashes, String newString) {
		super();
		this.timestampHashes = timestampHashes;
		this.newString = newString;
	}

	public List<String> getTimestampHashes() {
		return timestampHashes;
	}

	public void setTimestampHashes(List<String> timestampHashes) {
		this.timestampHashes = timestampHashes;
	}

	public String getNewString() {
		return newString;
	}

	public void setNewString(String newString) {
		this.newString = newString;
	}

}