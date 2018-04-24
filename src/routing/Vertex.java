package routing;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class Vertex {
	private final int id;
	//private double lat, lon;
	private int cost[];
	private ArrayList<Conflict> conflicts;
	private ArrayList<VertexReserv> vReservations;
	private VReservationComparator resComparator = new VReservationComparator();

	public Vertex(int id, int[] cost) {
		this.id = id;
		this.cost = cost;
		vReservations = new ArrayList<VertexReserv>();
	}
	
	public Vertex(int id) {
		this.id = id;
		cost = null;
		vReservations = new ArrayList<VertexReserv>();
	}
	
//	public Vertex(int id, double lat, double lon) {
//		this.id = id;
//		this.lat = lat;
//		this.lon = lon;
//		cost = null;
//		vReservations = new ArrayList<VertexReserv>();
//	}
//	
//	public double getLat() {
//		return lat;
//	}
//	
//	public double getLon() {
//		return lon;
//	}

	public int getId() {
		return id;
	}
	
	public void setConnectionsSize(int size) {
		cost = new int[size];
	}
	
	public double getConnectionCost(int vertexID) {
		if (cost == null)
			return Integer.MAX_VALUE;
		return cost[vertexID];
	}
	
	public void setConnectionCost(int id, int cost) {
		this.cost[id] = cost;
	}
	
	public void addReservation(VertexReserv res) {
		vReservations.add(res);
	}

	public void changeReservation(int id, double[] newInterval) {
		vReservations.get(id).changeTime(newInterval);
	}
	
	public boolean hasConflicts(boolean extnd) {
		if (vReservations.size() < 2)
			return false;

		conflicts = new ArrayList<Conflict>();
		sortReservations();
		if (extnd)
			for (int i = 1; i < vReservations.size(); i++) { //TODO new code
				if (vReservations.get(i - 1).getEndTime() > vReservations.get(i).getStartTime() && vReservations.get(i - 1).getPreviousVertexID() != vReservations.get(i).getPreviousVertexID())
					conflicts.add(new Conflict(vReservations.get(i - 1).getRouteID(), vReservations.get(i - 1).getReservationID(), vReservations.get(i).getRouteID(), vReservations.get(i).getReservationID()));
			}
		else
			for (int i = 1; i < vReservations.size(); i++)
				if (vReservations.get(i - 1).getEndTime() > vReservations.get(i).getStartTime())
					conflicts.add(new Conflict(vReservations.get(i - 1).getRouteID(), vReservations.get(i - 1).getReservationID(), vReservations.get(i).getRouteID(), vReservations.get(i).getReservationID()));

		return !conflicts.isEmpty();
	}
		
	public ArrayList<Conflict> getConflicts(){
		return conflicts;
	}
	
	public void sortReservations() {
		Collections.sort(vReservations, resComparator);	
	}
	
	private class VReservationComparator implements Comparator<VertexReserv> { // TODO mpori na kano geniko comparator
		@Override
		public int compare(VertexReserv r1, VertexReserv r2) {
			return (int) (r1.getStartTime() - r2.getStartTime());
		}
	}

}