package org.team1619.services.webdashboard.websocket;

import javax.annotation.Nullable;
import java.io.*;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.security.MessageDigest;
import java.util.Base64;

public class WebSocket {

	private final Socket fSocket;
	private final InputStream fInput;
	private final BufferedReader fReader;
	private final OutputStream fOutput;
	private final InetAddress fAddress;
	private final AbstractWebsocketServer fServer;
	@Nullable
	private String fPath;
	@Nullable
	private Headers fHeaders;

	protected WebSocket(Socket socket, AbstractWebsocketServer server) throws Exception {
		fSocket = socket;
		fSocket.setSoTimeout(1);

		fInput = socket.getInputStream();
		fReader = new BufferedReader(new InputStreamReader(fInput));
		fOutput = socket.getOutputStream();
		fAddress = socket.getInetAddress();

		fServer = server;

		handshake();

		fServer.onopen(this);
	}

	private void handshake() throws Exception {
		while (!fReader.ready()) ;

		fHeaders = new Headers(fReader.readLine().trim());

		while (fReader.ready()) {
			String line = fReader.readLine().trim();
			fHeaders.putHeader(line);
		}

		if (fHeaders.containsKey("Sec-WebSocket-Key")) {
			Headers responseHeaders = new Headers("HTTP/1.1 101 Switching Protocols");

			responseHeaders.put("Connection", "Upgrade");
			responseHeaders.put("Upgrade", "websocket");
			responseHeaders.put("Sec-WebSocket-Accept", Base64.getEncoder().encodeToString(MessageDigest.getInstance("SHA-1").digest((fHeaders.get("Sec-WebSocket-Key") + "258EAFA5-E914-47DA-95CA-C5AB0DC85B11").getBytes("UTF-8"))));

			write(responseHeaders.getHeaderText().getBytes());
		}

		fPath = fHeaders.getHeaderText().split(" ")[1];
	}

	public void send(String message) {

		byte[] messageBytes = message.getBytes();

		byte[] bytes;

		if (message.length() < 126) {
			bytes = new byte[2 + message.length()];

			bytes[0] = (byte) 129;
			bytes[1] = (byte) message.length();

			for (int b = 0; b < messageBytes.length; b++) {
				bytes[b + 2] = messageBytes[b];
			}
		} else {
			bytes = new byte[4 + message.length()];

			bytes[0] = (byte) 129;
			bytes[1] = (byte) 126;

			bytes[2] = (byte) ((message.length() >> 8) & 0xFF);
			bytes[3] = (byte) (message.length() & 0xFF);

			for (int b = 0; b < messageBytes.length; b++) {
				bytes[b + 4] = messageBytes[b];
			}
		}

		try {
			write(bytes);
		} catch (Exception e) {

		}
	}

	public String getPath() {
		if (fPath == null) {
			return "";
		}
		return fPath;
	}

	private void write(byte[] message) throws IOException {
		fOutput.write(message);
		fOutput.flush();
	}

	protected void update() {
		try {
			@Nullable
			String message = read();

			if (message != null) {
				if (message.equals("keepalive")) {
					send("keepalive");
					return;
				}

				for (byte b : message.getBytes()) {
					if (b < 0) {
						fServer.onclose(this);
						return;
					}
				}

				fServer.onmessage(this, message);
			}
		} catch (Exception e) {
			fServer.onclose(this);
		}
	}

	protected void close() {
		byte[] bytes = new byte[]{(byte) 1000, (byte) 0};

		try {
			write(bytes);
		} catch (IOException e) {

		}
	}

	@Nullable
	private String read() throws Exception {
		try {
			byte[] control = new byte[2];

			fInput.read(control);

			int textLength = (control[1] & 0xFF) - 128;

			if (textLength > 125) {
				byte[] length = new byte[2];
				fInput.read(length);
				textLength = ((length[0] & 0xFF) << 8) | (length[1] & 0xFF);
			}

			byte[] key = new byte[4];
			fInput.read(key);

			byte[] encoded = new byte[textLength];
			fInput.read(encoded);

			byte[] decoded = new byte[textLength];
			for (int i = 0; i < textLength; i++) {
				decoded[i] = (byte) (encoded[i] ^ key[i & 0x3]);
			}

			return new String(decoded);

		} catch (SocketTimeoutException e) {

		} catch (Exception e) {
			throw e;
		}

		return null;
	}
}