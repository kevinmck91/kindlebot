package com.kevinmck91.kindlebot.service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import com.kevinmck91.kindlebot.dto.GithubItem;
import com.kevinmck91.kindlebot.utils.GithubDownloader;

@Service
public class MagazineService {

	private static final Logger logger = LoggerFactory.getLogger(MagazineService.class);

	private final GithubDownloader githubDownloader;
	private final MagazineService self;

	// Constructor
	public MagazineService(GithubDownloader githubDownloader, @Lazy MagazineService self) {
		this.githubDownloader = githubDownloader;
		this.self = self;
	}

	public List<GithubItem> fetchData(String endpoint) throws IOException {

		// Call the endpoint to get the set of available Editions
		GithubItem[] githubItems = githubDownloader.fetchData(endpoint);

		List<GithubItem> githubItemsList = new ArrayList<GithubItem>();

		for (GithubItem githubItem : githubItems) {
			githubItemsList.add(githubItem);
		}

		return githubItemsList;
	}

	public List<GithubItem> fetchAllMagazines() {

		List<GithubItem> githubItems = new ArrayList<GithubItem>();

		GithubItem githubItem1 = new GithubItem();
		githubItem1.setName("NY");
		githubItem1.setUrl("https://api.github.com/repositories/769113642/contents/NY");
		githubItem1.setDescription("The New Yorker");

		GithubItem githubItem2 = new GithubItem();
		githubItem2.setName("TE");
		githubItem2.setUrl("https://api.github.com/repositories/737724859/contents/TE");
		githubItem2.setDescription("The Economist");

		GithubItem githubItem3 = new GithubItem();
		githubItem3.setName("TM");
		githubItem3.setUrl("https://api.github.com/repositories/769109109/contents/TM");
		githubItem3.setDescription("Time Magazine");

		githubItems.add(githubItem1);
		githubItems.add(githubItem2);
		githubItems.add(githubItem3);

		return githubItems;
	}

}
