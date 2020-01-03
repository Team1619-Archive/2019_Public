package org.team1619.services.webdashboard;

import org.team1619.services.webdashboard.websocket.AbstractWebsocketServer;
import org.team1619.services.webdashboard.websocket.WebSocket;
import org.team1619.utilities.LimitedSizeQueue;
import org.team1619.utilities.logging.LogHandler;
import org.team1619.utilities.logging.LogManager;
import org.team1619.utilities.logging.Logger;
import org.team1619.events.sim.SimBooleanInputSetEvent;
import org.team1619.events.sim.SimNumericInputSetEvent;
import org.team1619.events.sim.SimVectorInputSetEvent;
import org.team1619.shared.abstractions.*;

import javax.annotation.Nullable;
import java.util.*;

/**
 * WebsocketServer connects and communicates with the computer webdashboard server
 *
 * @author Matthew Oates
 */

public class WebsocketServer extends AbstractWebsocketServer implements LogHandler {

	private static final Logger sLogger = LogManager.getLogger(WebsocketServer.class);

	private ArrayList<String> autoOriginList = new ArrayList<>(Arrays.asList("Left", "Center", "Right"));
	private ArrayList<String> autoDestinationList = new ArrayList<>(Arrays.asList("Rocket Left", "Cargo Ship Left", "Cargo Ship Right", "Rocket Right"));
	private ArrayList<String> autoActionList = new ArrayList<>(Arrays.asList("Front Low", "Back Low", "Double Low", "Double Mid"));

	private final EventBus fEventBus;
	private final FMS fFMS;
	private final InputValues fSharedInputValues;
	private final OutputValues fSharedOutputValues;
	private final RobotConfiguration fRobotConfiguration;

	//Web sockets
	//Connects web page

	private final Set<WebSocket> fWebDashboardSockets = new HashSet<>();
	private final Set<WebSocket> fValuesSockets = new HashSet<>();
	private final Set<WebSocket> fMatchSockets = new HashSet<>();
	private final Set<WebSocket> fLogSockets = new HashSet<>();

	private Map<String, Double> fLastNumerics = new HashMap<>();
	private Map<String, Boolean> fLastBooleans = new HashMap<>();
	private Map<String, String> fLastStrings = new HashMap<>();
	private Map<String, Map<String, Double>> fLastVectors = new HashMap<>();
	private Map<String, Object> fLastOutputs = new HashMap<>();
	private Map<String, Map<String, Object>> fMatchValues = new HashMap<>();
	private final Map<String, Object> fLastMatchValues = new HashMap<>();
	private Queue<Map<String, String>> fLogMessages = new LimitedSizeQueue<>(100);

	public WebsocketServer(int port, EventBus eventBus, FMS fms, InputValues inputValues, OutputValues outputValues, RobotConfiguration robotConfiguration) {
		super(port);

		fEventBus = eventBus;
		fFMS = fms;
		fSharedInputValues = inputValues;
		fSharedOutputValues = outputValues;
		fRobotConfiguration = robotConfiguration;

		fSharedInputValues.setString("si_selected_auto", "No Auto");

		LogManager.addLogHandler(this);
	}

	public void initialize() {
		Object config = fRobotConfiguration.getCategory("web_dashboard_match_values");
		if (config instanceof HashMap) {
			fMatchValues = (Map<String, Map<String, Object>>) config;
		}

		start();
	}

	//Puts a log message into the cue to be sent to the dashboard
	public void log(String type, String message) {
		fLogMessages.add(Map.of("type", type, "message", message));
	}

	//Called by the service every frame
	public void broadcastToWebDashboard() {
		broadcastValuesDataToWebDashboard();

		broadcastMatchDataToWebDashboard();

		broadcastLogDataToWebDashboard();
	}

