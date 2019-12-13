package org.team1619.services.webdashboard.websocket;

import org.team1619.utilities.logging.LogManager;
import org.team1619.utilities.logging.Logger;

import javax.annotation.Nullable;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public abstract class AbstractWebsocketServer {

	private static final Logger sLogger = LogManager.getLogger(AbstractWebsocketServer.class);

	private final int fPort;
	@Nullable
	private ServerSocket fServer;

	private final Set<WebSocket> fSockets;
	private final Set<WebSocket> fCurrentSockets;

	public AbstractWebsocketServer(int port) {
		fPort = port;
		fSockets = Collections.synchronizedSet(new HashSet<>());
		fCurrentSockets = Collections.synchronizedSet(new HashSet<>());
	}

	public AbstractWebsocketServer() {
		this(80);
	}

	public void start() {
		try {
			fServer = new ServerSocket(fPort);
			fServer.setReuseAddress(true);
			fServer.setSoTimeout(1);
		} catch (Exception e) {
			onError(null, e);
		}

		execute(this::onStart);

		run();
	}

	private void run() {
		execute(() -> {
			Thread.currentThread().setName("WebSocketServer - Run");
			while (!Thread.currentThread().isInterrupted()) {
				try {
					@Nullable
					Socket connection = null;
					if (fServer != null) {
						connection = fServer.accept();
					}

					try {
						if (connection != null) {
							new WebSocket(connection, this);
						}
					} catch (Exception e) {
						execute(() -> onError(null, e));
					}
				} catch (SocketTimeoutException e) {

				} catch (Exception e) {
					execute(() -> onError(null, e));
				}

				try {
					fCurrentSockets.clear();
					fCurrentSockets.addAll(fSockets);

					for (WebSocket socket : fCurrentSockets) {
						socket.update();
					}
				} catch (Exception e) {
					execute(() -> onError(null, e));
				}
			}

			fCurrentSockets.clear();
			fCurrentSockets.addAll(fSockets);

			for (WebSocket socket : fCurrentSockets) {
				socket.close();
			}

			sLogger.info("Websocket server shutting down");
		});
	}

	protected void execute(Runnable run) {
		new Thread(run).start();
	}

	public abstract void onStart();

	protected final void onopen(WebSocket webSocket) {
		fSockets.add(webSocket);
		onOpen(webSocket);
	}

	public abstract void onOpen(WebSocket webSocket);

	protected final void onmessage(WebSocket webSocket, String message) {
		onMessage(webSocket, message);
	}

	public abstract void onMessage(WebSocket webSocket, String message);

	protected final void onclose(WebSocket webSocket) {
		fSockets.remove(webSocket);
		onClose(webSocket);
	}

	public abstract void onClose(WebSocket webSocket);

	protected final void onerror(WebSocket webSocket, Exception e) {
		onError(webSocket, e);
	}

	public abstract void onError(@Nullable WebSocket webSocket, Exception e);
}