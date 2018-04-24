package routing;

public class Delay {

	private int aircraftID;
	private double delay;

	public Delay(int aircraftID, double delay) {
		this.aircraftID = aircraftID;
		this.delay = delay;
	}

	public double getDelay() {
		return delay;
	}

	public int getAircraftID() {
		return aircraftID;
	}

	public void addDelay(double d) {
		delay += d;
	}
}
