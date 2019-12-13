package org.team1619.services.webdashboard;

import java.util.HashMap;

/**
 * UrlFormData is a utility to create, store, and export data in url-encoded form
 * The url-encoded form allow for easy transfer of data over a websocket
 *
 * @author Matthew Oates
 */

public class UrlFormData extends HashMap<String, String> {

	public UrlFormData() {
		this("");
	}

	public UrlFormData(String data) {
		for (String part : data.split("&")) {
			if (part.contains("=")) put(part.split("=")[0], part.split("=")[1]);
		}
	}

	public UrlFormData add(String key, String value) {
		put(key, value);
		return this;
	}

	public String getData() {
		StringBuilder data = new StringBuilder();

		for (HashMap.Entry<String, String> entry : entrySet()) {
			data.append(entry.getKey() + "=" + entry.getValue() + "&");
		}

		if (data.length() == 0) return "";

		return data.substring(0, data.length() - 1);
	}
}