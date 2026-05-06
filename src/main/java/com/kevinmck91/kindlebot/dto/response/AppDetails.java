package com.kevinmck91.kindlebot.dto.response;

public class AppDetails {

	private int id;
	private String name;
	private String rootUrl;

	public AppDetails(int id, String name, String rootUrl) {
		super();
		this.id = id;
		this.name = name;
		this.rootUrl = rootUrl;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getRootUrl() {
		return rootUrl;
	}

	public void setRootUrl(String rootUrl) {
		this.rootUrl = rootUrl;
	}

}
