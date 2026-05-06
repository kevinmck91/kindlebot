package com.kevinmck91.kindlebot.dto.response;

public class MagazineResponse {

	private String editionName;
	private String endpoint;
	private String magazineName;
	private String year;
	private String displayName;

	public MagazineResponse() {
		super();
	}

	public MagazineResponse(String editionName, String endpoint, String magazineName, String year, String displayName) {
		super();
		this.editionName = editionName;
		this.endpoint = endpoint;
		this.magazineName = magazineName;
		this.year = year;
		this.displayName = displayName;
	}

	public MagazineResponse(String magazineName) {
		this.magazineName = magazineName;
	}

	public String getEditionName() {
		return editionName;
	}

	public void setEditionName(String editionName) {
		this.editionName = editionName;
	}

	public String getEndpoint() {
		return endpoint;
	}

	public void setEndpoint(String endpoint) {
		this.endpoint = endpoint;
	}

	public String getMagazineName() {
		return magazineName;
	}

	public void setMagazineName(String magazineName) {
		this.magazineName = magazineName;
	}

	public String getYear() {
		return year;
	}

	public void setYear(String year) {
		this.year = year;
	}

	public String getDisplayName() {
		return displayName;
	}

	public void setDisplayName(String displayName) {
		this.displayName = displayName;
	}

}