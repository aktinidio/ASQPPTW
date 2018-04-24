package routing;

public class Flight {
	private int id, source, target, pushbackTime;
	private double time, minimumRoutingTime;
	private boolean arrival;

	public Flight(int id, int source, int target, double time, boolean arrival, int pushTime, double minimumRouting) {
		this.id = id;
		this.source = source;
		this.target = target;
		this.time = time;
		this.arrival = arrival;
		this.pushbackTime = pushTime;
		this.minimumRoutingTime = minimumRouting;
	}
	
	public int getId(){
		return id;
	}
	
	public int getSource(){
		return source;
	}
	
	public int getTarget(){
		return target;
	}
	
	public double getTime(){
		return time;
	}
	
	public boolean isArrival(){
		return arrival;
	}
	
	public int getPushbackTime(){
		return pushbackTime;
	}
	
	public double getMinimumRoutingTime(){
		return minimumRoutingTime;
	}

	public void changeID(int newID) {
		id = newID;		
	}
}
