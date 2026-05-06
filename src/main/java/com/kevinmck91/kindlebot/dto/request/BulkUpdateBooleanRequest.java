package com.kevinmck91.kindlebot.dto.request;

import java.util.List;

public class BulkUpdateBooleanRequest {

	private List<String> timestampHashes;
	private Boolean newBoolean;
	

	public BulkUpdateBooleanRequest() {
		super();
	}

	public BulkUpdateBooleanRequest(List<String> timestampHashes, Boolean newBoolean) {
		super();
		this.timestampHashes = timestampHashes;
		this.newBoolean = newBoolean;
	}

	public List<String> getTimestampHashes() {
		return timestampHashes;
	}

	public void setTimestampHashes(List<String> timestampHashes) {
		this.timestampHashes = timestampHashes;
	}

	public Boolean getNewBoolean() {
		return newBoolean;
	}

	public void setNewBoolean(Boolean newBoolean) {
		this.newBoolean = newBoolean;
	}

}