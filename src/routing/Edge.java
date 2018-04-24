package routing;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class Edge {
	private final Vertex source;
	private final Vertex destination;
	private final double weight;
	private ArrayList<Window> windows, tempWindows;
	private ArrayList<Edge> confEdges, confInEdges, confOutEdges;
	private ArrayList<Reservation> reservations;
	private static final double AIRCRAFT_SPEED = 8.2; //m/s
	private ReservationComparator resComparator = new ReservationComparator();
	private WindowComparator winComparator = new WindowComparator();

	public Edge(Vertex source, Vertex destination, double distance) {
		this.source = source;
		this.destination = destination;
		this.weight = Math.round(distance / AIRCRAFT_SPEED);
		windows = new ArrayList<Window>();
		windows.add(new Window(this, 0.0, Double.MAX_VALUE));
		tempWindows = new ArrayList<Window>();
		tempWindows.add(new Window(this, 0.0, Double.MAX_VALUE));
		confEdges = new ArrayList<Edge>();
		confInEdges = new ArrayList<Edge>();
		confOutEdges = new ArrayList<Edge>();
		reservations = new ArrayList<Reservation>();
	}

	public ArrayList<Window> getWindows() {
		return windows;
	}
	
	public ArrayList<Window> getTempWindows() {
		return tempWindows;
	}

	public void insertWindow(double sTime, double eTime) {
		windows.add(new Window(this, sTime, eTime));
		Collections.sort(windows, winComparator);
	}
	
	public void insertTempWindow(double sTime, double eTime) {
		tempWindows.add(new Window(this, sTime, eTime));
		Collections.sort(tempWindows, winComparator);
	}

	public void deleteWindow(int n) {
		windows.remove(n);
	}

	public void deleteTempWindow(int n) {
		tempWindows.remove(n);
	}
	
	public void fixWindowsByReservation(double sTime, double eTime, boolean temp) { //sTime and eTime are for the reservation
		
		ArrayList<Window> w = temp ? tempWindows : windows;

		for (int i = 0; i < w.size(); i++) {
			if (w.get(i).getStartTime() <= sTime && w.get(i).getEndTime() >= eTime) {
				if (w.get(i).getStartTime() == sTime && w.get(i).getEndTime() == eTime)
					w.remove(w.get(i));
				else if (w.get(i).getStartTime() != sTime && w.get(i).getEndTime() != eTime) {
					w.add(new Window(this, w.get(i).getStartTime(), sTime));
					w.add(new Window(this, eTime, w.get(i).getEndTime()));
					w.remove(w.get(i));
				} else if (w.get(i).getStartTime() == sTime)
					w.get(i).setStartTime(eTime);
				else
					w.get(i).setEndTime(sTime);
				break;
			} else if (sTime <= w.get(i).getStartTime() && eTime > w.get(i).getStartTime()) {
				w.get(i).setStartTime(eTime);
				break;
			} else if (sTime < w.get(i).getEndTime() && eTime >= w.get(i).getEndTime())
				w.get(i).setEndTime(sTime);
			else if (sTime < w.get(i).getStartTime() && eTime > w.get(i).getEndTime())
				w.remove(w.get(i));
		}
		Collections.sort(w, winComparator);
		//System.out.println("Window " + this.source.getId() + "-" + this.destination.getId() + " " + (double) Math.round(sTime * 10) / 10 + " " + (double) Math.round(eTime * 10) / 10);
	}

	public void addReservation(Reservation r) { //does it need to get sorted?
		reservations.add(r);
		//Collections.sort(reservations, resComparator);
	}

	public ArrayList<Reservation> getReservations() {
		return reservations;
	}

	public Vertex getDestination() {
		return destination;
	}

	public Vertex getSource() {
		return source;
	}

	public double getWeight() {
		return weight;
	}

	public void addConflInEdge(Edge edge) {
		confInEdges.add(edge);
	}

	public void addConflOutEdge(Edge edge) {
		confOutEdges.add(edge);
	}
	
	public void addConflEdge(Edge edge) {
		confEdges.add(edge);
	}

	public ArrayList<Edge> getConflEdges(boolean backwards, boolean arrival) {
		return arrival ? (backwards ? confInEdges : confOutEdges) : (backwards ? confOutEdges : confInEdges);
	}

	public void clearWindows(boolean temp) {
		ArrayList<Window> w = temp ? tempWindows : windows;
		w.clear();
		w.add(new Window(this, 0.0, Double.MAX_VALUE));
	}
	
	public void clearReservations() {	//TODO Not used
		reservations.clear();
	}
	
	public void sortReservations() {
		Collections.sort(reservations, resComparator);	
	}
	
	public void sortWindows(boolean temp) {
		Collections.sort(temp ? tempWindows : windows, winComparator);
	}	

	private class WindowComparator implements Comparator<Window> { // TODO mpori na kano geniko comparator
		@Override
		public int compare(Window w1, Window w2) {
			return (int) (w1.getStartTime() - w2.getStartTime());
		}
	}
	
	private class ReservationComparator implements Comparator<Reservation> { // TODO mpori na kano geniko comparator
		@Override
		public int compare(Reservation r1, Reservation r2) {
			return (int) (r1.getStartTime() - r2.getStartTime());
		}
	}
}