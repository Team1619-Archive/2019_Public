package org.team1619.services.webdashboard;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

import javax.annotation.Nullable;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;

public class WebHttpServer {

	@Nullable
	private HttpServer fHttpServer;

	public WebHttpServer(int port) {
		try {
			fHttpServer = HttpServer.create(new InetSocketAddress(port), 8);

			fHttpServer.createContext("/", (e) -> {
				fullWrite(e, readFile("webdashboard/webdashboard.html"));

				e.close();
			});

			fHttpServer.createContext("/pages", (e) -> {
				if (e.getRequestURI().getPath().contains("css") || e.getRequestURI().getPath().contains("ttf")) {
					e.getResponseHeaders().add("content-type", "text/css");
				}

				fullWrite(e, readFile(e.getRequestURI().getPath().split("/pages/", 2)[1]));

				e.close();
			});

			fHttpServer.createContext("/match", (e) -> {
				fullWrite(e, readFile("webdashboard/match/match.html"));

				e.close();
			});

			fHttpServer.createContext("/log", (e) -> {
				fullWrite(e, readFile("webdashboard/log/log.html"));

				e.close();
			});

			fHttpServer.start();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	//Reads entire file
	private static byte[] readFile(String path) {
		try {
			@Nullable
			InputStream fis = WebHttpServer.class.getClassLoader().getResourceAsStream(path);
			byte[] r = new byte[0];
			if (fis != null) {
				r = fis.readAllBytes();
				fis.read(r);
				fis.close();
			}

			return r;
		} catch (Exception e) {

		}

		return new byte[0];
	}

	//Writes entire file to HttpExchange
	private static void fullWrite(HttpExchange e, String r) {
		try {
			byte[] bytes = r.getBytes();
			e.sendResponseHeaders(200, bytes.length);
			OutputStream body = e.getResponseBody();
			body.write(bytes);
			body.flush();
			body.close();
		} catch (Exception e1) {

		}
	}

	//Writes entire file to HttpExchange
	private static void fullWrite(HttpExchange e, byte[] r) {
		fullWrite(e, new String(r));
	}
}
