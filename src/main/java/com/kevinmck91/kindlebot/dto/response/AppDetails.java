package com.kevinmck91.kindlebot.dto.response;

public class AppDetails {

	private int id;
	private String name;
	private String rootUrl;
	private String icon;
	private boolean isAvailable;

	public AppDetails(int id, String name, String rootUrl, String icon, Boolean isAvailable) {
		super();
		this.id = id;
		this.name = name;
		this.rootUrl = rootUrl;
		this.icon = icon;
		this.isAvailable = isAvailable;
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

	public boolean isAvailable() {
		return isAvailable;
	}

	public void setAvailable(boolean isAvailable) {
		this.isAvailable = isAvailable;
	}

}
