package routing;

public class Window {

	@SuppressWarnings("unused")
	private Edge edge;
	private double startTime;
	private double endTime;

	public Window(Edge e, double sTime, double eTime) {
		edge = e;
		startTime = sTime;
		endTime = eTime;
	}

	public double getStartTime() {
		return startTime;
	}

	public double getEndTime() {
		return endTime;
	}

	public void setStartTime(double t) {
		startTime = t;
	}

	public void setEndTime(double t) {
		endTime = t;
	}
}