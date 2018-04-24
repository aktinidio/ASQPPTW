package routing;

public class Reservation {
	private double[] interval;
	private boolean backwards;
	private Edge edge;
	private int airplaneID;

	public Reservation(Edge edge, double[] interval, int airplaneID, boolean backwards) {
		this.interval = interval;
		this.backwards = backwards;
		this.edge = edge;
		this.airplaneID = airplaneID;
	}

	public double getStartTime() {
		return interval[0];
	}

	public double getEndTime() {
		return interval[1];
	}

	public boolean isBackwards() {
		return backwards;
	}

	public Edge getEdge() {
		return edge;
	}

	public int getAirplaneID() {
		return airplaneID;
	}
}
