package org.team1619.services.sim;

import com.google.common.primitives.Doubles;
import com.google.common.util.concurrent.AbstractExecutionThreadService;
import org.team1619.utilities.injection.Inject;
import org.team1619.utilities.logging.LogManager;
import org.team1619.utilities.logging.Logger;
import org.team1619.events.sim.SimBooleanInputSetEvent;
import org.team1619.events.sim.SimNumericInputSetEvent;
import org.team1619.shared.abstractions.EventBus;
import org.team1619.shared.abstractions.FMS;

import javax.annotation.Nullable;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Arrays;
import java.util.List;

public class SimInputSocketListenerService extends AbstractExecutionThreadService {

	private static final Logger sLogger = LogManager.getLogger(SimInputSocketListenerService.class);

	private final EventBus fEventBus;
	private final FMS fFMS;

	private ServerSocket fSocket;

	@Inject
	public SimInputSocketListenerService(EventBus eventBus, FMS fms) throws IOException {
		fEventBus = eventBus;
		fFMS = fms;

		fSocket = new ServerSocket();
	}

	@Override
	protected void startUp() throws Exception {
		sLogger.info("Starting SimInputSocketListenerService");
		fSocket.bind(new InetSocketAddress("0.0.0.0", 11619));
		sLogger.info("SimInputSocketListenerService started");
	}

	@Override
	protected void run() throws Exception {
		while (true) {
			Socket socket = fSocket.accept();
			BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));

			while (true) {
				@Nullable String line = reader.readLine();
				if (line == null) {
					break;
				}

				sLogger.debug("Received event: '" + line + "'");

				String[] args = line.split(" ");
				// <type> <input or fms_mode> [option]
				switch (args[0]) {
					case "input":
						switch (args[1]) {
							case "b": {
								String name = args[2];
								boolean value = Boolean.valueOf(args[3]);
								fEventBus.post(new SimBooleanInputSetEvent(name, value));
								break;
							}
							case "n": {
								String name = args[2];
								double value = Double.valueOf(args[3]);
								fEventBus.post(new SimNumericInputSetEvent(name, value));
								break;
							}
							case "v": {
								String name = args[2];
								String[] data = args[3].split(",");
								List<Double> values = Doubles.asList(Arrays.stream(data).mapToDouble(Double::parseDouble).toArray());
								//fEventBus.post(new SimVectorInputSetEvent(name, values));
								break;
							}
						}
						break;

					case "fms_mode":
						switch (args[1]) {
							case "auto":
								fFMS.setMode(FMS.Mode.AUTONOMOUS);
								break;
							case "teleop":
								fFMS.setMode(FMS.Mode.TELEOP);
								break;
							case "disabled":
								fFMS.setMode(FMS.Mode.DISABLED);
								break;
							case "test":
								fFMS.setMode(FMS.Mode.TEST);
								break;
						}
						break;
				}
			}
		}
	}
}
