package routing;

public class Label implements Comparable<Label> {

	public int labelNodePos, aircraftId, predNodePos, labelListPredPos;
	public double[] interval;
	private double cost;
	public boolean activ, backwards;

	public Label(int labelNodePos, double startTime, double endTime, int aircraftId, int predNodePos, int labelPredPos, boolean backwards) {
		this.labelNodePos = labelNodePos;
		this.interval = new double[] { startTime, endTime };
		this.predNodePos = predNodePos;
		this.aircraftId = aircraftId;
		this.labelListPredPos = labelPredPos;
		this.activ = true;
		this.backwards = backwards;
	}

	public Label(int labelNodePos, double cost, double startTime, double endTime, int aircraftId, int predNodePos, int labelPredPos, boolean backwards) {
		this.labelNodePos = labelNodePos;
		this.cost = cost;
		this.interval = new double[] { startTime, endTime };
		this.predNodePos = predNodePos;
		this.aircraftId = aircraftId;
		this.labelListPredPos = labelPredPos;
		this.activ = true;
		this.backwards = backwards;
	}

	public int compareTo(Label other) {
		return (int) (cost - other.cost);
	}

	public void setCost(double c) {
		cost = c;
	}
}