package com.kevinmck91.kindlebot.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class GithubItem {

	@JsonProperty("")
	private String name;

	@JsonProperty("")
	private String path;

	@JsonProperty("")
	private String sha;

	@JsonProperty("")
	private String size;

	@JsonProperty("")
	private String url;

	@JsonProperty("")
	private String html_url;

	@JsonProperty("")
	private String git_url;

	@JsonProperty("")
	private String download_url;

	@JsonProperty("")
	private String type;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}

	public String getSha() {
		return sha;
	}

	public void setSha(String sha) {
		this.sha = sha;
	}

	public String getSize() {
		return size;
	}

	public void setSize(String size) {
		this.size = size;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public String getHtml_url() {
		return html_url;
	}

	public void setHtml_url(String html_url) {
		this.html_url = html_url;
	}

	public String getGit_url() {
		return git_url;
	}

	public void setGit_url(String git_url) {
		this.git_url = git_url;
	}

	public String getDownload_url() {
		return download_url;
	}

	public void setDownload_url(String download_url) {
		this.download_url = download_url;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

}