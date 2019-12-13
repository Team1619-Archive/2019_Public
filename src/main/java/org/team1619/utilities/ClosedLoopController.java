package org.team1619.utilities;

import org.team1619.models.exceptions.ConfigurationException;
import org.team1619.models.exceptions.ConfigurationInvalidTypeException;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;
import org.team1619.utilities.logging.LogManager;
import org.team1619.utilities.logging.Logger;

public class ClosedLoopController {

	private static final Map<String, Double> sProfileDefaults = new HashMap<>();

	static {
		sProfileDefaults.put("integral_range", -1.0);
		sProfileDefaults.put("max_integral", -1.0);
		sProfileDefaults.put("idle_output", 0.0);
	}

	public static class Profile {

		public String fName;

		public double f, p, i, d;
		public double maxIntegral, integralRange;
		public double maxOutput;
		public double idleOutput;

		public double kv, ka;
		public double kForceCompensation;


		public boolean hasFeedForward;
		public boolean hasForceCompensation;

		public Profile(String name, Map<String, Double> values) {
			fName = name;
			if (values.containsKey("f") && values.containsKey("p") && values.containsKey("i") && values.containsKey("d") &&
					values.containsKey("max_integral") && values.containsKey("integral_range") && values.containsKey("max_output") && values.containsKey("idle_output")) {
				this.f = values.get("f");
				this.p = values.get("p");
				this.i = values.get("i");
				this.d = values.get("d");

				this.maxIntegral = values.get("max_integral");
				this.integralRange = values.get("integral_range");
				this.maxOutput = values.get("max_output");
				this.idleOutput = values.get("idle_output");

				if (values.containsKey("ka") && values.containsKey("kv")) {
					hasFeedForward = true;
					this.ka = values.get("ka");
					this.kv = values.get("kv");
				}

				if (values.containsKey("force_compensation")) {
					this.hasForceCompensation = true;
					this.kForceCompensation = values.get("force_compensation");

				}
			} else {
				throw new ConfigurationException("Must provide value for 'f', 'p', 'i', 'd', 'max_output'");
			}
		}
	}

	private static final Logger sLogger = LogManager.getLogger(ClosedLoopController.class);

	private final YamlConfigParser fYamlConfigParser;
	private final String fType;
	private final String fName;
	private final Map<String, ClosedLoopController.Profile> fProfiles = new HashMap<>();
	@Nullable
	private Profile fCurrentProfile;

	private double fP, fI, fD;
	private double fSetpoint = 0.0;
	private double fIntegral = 0.0;
	private double fPreviousError = 0.0;

	private long fPreviousTime = -1;

	public ClosedLoopController(String name) {
		fName = name;
		fYamlConfigParser = new YamlConfigParser();
		fYamlConfigParser.loadWithFolderName("closed-loop-profiles.yaml");

		Config config = fYamlConfigParser.getConfig(name);
		fType = config.getType();
		Object configProfiles = config.get("profiles");

		if (!(configProfiles instanceof Map)) {
			throw new ConfigurationInvalidTypeException("Map", "profiles", configProfiles);
		}

		Map<String, Map<String, Double>> pidProfiles = (Map<String, Map<String, Double>>) configProfiles;

		for (Map.Entry<String, Map<String, Double>> profile : pidProfiles.entrySet()) {
			Map<String, Double> PIDValues = new HashMap<>(sProfileDefaults);
			for (Map.Entry<String, Double> parameter : pidProfiles.get(profile.getKey()).entrySet()) {
				PIDValues.put(parameter.getKey(), parameter.getValue());
			}
			fProfiles.put(profile.getKey(), new ClosedLoopController.Profile(profile.getKey(), PIDValues));
		}

	}

	public void setProfile(String name) {
		if (!fProfiles.containsKey(name)) {
			throw new RuntimeException(fName + "does not contain a profile named '" + name + "'");
		}
		fCurrentProfile = fProfiles.get(name);
	}

	public void set(double setpoint) {
		fSetpoint = setpoint;
	}

	public void reset() {
		fIntegral = 0.0;
		fPreviousError = 0.0;
		fPreviousTime = -1;
	}

	public double getSetpoint() {
		return fSetpoint;
	}

	public double getIntegral() {
		return fIntegral;
	}

	public double getError(double measuredValue) {
		return fSetpoint - measuredValue;
	}

	public double getWithPID(double measuredValue) {
		assert fCurrentProfile != null;
		long time = System.currentTimeMillis();

		if (fPreviousTime == -1) {
			fPreviousTime = time;
		}

		double deltaTime = (time - this.fPreviousTime) / 1000.0;

		double error = fSetpoint - measuredValue;

		boolean insideIntegralRange = (fCurrentProfile.integralRange == -1 || Math.abs(error) <= fCurrentProfile.integralRange);

		if (insideIntegralRange) {
			fIntegral += deltaTime * error;

			if (fCurrentProfile.maxIntegral != -1) {
				if (fIntegral < 0.0) {
					fIntegral = Math.max(fIntegral, -fCurrentProfile.maxIntegral);
				} else {
					fIntegral = Math.min(fIntegral, fCurrentProfile.maxIntegral);
				}
			}
		}

		double deltaError = error - fPreviousError;
		double derivative = deltaTime != 0.0 ? deltaError / deltaTime : Double.MAX_VALUE;

		if (derivative == Double.MAX_VALUE) {
			//	sLogger.warn("Derivative is at max value (no delta time) and will be multiplied by {}", fCurrentProfile.d);
		}

		fP = fCurrentProfile.p * error;
		fI = fCurrentProfile.i * this.fIntegral;
		fD = fCurrentProfile.d * derivative;

		double output = fCurrentProfile.f * fSetpoint + fP + fI + fD + fCurrentProfile.idleOutput;

		fPreviousTime = time;
		fPreviousError = error;

		if (fCurrentProfile.fName.equals("down")) {
//			SmartDashboard.putNumber("error", error);
//			SmartDashboard.putNumber("p", fP);
//			SmartDashboard.putNumber("i", fI);
//			SmartDashboard.putNumber("d", fD);
//			SmartDashboard.putNumber("setpoint", fSetpoint);
		}

		if (fCurrentProfile.hasForceCompensation) {
			return (output < 0 ? -1 : 1) * Math.min(Math.abs(output), fCurrentProfile.maxOutput) + (fCurrentProfile.kForceCompensation * Math.sin(measuredValue));
		} else {
			return (output < 0 ? -1 : 1) * Math.min(Math.abs(output), fCurrentProfile.maxOutput);
		}
	}

	public double get(double measuredValue, double acceleration) {
		assert fCurrentProfile != null;
		if (!fCurrentProfile.hasFeedForward) {
			throw new RuntimeException("The profile provided must include feedforward constants 'ka' and 'kv'");
		}

		double pidValue = getWithPID(measuredValue);
		return fCurrentProfile.kv * getSetpoint() + fCurrentProfile.ka * acceleration + pidValue;
	}

}