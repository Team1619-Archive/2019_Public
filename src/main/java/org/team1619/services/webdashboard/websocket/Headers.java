package org.team1619.services.webdashboard.websocket;

import java.util.HashMap;

public class Headers extends HashMap<String, String> {

	private final String fLine;

	public Headers(String line) {
		fLine = line;
	}

	public Headers() {
		this("");
	}

	public void putHeader(String line) {
		String[] parts = line.split(": ");
		if (parts.length < 2) return;
		put(parts[0], parts[1]);
	}

	public String getHeaderText() {
		StringBuilder headerText = new StringBuilder();

		if (!fLine.equals("")) {
			headerText.append(fLine).append("\r\n");
		}

		for (HashMap.Entry<String, String> header : this.entrySet()) {
			headerText.append(header.getKey()).append(": ").append(header.getValue()).append("\r\n");
		}

		headerText.append("\r\n");

		return headerText.toString();
	}
}
