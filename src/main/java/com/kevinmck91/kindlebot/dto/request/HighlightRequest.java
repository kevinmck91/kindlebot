package com.kevinmck91.kindlebot.dto.request;

import java.util.ArrayList;
import java.util.List;

public class HighlightRequest {

	private String title;
	private String author;
	private String excerpt;
	private String note;
	private String page;
	private String source;
	private List<String> highlightTags = new ArrayList<>();

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getAuthor() {
		return author;
	}

	public void setAuthor(String author) {
		this.author = author;
	}

	public String getExcerpt() {
		return excerpt;
	}

	public void setExcerpt(String excerpt) {
		this.excerpt = excerpt;
	}

	public String getNote() {
		return note;
	}

	public void setNote(String note) {
		this.note = note;
	}

	public List<String> getHighlightTags() {
		return highlightTags;
	}

	public void setHighlightTags(List<String> highlightTags) {
		this.highlightTags = highlightTags;
	}

	public String getSource() {
		return source;
	}

	public void setSource(String source) {
		this.source = source;
	}

	public String getPage() {
		return page;
	}

	public void setPage(String page) {
		this.page = page;
	}

}