	//Send information for the values page
	private void broadcastValuesDataToWebDashboard() {
		if (fValuesSockets.isEmpty()) return;

		StringBuilder values = new StringBuilder();

		Map<String, Double> numerics = new HashMap<>();
		numerics.putAll(fSharedInputValues.getAllNumerics());
		for (HashMap.Entry<String, Double> value : numerics.entrySet()) {
			if (!value.getValue().equals(fLastNumerics.get(value.getKey()))) {
				values.append("numeric*").append(value.getKey()).append("*").append(String.format("%6f", value.getValue())).append("~");
			}
		}
		fLastNumerics = numerics;

		Map<String, Boolean> booleans = new HashMap<>();
		booleans.putAll(fSharedInputValues.getAllBooleans());
		for (HashMap.Entry<String, Boolean> value : booleans.entrySet()) {
			if (!value.getValue().equals(fLastBooleans.get(value.getKey()))) {
				values.append("boolean*").append(value.getKey()).append("*").append(value.getValue()).append("~");
			}
		}
		fLastBooleans = booleans;

		Map<String, String> strings = new HashMap<>();
		strings.putAll(fSharedInputValues.getAllStrings());
		for (HashMap.Entry<String, String> value : strings.entrySet()) {
			if (!value.getValue().equals(fLastStrings.get(value.getKey()))) {
				values.append("string*").append(value.getKey()).append("*").append(value.getValue()).append("~");
			}
		}
		fLastStrings = strings;

		Map<String, Map<String, Double>> vectors = new HashMap<>();
		vectors.putAll(fSharedInputValues.getAllVectors());
		for (HashMap.Entry<String, Map<String, Double>> value : vectors.entrySet()) {
			if (!value.getValue().equals(fLastVectors.get(value.getKey()))) {
				values.append("vector*").append(value.getKey());
				for (Map.Entry<String, Double> v : value.getValue().entrySet()) {
					values.append("*").append(v.getKey()).append(": ").append(v.getValue());
				}
				values.append("~");
			}
		}
		fLastVectors = vectors;

		Map<String, Object> outputs = new HashMap<>();
		outputs.putAll(fSharedOutputValues.getAllOutputs());
		for (HashMap.Entry<String, Object> value : outputs.entrySet()) {
			if (!value.getValue().equals(fLastOutputs.get(value.getKey()))) {
				values.append("output*").append(value.getKey()).append("*").append(value.getValue()).append("~");
			}
		}
		fLastOutputs = outputs;

		if (values.length() > 0) {

			send(fValuesSockets, new UrlFormData()
					.add("response", "values")
					.add("values", values.substring(0, values.length() - 1))
					.getData());
		}
	}

	//Send information for the match web page
	private void broadcastMatchDataToWebDashboard() {
		if (fMatchSockets.isEmpty()) return;

		Map<String, Object> allValues = new HashMap<>();
		allValues.putAll(fSharedInputValues.getAllNumerics());
		allValues.putAll(fSharedInputValues.getAllBooleans());
		allValues.putAll(fSharedInputValues.getAllStrings());
		allValues.putAll(fSharedOutputValues.getAllOutputs());

		StringBuilder values = new StringBuilder();

		for(HashMap.Entry<String, Map<String, Object>> matchValue : fMatchValues.entrySet()) {
			String type = matchValue.getValue().get("type").toString();

			String name = matchValue.getKey();
			if(matchValue.getValue().containsKey("display_name")) {
				name = String.valueOf(matchValue.getValue().get("display_name"));
			}

			String value = "";
			if(allValues.containsKey(matchValue.getKey())) {
				value = String.valueOf(allValues.get(matchValue.getKey()));
			} else {
				if (type.equals("value") || type.equals("boolean")) {
					value = "";
				} else if (type.equals("dial")) {
					value = "0";
				}
			}

			String min = "0";
			if(matchValue.getValue().containsKey("min")) {
				min = String.valueOf(matchValue.getValue().get("min"));
			}

			String max = "10";
			if(matchValue.getValue().containsKey("max")) {
				max = String.valueOf(matchValue.getValue().get("max"));
			}

			if(!(fLastMatchValues.containsKey(matchValue.getKey()) && fLastMatchValues.get(matchValue.getKey()).equals(value))) {
				if (type.equals("value") || type.equals("boolean")) {
					values.append(type).append("*").append(name).append("*").append(value).append("~");
				} else if (type.equals("dial")) {
					values.append(type).append("*").append(name).append("*").append(value).append("*").append(min).append("*").append(max).append("~");
				}
			}

			fLastMatchValues.put(matchValue.getKey(), value);
		}

		if (values.length() > 0) {
			send(fMatchSockets, new UrlFormData()
					.add("response", "match_values")
					.add("values", values.substring(0, values.length() - 1))
					.getData());
		}
	}

	//Sends information for the log web page
	private void broadcastLogDataToWebDashboard() {
		if(fLogSockets.isEmpty()) {
			return;
		}

		while(!fLogMessages.isEmpty()) {
			Map<String, String> data = fLogMessages.remove();
			send(fLogSockets, new UrlFormData()
					.add("response", "log")
					.add("type", data.get("type"))
					.add("message", data.get("message"))
					.getData());
		}
	}

	private String listToUrlFormDataList(List list) {
		String urlFormDataList = "";

		for (Object item : list) {
			urlFormDataList += item.toString() + "~";
		}
		if (urlFormDataList.length() > 0) {
			urlFormDataList = urlFormDataList.substring(0, urlFormDataList.length() - 1);
		}

		return urlFormDataList;
	}

	//Called when a new websocket connection opens
	@Override
	public void onOpen(WebSocket socket) {
		try {
			switch (socket.getPath()) {
					case "/webdashboard": {
						fWebDashboardSockets.add(socket);

						sendAutoData();

						sendConnected();

						break;
					}
					case "/values": {
						fValuesSockets.add(socket);

						clearAllValues();

						break;
					}
					case "/match": {
						fMatchSockets.add(socket);

						sendAutoData();

						clearMatchValues();
						break;
					}
					case "/log": {
						fLogSockets.add(socket);
						break;
					}
				}
		} catch (Exception e) {
			onError(socket, e);
		}
	}

