package com.umutcanbolat.instantusernamesearchapi.controller;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ResourceLoader;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import com.umutcanbolat.instantusernamesearchapi.Model.ServiceModel;
import com.umutcanbolat.instantusernamesearchapi.Model.ServiceResponseModel;
import com.umutcanbolat.instantusernamesearchapi.Model.SiteModel;

@RestController
public class UserController {

	@Autowired
	private ResourceLoader resourceLoader;

	@RequestMapping("/check/{service}/{username}")
	public ServiceResponseModel searchUsername(@PathVariable String service, @PathVariable String username)
			throws FileNotFoundException, UnirestException {
		try {

			// read sites data from resources
			InputStream in = getClass().getResourceAsStream("/static/sites.json");
			BufferedReader reader = new BufferedReader(new InputStreamReader(in));

			// parse json to model list
			Gson gson = new Gson();
			JsonReader jReader = new JsonReader(reader);
			Type listType = new TypeToken<ArrayList<SiteModel>>() {
			}.getType();
			List<SiteModel> sitesList = gson.fromJson(reader, listType);

			for (SiteModel site : sitesList) {
				if (site.getService().toLowerCase().equals(service.toLowerCase())) {
					String url = site.getUrl().replace("{}", username);
					// set unirest not to follow redirects
//					Unirest.setHttpClient(
//							org.apache.http.impl.client.HttpClients.custom().disableRedirectHandling().build());
					HttpResponse<String> response = Unirest.get(url).header("Connection", "keep-alive")
							.header("Upgrade-Insecure-Requests", "1")
							.header("User-Agent",
									"Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/71.0.3578.98 Safari/537.36")
							.header("Accept",
									"text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,image/apng,*/*;q=0.8")
							.header("Accept-Encoding", "gzip, deflate").header("Accept-Language", "en-US;q=1")
							.asString();
					boolean available = false;
					if (response.getStatus() != 200) {
						System.out.println(service + "     " + response.getStatus());
						available = true;
					} else {
						// quick solution for hackernews
						if ("HackerNews".equals(site.getService())) {
							if ("No such user.".equals(response.getBody())) {
								available = true;
							}
						}

					}
					return new ServiceResponseModel(site.getService(), url, available);
				}
			}
			// service not found
			return new ServiceResponseModel("Service: " + service + " is not supported");
		} catch (Exception ex) {
			return new ServiceResponseModel(ex.getMessage());
		}
	}

	@RequestMapping("/services/getAll")
	public List<ServiceModel> getServicesList() {
		try {

			List<ServiceModel> serviceList = new ArrayList<ServiceModel>();

			// read sites data from resources
			InputStream in = getClass().getResourceAsStream("/static/sites.json");
			BufferedReader reader = new BufferedReader(new InputStreamReader(in));

			// parse json to model list
			Gson gson = new Gson();
			JsonReader jReader = new JsonReader(reader);
			Type listType = new TypeToken<ArrayList<SiteModel>>() {
			}.getType();
			List<SiteModel> sitesList = gson.fromJson(reader, listType);

			for (SiteModel site : sitesList) {
				serviceList.add(
						new ServiceModel(site.getService(), "/" + site.getService().toLowerCase() + "/{username}"));
			}
			return serviceList;
		} catch (Exception ex) {
			return null;
		}

	}
}
