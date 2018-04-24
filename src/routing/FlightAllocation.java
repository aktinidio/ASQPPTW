package routing;

import java.util.ArrayList;

public class FlightAllocation {

	private ArrayList<Flight> flights;
	private double delay;

	public FlightAllocation(ArrayList<Flight> flights, double delay) {
		this.flights = flights;
		this.delay = delay;
	}
	
	public ArrayList<Flight> getFlights() {
		return flights;
	}
	
	public int[] getAllIDs() {
		int[] temp = new int[flights.size()];
		for (int i = 0; i < flights.size(); i++)
			temp[i] = flights.get(i).getId();
		return temp;
	}
	
	public double getDelay() {
		return delay;
	}
	
	public int[] addNgetIds(int extraID) {
		int[] temp = new int[flights.size() + 1];
		for(int i = 0; i < flights.size(); i++)
			temp[i] = flights.get(i).getId();
		temp[temp.length - 1] = extraID;
		return temp;
	}
	
	public int[] addNgetSource(int extraSource) {
		int[] temp = new int[flights.size() + 1];
		for(int i = 0; i < flights.size(); i++)
			temp[i] = flights.get(i).getSource();
		temp[temp.length - 1] = extraSource;
		return temp;
	}
	
	public int[] addNgetTarget(int extraTarget) {
		int[] temp = new int[flights.size() + 1];
		for(int i = 0; i < flights.size(); i++)
			temp[i] = flights.get(i).getTarget();
		temp[temp.length - 1] = extraTarget;
		return temp;
	}
	
	public double[] addNgetTime(double extraTime) {
		double[] temp = new double[flights.size() + 1];
		for(int i = 0; i < flights.size(); i++)
			temp[i] = flights.get(i).getTime();
		temp[temp.length - 1] = extraTime;
		return temp;
	}
	
	public boolean[] addNgetArrival(boolean extraArrival) {
		boolean[] temp = new boolean[flights.size() + 1];
		for(int i = 0; i < flights.size(); i++)
			temp[i] = flights.get(i).isArrival();
		temp[temp.length - 1] = extraArrival;
		return temp;
	}
	
	public int[] addNgetIds(ArrayList<Flight> list) {
		int[] temp = new int[flights.size() + list.size()];
		for(int i = 0; i < flights.size(); i++)
			temp[i] = flights.get(i).getId();
		for(int i = 0; i < list.size(); i++)
			temp[flights.size() + i] = list.get(i).getId();
		return temp;
	}
	
	public int[] addNgetSource(ArrayList<Flight> list) {
		int[] temp = new int[flights.size() + list.size()];
		for(int i = 0; i < flights.size(); i++)
			temp[i] = flights.get(i).getSource();
		for(int i = 0; i < list.size(); i++)
			temp[flights.size() + i] = list.get(i).getSource();
		return temp;
	}
	
	public int[] addNgetTarget(ArrayList<Flight> list) {
		int[] temp = new int[flights.size() + list.size()];
		for(int i = 0; i < flights.size(); i++)
			temp[i] = flights.get(i).getTarget();
		for(int i = 0; i < list.size(); i++)
			temp[flights.size() + i] = list.get(i).getTarget();
		return temp;
	}
	
	public double[] addNgetTime(ArrayList<Flight> list) {
		double[] temp = new double[flights.size() + list.size()];
		for(int i = 0; i < flights.size(); i++)
			temp[i] = flights.get(i).getTime();
		for(int i = 0; i < list.size(); i++)
			temp[flights.size() + i] = list.get(i).getTime();
		return temp;
	}
	
	public boolean[] addNgetArrival(ArrayList<Flight> list) {
		boolean[] temp = new boolean[flights.size() + list.size()];
		for(int i = 0; i < flights.size(); i++)
			temp[i] = flights.get(i).isArrival();
		for(int i = 0; i < list.size(); i++)
			temp[flights.size() + i] = list.get(i).isArrival();
		return temp;
	}
}