	//Called when a websocket connection closes
	@Override
	public void onClose(WebSocket webSocket) {
		removeSocket(webSocket);
	}

	//All messages received over websocket connections come here to be forwarded to the robot or web page
	@Override
	public void onMessage(WebSocket webSocket, String message) {

		UrlFormData data = new UrlFormData(message);

		if (data.containsKey("request")) {
			switch (data.get("request")) {
				case "all_values": {
					clearAllValues();
					break;
				}
				case "all_match_values": {
					clearMatchValues();
					break;
				}
				case "change_value": {
					switch (data.get("type")) {
						case "numeric":
							fEventBus.post(new SimNumericInputSetEvent(data.get("name"), Double.valueOf(data.get("value"))));
							break;
						case "boolean":
							fEventBus.post(new SimBooleanInputSetEvent(data.get("name"), Boolean.valueOf(data.get("value"))));
							break;
						case "string":
							break;
						case "vector":
							Map<String, Double> vector = new HashMap<>();
							vector.putAll(fSharedInputValues.getVector(data.get("name")));
							vector.put(data.get("selected"), Double.valueOf(data.get("value")));
							fEventBus.post(new SimVectorInputSetEvent(data.get("name"), vector));
							break;
					}
					break;
				}
				case "get_auto_data": {
					sendAutoData();
					break;
				}
				case "set_auto_data": {
					fSharedInputValues.setString("si_auto_origin", data.get("auto_origin"));
					fSharedInputValues.setString("si_auto_destination", data.get("auto_destination"));
					fSharedInputValues.setString("si_auto_action", data.get("auto_action"));
					fSharedInputValues.setString("si_selected_auto",
							fSharedInputValues.getString("si_auto_origin") + " to " +
									fSharedInputValues.getString("si_auto_destination") + ", " +
									fSharedInputValues.getString("si_auto_action"));
					break;
				}
				case "set_fms_mode": {
					switch (data.get("mode")) {
						case "auto":
							fFMS.setMode(FMS.Mode.AUTONOMOUS);
							return;
						case "teleop":
							fFMS.setMode(FMS.Mode.TELEOP);
							return;
						case "disabled":
							fFMS.setMode(FMS.Mode.DISABLED);
							return;
						case "test":
							fFMS.setMode(FMS.Mode.TEST);
							return;
					}
				}
				default: {
					sLogger.info("Unknown data: " + data);
				}
			}
		} else {
			sLogger.info("Unknown message: " + message);
		}
	}

	//All websocket error are handled here
	@Override
	public void onError(@Nullable WebSocket webSocket, Exception e) {
		sLogger.error("{} exception on {}", e.getMessage(), webSocket);
		e.printStackTrace();
	}

	//Called when the server starts
	@Override
	public void onStart() {

	}

	//Sends robot connection status to the web page
	private synchronized void sendConnected() {
		String message = new UrlFormData()
				.add("response", "connected")
				.add("connected", "true")
				.getData();

		send(fWebDashboardSockets, message);
	}

	private void sendAutoData() {
		String response = new UrlFormData()
				.add("response", "auto_data")
				.add("auto_origin_list", listToUrlFormDataList(autoOriginList))
				.add("auto_destination_list", listToUrlFormDataList(autoDestinationList))
				.add("auto_action_list", listToUrlFormDataList(autoActionList))
				.getData();

		send(fWebDashboardSockets, response);
		send(fMatchSockets, response);
	}

	private void clearAllValues() {
		fLastNumerics = new HashMap<>();
		fLastBooleans = new HashMap<>();
		fLastStrings = new HashMap<>();
		fLastVectors = new HashMap<>();
		fLastOutputs = new HashMap<>();
	}

	private void clearMatchValues() {
		fLastMatchValues.clear();
	}

	private void send(Set<WebSocket> sockets, String message) {
		Set<WebSocket> sendSockets = new HashSet<>();

		sendSockets.addAll(sockets);

		sendSockets.forEach(socket -> {
			try {
				socket.send(message);
			} catch (Exception e) {
				sLogger.error(e);
			}
		});
	}

	private void removeSocket(WebSocket socket) {
		fWebDashboardSockets.remove(socket);
		fValuesSockets.remove(socket);
		fMatchSockets.remove(socket);
		fLogSockets.remove(socket);
	}

	// Call the log method with the correct message level
	@Override
	public void trace(String message) {
		log("TRACE", message);
	}

	@Override
	public void debug(String message) {
		log("DEBUG", message);
	}

	@Override
	public void info(String message) {
		log("INFO", message);
	}

	@Override
	public void error(String message) {
		log("ERROR", message);
	}
}
