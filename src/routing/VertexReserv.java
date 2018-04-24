package routing;

public class VertexReserv {
	@SuppressWarnings("unused")
	private int id, route, reserv, vID, pvID;
	private double[] interval;

	public VertexReserv(int aircraftID, double[] interval, int routeID, int reservationID, int vertexID, int previousVertexID) {
		this.interval = interval;
		id = aircraftID;
		route = routeID;
		reserv = reservationID;
		vID = vertexID;
		pvID = previousVertexID;
	}

	public void changeTime(double[] newInterval) {
		interval = newInterval;
	}

	public int getAirplaneID() {
		return id;
	}

	public double getStartTime() {
		return interval[0];
	}

	public double getEndTime() {
		return interval[1];
	}
	
	public double[] getInterval() {
		return interval;
	}

	public double getTimeDiffernece() {
		return interval[1] - interval[0];
	}

	public int getRouteID() {
		return route;
	}

	public int getReservationID() {
		return reserv;
	}
	
	public int getPreviousVertexID() {
		return pvID;
	}
}
