package com.kevinmck91.kindlebot.dto.response;

public class AppDetails {

	private int id;
	private String name;
	private String rootUrl;
	private String icon;

	public AppDetails(int id, String name, String rootUrl, String icon) {
		super();
		this.id = id;
		this.name = name;
		this.rootUrl = rootUrl;
		this.icon = icon;
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

	public String getIcon() {
		return icon;
	}

	public void setIcon(String icon) {
		this.icon = icon;
	}

}
