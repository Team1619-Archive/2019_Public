package org.team1619.utilities;

import java.util.HashMap;

/**
 * WebDashboardGraphDataset makes it very easy to make a set of points to send to the web webdashboard.
 * Once the dataset is completed, simply put it in shared input values as a vector with the prefix "gr_",
 * and it will be put on the graph page of the web webdashboard.
 *
 * @author Matthew Oates
 */

public class WebDashboardGraphDataset extends HashMap<String, Double> {

	int fNumPoints = 0;

	public WebDashboardGraphDataset addPoint(double x, double y) {
		put(fNumPoints + "x", x);
		put(fNumPoints + "y", y);
		fNumPoints++;

		return this;
	}
}
