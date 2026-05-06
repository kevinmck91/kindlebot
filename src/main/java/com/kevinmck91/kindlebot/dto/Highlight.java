package com.kevinmck91.kindlebot.dto;

import java.util.ArrayList;
import java.util.List;

public class Highlight {

	private String timestampHash;
	private String highlightHeader;
	private String title;
	private String author;
	private String dateString;
	private String excerpt;
	private String page;
	private String location;
	private String visibility;
	private String note;
	private String source;
	private List<String> highlightTags = new ArrayList<>();
	private String highlightTagsString;

	public String getTimestampHash() {
		return timestampHash;
	}

	public void setTimestampHash(String timestampHash) {
		this.timestampHash = timestampHash;
	}

	public String getHighlightHeader() {
		return highlightHeader;
	}

	public void setHighlightHeader(String highlightHeader) {
		this.highlightHeader = highlightHeader;
	}

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

	public String getDateString() {
		return dateString;
	}

	public void setDateString(String dateString) {
		this.dateString = dateString;
	}

	public String getExcerpt() {
		return excerpt;
	}

	public void setExcerpt(String excerpt) {
		this.excerpt = excerpt;
	}

	public String getPage() {
		return page;
	}

	public void setPage(String page) {
		this.page = page;
	}

	public String getLocation() {
		return location;
	}

	public void setLocation(String location) {
		this.location = location;
	}

	public String getVisibility() {
		return visibility;
	}

	public void setVisibility(String visibility) {
		this.visibility = visibility;
	}

	public String getNote() {
		return note;
	}

	public void setNote(String note) {
		this.note = note;
	}

	public String getSource() {
		return source;
	}

	public void setSource(String source) {
		this.source = source;
	}

	public List<String> getHighlightTags() {
		return highlightTags;
	}

	public void setHighlightTags(List<String> highlightTags) {
		this.highlightTags = highlightTags;
	}

	public String getHighlightTagsString() {
		return highlightTagsString;
	}

	public void setHighlightTagsString(String highlightTagsString) {
		this.highlightTagsString = highlightTagsString;
	}

}
