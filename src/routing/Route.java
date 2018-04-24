package routing;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class Route {

	private ArrayList<Reservation> route = new ArrayList<Reservation>();
	private ArrayList<VertexReserv> nodeReserv = new ArrayList<VertexReserv>();
	//private ArrayList<FlightAllocation> badCombinations = new ArrayList<FlightAllocation>();
	private ArrayList<Delay> delays = new ArrayList<Delay>();
	private int id, startPos, endPos, pushback;
	private double assignedTime, minimumRoutingTime; //latestEndTime = 0, 
	private boolean arrival;

	public Route(int aircraftID, int source, int destination, double time, boolean arrival, int pushbackTime, double minimum) {
		id = aircraftID;
		startPos = source;
		endPos = destination;
		assignedTime = time;
		pushback = pushbackTime;
		this.arrival = arrival;
		minimumRoutingTime = minimum;
	}

	public int getID() {
		return id;
	}

	public void addReservation(Reservation r, boolean arrival) {
		route.add(r);
		r.getEdge().addReservation(r);
	}

	public void addSimpleReservation(Reservation r) {
		route.add(r);
	}

	public ArrayList<Reservation> getReservations() {
		return route;
	}

	public Reservation getReservation(int reservationNo) {
		return route.get(reservationNo);
	}
	
	public void addVertexReservation(VertexReserv reservation) {
		nodeReserv.add(reservation);
	}

	public ArrayList<VertexReserv> getVertexReservations() {
		return nodeReserv;
	}

	public VertexReserv getVertexReservation(int reservationNo) {
		return nodeReserv.get(reservationNo);
	}
	
	public void changeVertexReservation(double[] interval, int id) {
		 nodeReserv.get(id).changeTime(interval);
	}

	public double getTotalTime() {
		//return route.get(0).getEndTime() - assignedTime; //route.get(route.size() - 1).getStartTime();
		if (arrival)
			return getEndTime() - assignedTime;
		else
			return assignedTime - getStartTime();
	}

	public double getTotalTimeRev() {
		return route.get(route.size() - 1).getEndTime() - route.get(0).getStartTime();
	}
	
	public int getPushbackTime() {
		return pushback;
	}
	
	public double getMinimumRouting() {
		return minimumRoutingTime;
	}

	public int getSourcePos() {
		return startPos;
	}

	public int getDestinationPos() {
		return endPos;
	}

	public double getEndTime() {
		return (route.get(0).getEndTime() >  route.get(route.size() - 1).getEndTime() ? route.get(0).getEndTime() : route.get(route.size() - 1).getEndTime());
	}

	public double getStartTime() {
		return (route.get(route.size() - 1).getStartTime() < route.get(0).getStartTime() ? route.get(route.size() - 1).getStartTime() : route.get(0).getStartTime());
	}

	public boolean isArrival() {
		return arrival;
	}
	
	public int size() {
		return route.size();
	}

/*	public double getTotlaDelay() {
		double sum = 0;
		for (Delay d : delays)
			sum += d.getDelay();
		return sum;
	}

	public boolean isDelayed() {
		return !delays.isEmpty();
	}

	public void addBadCombination(ArrayList<Flight> flights, double delay) {
		badCombinations.add(new FlightAllocation(flights, delay));
	}

	public ArrayList<FlightAllocation> getBadCombinations() {
		return badCombinations;
	}

	public boolean hasBadCombinations() {
		return badCombinations.size() > 0;
	}
	
	public ArrayList<ChristofasFeedback> getFeedback() {
		ArrayList<ChristofasFeedback> temp = new ArrayList<ChristofasFeedback>();
		for (FlightAllocation allocation : badCombinations){
			temp.add(new ChristofasFeedback(allocation.getAllIDs(), allocation.getDelay()));
		}
		return temp;
	}

	public void updateLatestTime(double time) {
		if (time > latestEndTime)
			latestEndTime = time;
	}
	
	public double getLatestTime() {
		return getEndTime() > latestEndTime ? getEndTime() : latestEndTime;
	}
	
	public int getMostConflicted() {
		int aircraft = 0;
		double max = 0;
		for (Delay delay : delays)
			if (delay.getDelay() > max) {
				max = delay.getDelay();
				aircraft = delay.getAircraftID();
			}
		return aircraft;
	}

	public int[] getAllDelays() {
		int[] temp = new int[delays.size()];
		for (int i = 0; i < delays.size(); i++)
			temp[i] = delays.get(i).getAircraftID();
		return temp;
	}

	public boolean checkPrevious(int id) {
		for (Delay d : delays)
			if (d.getAircraftID() != id)
				return false;
		return true;
	}

	public int getDelaysNo() {
		return delays.size();
	}

	public void checkForPushbackDelays(double time, boolean arrival) {
		
		int last = route.size() - 1;

		if ((!arrival && route.get(last).getStartTime() > time) || (arrival && route.get(0).getEndTime() < time)) {
			//System.out.println("------------------------- Delay (Late start) ----------------------------");

			int prevAircraft = route.get(arrival ? 0 : last).getEdge().getReservations().get(route.get(arrival ? 0 : last).getEdge().getReservations().size() - 2).getAirplaneID();

			//System.out.println("Caused by Airplane: " + prevAircraft);
			//System.out.println();
			boolean found = false;

			for (Delay delay : delays)
				if (delay.getAircraftID() == prevAircraft) {
					delay.addDelay(arrival ? time - route.get(0).getEndTime() : route.get(last).getStartTime() - time);
					found = true;
					break;
				}
			if (!found)
				delays.add(new Delay(prevAircraft, arrival ? time - route.get(0).getEndTime() : route.get(last).getStartTime() - time));//route.get(route.size() - 2).getStartTime() - route.get(route.size() - 1).getEndTime()));
			found = false;
		}
	}*/
	
	public void sortReservations() {
		Collections.sort(route, new Comparator<Reservation>() {
			@Override
			public int compare(Reservation r1, Reservation r2) {
				return Double.compare(r2.getStartTime(), r1.getStartTime());
			}
		});
	}
	
	public void sortIncReservations() {
		Collections.sort(route, new Comparator<Reservation>() {
			@Override
			public int compare(Reservation r1, Reservation r2) {
				return Double.compare(r1.getStartTime(), r2.getStartTime());
			}
		});
	}

	public void sortDelays() {
		Collections.sort(delays, new Comparator<Delay>() {
			@Override
			public int compare(Delay d1, Delay d2) {
				return Double.compare(d1.getDelay(), d2.getDelay());
			}
		});
	}
}
