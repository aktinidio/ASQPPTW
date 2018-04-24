package routing;


public class ChristofasFeedback {

	private final int[] ids;
	private final double cost;

	public ChristofasFeedback(int[] aircraftIDs, double cost) {
		ids = aircraftIDs;
		this.cost = cost;
	}

	public int[] getIDs() {
		return ids;
	}

	public double getCost() {
		return cost;
	}
}
