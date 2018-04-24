package routing;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.PriorityQueue;

public class ASQPPTW {

	private boolean flip;
	private Label labelN, label;
	private static final double SAFETY_TIME = 0.01;
	private final List<Edge> edges;
	private final List<Vertex> nodes;
	private final Integer[] destNode;
	private ArrayList<Route> routes = new ArrayList<Route>();
	private ArrayList<Label> labelList;
	private int pushbackTime;
	//long stratTime;
	//long timer = 0;
	
	public ASQPPTW(List<Edge> e, List<Vertex> v, Integer[] d) {
		edges = e;
		nodes = v;
		destNode = d;
		setConflEdges();
	}

	public void execute(List<Flight> flights) {
		
		routes = new ArrayList<Route>();				
		for (int i = 0; i < flights.size(); i++) {
			//stratTime = System.currentTimeMillis();
			runQpptw(flights.get(i));
			readjustWindows(i, flights.get(i).isArrival());
			routes.get(i).sortReservations();
			
			//System.out.println(Math.round((routes.get(i).getTotalTime() - routes.get(i).getMinimumRouting() - (routes.get(i).isArrival() ? 0 : flights.get(i).getPushbackTime()))*1000)/1000);
			//totalDelay += routes.get(i).getTotalTime() - routes.get(i).getMinimumRouting() - (routes.get(i).isArrival() ? 0 : flights.get(i).getPushbackTime());
			//totalRouting += routes.get(i).getTotalTime() - (routes.get(i).isArrival() ? 0 : flights.get(i).getPushbackTime());
			
			//System.out.println(Math.round(( routes.get(i).getMinimumRouting()) + (routes.get(i).isArrival() ? 0 : flights.get(i).getPushbackTime()))+ "\t" + Math.round(routes.get(i).getTotalTime()));
			//if(flights.get(i).isArrival())
			//System.out.println(Math.round( routes.get(i).getMinimumRouting()));
			//System.out.println(Math.round( routes.get(i).getTotalTime()));
			//printMovements(i, flights.get(i).isArrival());
		}
		//System.out.println(timer);
	}
	
	private void setConflEdges() {
		for (Edge e1 : edges)
			for (Edge e2 : edges)
				if (e1 != e2)
					if (e1.getDestination() == e2.getDestination() || e1.getDestination() == e2.getSource())
						e1.addConflOutEdge(e2);
					else if (e1.getSource() == e2.getDestination() || e1.getSource() == e2.getSource())
						e1.addConflInEdge(e2);
	}
	
	private void runQpptw(Flight f) {
		
		boolean arrival = f.isArrival();
		int aircraftID = f.getId(), sourcePos = arrival ? f.getSource() : f.getTarget(), targetPos = arrival ? f.getTarget() : f.getSource();
		double time = f.getTime();
		pushbackTime = f.getPushbackTime();
		
		routes.add(new Route(aircraftID, sourcePos, targetPos, time, arrival, pushbackTime, f.getMinimumRoutingTime()));

		boolean insert;
		double timeIn, timeOut;
		Vertex node;
												
		PriorityQueue<Label> heap =  new PriorityQueue<Label>();							// 1
		labelList = new ArrayList<Label>();													// 2
		
		if(arrival)
			label = new Label(sourcePos, time + f.getMinimumRoutingTime(), time, Double.MAX_VALUE, aircraftID, -1, -1, false);// 3																									
		else
			label = new Label(sourcePos, -time + f.getMinimumRoutingTime(), 0, time, aircraftID, -1, -1, false); 			  // 3/ /TODO on arrival the source is the destination :/																// 4
		heap.add(label);																	// 4
		labelList.add(label); 																// 5
		//int c = 0;
		
		while (!heap.isEmpty()) { 															// 6									
			label = heap.poll();															// 7
			if (label.labelNodePos == targetPos) { 											// 8
				reconstructRoute(label, aircraftID, arrival); 								// 9
//				if(arrival)
//					System.out.println(c);
				return; 																	// 10
			}
			for (Edge edge : edges) { 														// 11
				if (isOutgoingEdgeOfVertex(edge, nodes.get(label.labelNodePos), label.predNodePos > -1 ? nodes.get(label.predNodePos) : null)) {
					for (int j = 0; j < edge.getWindows().size(); j++) {					// 12
						if (edge.getWindows().get(j).getStartTime() > label.interval[1]) 	// 14
							break; 															// 15
						if (edge.getWindows().get(j).getEndTime() >= label.interval[0]) { 	// 16

							if (arrival) {
								timeIn = Math.max(label.interval[0], edge.getWindows().get(j).getStartTime()); // 18
								timeOut = timeIn + edge.getWeight();				 	   // 19

								if (timeOut <= edge.getWindows().get(j).getEndTime()) { 	// 20
									node = flip ? edge.getSource() : edge.getDestination();
									labelN = new Label(node.getId(), timeOut, edge.getWindows().get(j).getEndTime(), aircraftID, label.labelNodePos, labelList.indexOf(label), flip);
									insert = true;											// 21+22

									for (int k = 0; k < labelList.size(); k++) { 			// 24
										if (dominates(labelList.get(k), labelN)) { 			// 25
											insert = false;
											break; 											// 26
										}
										if (dominates(labelN, labelList.get(k))) 			// 27
											heap.remove(labelList.get(k));					// 28 labelList.remove(labelList.get(k));
									} 														// 29 Warning removing this line keeps the index of the list up to date BUT increases the repetitions.
									if (insert) {											// A* addition f(n)=g(n)+h(n)
										labelN.setCost(labelN.interval[0] + (labelN.labelNodePos == targetPos ? 0 : node.getConnectionCost(Arrays.asList(destNode).indexOf(targetPos))));
										heap.add(labelN);									// 30
										labelList.add(labelN); 								// 31
										//c++;
									}
								}
							} else {
								node = flip ? edge.getSource() : edge.getDestination();
								timeOut = Math.min(label.interval[1], edge.getWindows().get(j).getEndTime());
								timeIn = timeOut - edge.getWeight() - (node.getId() == targetPos ? pushbackTime : 0);
								if (timeIn >= edge.getWindows().get(j).getStartTime()) {	// 20
									
									labelN = new Label(node.getId(), edge.getWindows().get(j).getStartTime(), timeIn, aircraftID, label.labelNodePos, labelList.indexOf(label), flip);
									insert = true;											// 21+22

									for (int k = 0; k < labelList.size(); k++) { 			// 24
										if (dominates(labelList.get(k), labelN)) { 			// 25
											insert = false;
											break; 											// 26
										}
										if (dominates(labelN, labelList.get(k))) 			// 27
											heap.remove(labelList.get(k));					// 28 labelList.remove(labelList.get(k));			
									} 														// 29 Warning removing this line keeps the index of the list up to date BUT increases the repetitions.
									if (insert) {
										labelN.setCost(-labelN.interval[1] + (labelN.labelNodePos == targetPos ? -pushbackTime : node.getConnectionCost(Arrays.asList(destNode).indexOf(targetPos))));
										heap.add(labelN);
										labelList.add(labelN); 								// 31
										//c++;
									}
								}
							}
						}
					}
				}
			}
		}
		System.err.println("There is no s-t route... :/ ");
		return;
	}
	
/*	private double getNodeDist(Vertex v1, Vertex v2){
		return distance(v1.getLat(), v1.getLon(), v2.getLat(), v2.getLon())/8.2;
	}
	
	private double distance(double lat1, double lon1, double lat2, double lon2) {
		return Math.acos(Math.sin(deg2rad(lat1)) * Math.sin(deg2rad(lat2)) + Math.cos(deg2rad(lat1)) * Math.cos(deg2rad(lat2)) * Math.cos(deg2rad(lon1 - lon2))) * 6371000;
	}
	
	private double deg2rad(double deg) {
	  return (deg * Math.PI / 180.0);
	}*/
	
	/*private void runQpptw(Flight f) {
		
		boolean arrival = f.isArrival();
		int aircraftID = f.getId(), sourcePos = arrival ? f.getSource() : f.getTarget(), targetPos = arrival ? f.getTarget() : f.getSource();
		double time = f.getTime();
		pushbackTime = f.getPushbackTime();
		
		routes.add(new Route(aircraftID, sourcePos, targetPos, time, arrival, pushbackTime, f.getMinimumRoutingTime()));

		boolean insert;
		double timeIn, timeOut;
		Vertex node;
												
		PriorityQueue<Label> heap =  new PriorityQueue<Label>();							// 1
		labelList = new ArrayList<Label>();													// 2
		
		if(arrival){
			label = new Label(sourcePos, time + f.getMinimumRoutingTime(), time, Double.MAX_VALUE, aircraftID, -1, -1, false);// 3																									

			heap.add(label);																	// 4
		labelList.add(label); 																// 5
		//int c = 0;

		while (!heap.isEmpty()) { 															// 6									
			label = heap.poll();															// 7
			if (label.labelNodePos == targetPos) { 											// 8
				reconstructRoute(label, aircraftID, arrival); 								// 9
				//timer += System.currentTimeMillis() - stratTime;
				//System.out.println(c);
				return; 																	// 10
			}
			for (Edge edge : edges) { 														// 11
				if (isOutgoingEdgeOfVertex(edge, nodes.get(label.labelNodePos), label.predNodePos > -1 ? nodes.get(label.predNodePos) : null)) {
					for (int j = 0; j < edge.getWindows().size(); j++) {					// 12
						if (edge.getWindows().get(j).getStartTime() > label.interval[1]) 	// 14
							break; 															// 15
						if (edge.getWindows().get(j).getEndTime() >= label.interval[0]) { 	// 16
								timeIn = Math.max(label.interval[0], edge.getWindows().get(j).getStartTime()); // 18
								timeOut = timeIn + edge.getWeight();				 	   // 19

								if (timeOut <= edge.getWindows().get(j).getEndTime()) { 	// 20
									node = flip ? edge.getSource() : edge.getDestination();
									labelN = new Label(node.getId(), timeOut, edge.getWindows().get(j).getEndTime(), aircraftID, label.labelNodePos, labelList.indexOf(label), flip);
									insert = true;											// 21+22

									for (int k = 0; k < labelList.size(); k++) { 			// 24
										if (dominates(labelList.get(k), labelN)) { 			// 25
											insert = false;
											break; 											// 26
										}
										if (dominates(labelN, labelList.get(k))) 			// 27
											heap.remove(labelList.get(k));					// 28
									} 														// 29
									if (insert) {											// A* addition f(n)=g(n)+h(n)
										labelN.setCost(labelN.interval[0] + (labelN.labelNodePos == targetPos ? 0 : node.getConnectionCost(Arrays.asList(destNode).indexOf(targetPos))));
										heap.add(labelN);									// 30
										labelList.add(labelN); 								// 31
										//c++;
									}
								}
						}
					}
				}
			}
		}
		} else {
			label = new Label(sourcePos, -time + f.getMinimumRoutingTime(), 0, time, aircraftID, -1, -1, false); 				// 3/ // 4
			heap.add(label);																	// 4
			labelList.add(label); 																// 5
		while (!heap.isEmpty()) { 															// 6									
			label = heap.poll();															// 7
			if (label.labelNodePos == targetPos) { 											// 8
				reconstructRoute(label, aircraftID, arrival); 								// 9
				//timer += System.currentTimeMillis() - stratTime;
				//System.out.println(c);
				return; 																	// 10
			}
			for (Edge edge : edges) { 														// 11
				if (isOutgoingEdgeOfVertex(edge, nodes.get(label.labelNodePos), label.predNodePos > -1 ? nodes.get(label.predNodePos) : null)) {
					for (int j = 0; j < edge.getWindows().size(); j++) {					// 12
						if (edge.getWindows().get(j).getStartTime() > label.interval[1]) 	// 14
							break; 															// 15
						if (edge.getWindows().get(j).getEndTime() >= label.interval[0]) { 	// 16
								node = flip ? edge.getSource() : edge.getDestination();
								timeOut = Math.min(label.interval[1], edge.getWindows().get(j).getEndTime());
								timeIn = timeOut - edge.getWeight() - (node.getId() == targetPos ? pushbackTime : 0);
								if (timeIn >= edge.getWindows().get(j).getStartTime()) {	// 20
									
									labelN = new Label(node.getId(), edge.getWindows().get(j).getStartTime(), timeIn, aircraftID, label.labelNodePos, labelList.indexOf(label), flip);
									insert = true;											// 21+22

									for (int k = 0; k < labelList.size(); k++) { 			// 24
										if (dominates(labelList.get(k), labelN)) { 			// 25
											insert = false;
											break; 											// 26
										}
										if (dominates(labelN, labelList.get(k))) 			// 27
											heap.remove(labelList.get(k));					// 28
									} 														// 29
									if (insert) {
										labelN.setCost(-labelN.interval[1] + (labelN.labelNodePos == targetPos ? -pushbackTime : node.getConnectionCost(Arrays.asList(destNode).indexOf(targetPos))));
										heap.add(labelN);
										labelList.add(labelN); 								// 31
										//c++;
									}
								}
						}
					}
				}
			}
		}
		}
		System.err.println("There is no s-t route... :/ ");
		return;
	}*/

	private void reconstructRoute(Label label, int aircraftID, boolean arrival) { // with edgeWeight

		int labelNodePos = label.labelNodePos, predNodePos = label.predNodePos, labelsPos = labelList.indexOf(label), firstNodePos = labelNodePos;
		double beginTime = Double.MAX_VALUE, endTime = Double.MAX_VALUE;
		
		double extraTime = pushbackTime;
		if (arrival)
			while (labelsPos != 0) {

				Edge edge = edgeByVertexPos(predNodePos, labelNodePos);

				if (labelNodePos == label.labelNodePos) {
					endTime = labelList.get(labelsPos).interval[0];
					beginTime = Math.min(endTime - edge.getWeight(), labelList.get(labelList.get(labelsPos).labelListPredPos).interval[1]);
				} else {
					endTime = beginTime;
					beginTime = Math.min(labelList.get(labelsPos).interval[0] - edge.getWeight(), labelList.get(labelList.get(labelsPos).labelListPredPos).interval[1]);
				}
//				if (label.labelListPredPos == 0)
//					beginTime -= extraTime;
				
				routes.get(aircraftID).addReservation(new Reservation(edge, new double[] { beginTime, endTime }, aircraftID, labelList.get(labelsPos).backwards), arrival);
				edge.fixWindowsByReservation(beginTime, endTime, false);

				labelsPos = label.labelListPredPos;
				label = labelList.get(labelsPos);
				labelNodePos = label.labelNodePos;
				predNodePos = label.predNodePos;
			}
		else
			while (labelsPos != 0) {

				Edge edge = edgeByVertexPos(predNodePos, labelNodePos);

				if (labelNodePos == label.labelNodePos) {
					beginTime = labelList.get(labelsPos).interval[1];
					endTime = Math.max(beginTime + edge.getWeight(), labelList.get(labelList.get(labelsPos).labelListPredPos).interval[0]);
				} else {
					beginTime = endTime;
					endTime = Math.max(labelList.get(labelsPos).interval[1] + edge.getWeight(), labelList.get(labelList.get(labelsPos).labelListPredPos).interval[0]);
				}
				
				if (labelNodePos == firstNodePos)
					endTime += extraTime;

				routes.get(aircraftID).addReservation(new Reservation(edge, new double[] { beginTime, endTime }, aircraftID, labelList.get(labelsPos).backwards), arrival);
				edge.fixWindowsByReservation(beginTime, endTime, false);

				labelsPos = label.labelListPredPos;
				label = labelList.get(labelsPos);
				labelNodePos = label.labelNodePos;
				predNodePos = label.predNodePos;
			}
	}

	private void readjustWindows(int aircraftID, boolean arrival) {	

		for (Reservation reservation : routes.get(aircraftID).getReservations()) {
			double inTime = reservation.getStartTime(), outTime = reservation.getEndTime() + SAFETY_TIME;

			for (Edge edge : reservation.getEdge().getConflEdges(reservation.isBackwards(), arrival)){
				edge.addReservation(new Reservation(edge, new double[] {inTime, outTime}, aircraftID, reservation.isBackwards()));
				for (int j = 0; j < edge.getWindows().size(); j++) {
					if (outTime <= edge.getWindows().get(j).getStartTime())
						break;
					if (inTime < edge.getWindows().get(j).getEndTime()) {
						if (inTime < edge.getWindows().get(j).getStartTime() + SAFETY_TIME) {
							if (edge.getWindows().get(j).getEndTime() - SAFETY_TIME < outTime)
								edge.deleteWindow(j--);
							else
								edge.getWindows().get(j).setStartTime(outTime);
						} else {
							if (edge.getWindows().get(j).getEndTime() - SAFETY_TIME < outTime)
								edge.getWindows().get(j).setEndTime(inTime);
							else {
								edge.insertWindow(edge.getWindows().get(j).getStartTime(), inTime);
								//if (edge.getWindows().size() > j + 1) // TODO elenxos
								edge.getWindows().get(j).setStartTime(outTime);
							}
						}
					}
					edge.sortWindows(false);
				}
			}
		}
	}
	
	public Route runSimpleQpptw(int sourcePos, int targetPos, boolean arrival, double time, int pushbackT) {

		boolean insert;
		double timeIn, timeOut;
		FibonacciHeap<Label> fib;
		fib = new FibonacciHeap<Label>();													// 1
		labelList = new ArrayList<Label>();
		ArrayList<FibonacciHeap.Entry<Label>> entries;
		entries = new ArrayList<FibonacciHeap.Entry<Label>>();
		
		if(arrival) {
			label = new Label(sourcePos, time, Double.MAX_VALUE, 0, -1, -1, false); 		// 3
			entries.add(fib.enqueue(label, time)); 											// 4
		} else {
			label = new Label(sourcePos, 0, time, 0, -1, -1, false); 						// 3
			entries.add(fib.enqueue(label, -time)); 										// 4
		}
		labelList.add(label); 																// 5
		
		while (!fib.isEmpty()) { 															// 6
			label = fib.dequeueMin().getValue(); 											// 7

			if (label.labelNodePos == targetPos) 											// 8
				return reconstructSimpleRoute(label, time, arrival, pushbackT); 			// 9 + 10
			
			for (Edge edge : edges) {														// 11
				if (isOutgoingEdgeOfVertex(edge, nodes.get(label.labelNodePos), label.predNodePos > -1 ? nodes.get(label.predNodePos) : null)) {
					for (int j = 0; j < edge.getTempWindows().size(); j++) {					// 12
						if (edge.getTempWindows().get(j).getStartTime() > label.interval[1])	// 14
							break; 																// 15
						if (edge.getTempWindows().get(j).getEndTime() >= label.interval[0]) { 	// 16
							if (arrival) {	
								timeIn = Math.max(label.interval[0], edge.getTempWindows().get(j).getStartTime()); // 18
								timeOut = timeIn + edge.getWeight(); 							// 19
	
								if (timeOut <= edge.getTempWindows().get(j).getEndTime()) { 	// 20
																								// 21+22 
									labelN = new Label((flip ? edge.getSource() : edge.getDestination()).getId(), timeOut, edge.getTempWindows().get(j).getEndTime(), 0, label.labelNodePos, labelList.indexOf(label), flip);
									insert = true;
	
									for (int k = 0; k < labelList.size(); k++) { 				// 24
										if (dominates(labelList.get(k), labelN)) { 				// 25
											insert = false;
											break; 												// 26
										}
										if (dominates(labelN, labelList.get(k))) 				// 27
											fib.delete(entries.get(k));							// 28				
									}															// 29
									if (insert) {
										entries.add(fib.enqueue(labelN, labelN.interval[0])); 	// 30
										labelList.add(labelN);									// 31
									}
								}
							} else {
								timeOut = Math.min(label.interval[1], edge.getTempWindows().get(j).getEndTime());
								timeIn = timeOut - edge.getWeight() - ((flip ? edge.getSource() : edge.getDestination()).getId() == targetPos ? pushbackTime : 0);
								if (timeIn >= edge.getTempWindows().get(j).getStartTime()) {	// 20
																							// 21+22
									labelN = new Label((flip ? edge.getSource() : edge.getDestination()).getId(), edge.getTempWindows().get(j).getStartTime(), timeIn, 0, label.labelNodePos, labelList.indexOf(label), flip);
									insert = true;

									for (int k = 0; k < labelList.size(); k++) { 			// 24
										if (dominates(labelList.get(k), labelN)) { 			// 25
											insert = false;
											break; 											// 26
										}
										if (dominates(labelN, labelList.get(k))) 			// 27
											fib.delete(entries.get(k)); 					// 28
									} 														// 29
									if (insert) {
										entries.add(fib.enqueue(labelN, -labelN.interval[1])); // 30
										labelList.add(labelN); 								// 31
									}
								}
							}
						}
					}
				}
			}
		}
		System.err.println("There is no s-t route... :/ ");
		return null;
	}
	
	private Route reconstructSimpleRoute(Label label, double time, boolean arrival, int pushbackT) { // with edgeWeight

		int labelNodePos = label.labelNodePos, predNodePos = label.predNodePos, labelsPos = labelList.indexOf(label), firstNodePos = labelNodePos;
		double beginTime = Double.MAX_VALUE, endTime = Double.MAX_VALUE;
		Route r = new Route(0, 0, 0, time, arrival, pushbackT, 0);	//TODO do I need this info?
		double extraTime = pushbackT;

		if (arrival)
			while (labelsPos != 0) {
				Edge edge = edgeByVertexPos(predNodePos, labelNodePos);
				if (labelNodePos == label.labelNodePos) {
					endTime = labelList.get(labelsPos).interval[0];
					beginTime = Math.min(endTime - edge.getWeight(), labelList.get(labelList.get(labelsPos).labelListPredPos).interval[1]);
				} else {
					endTime = beginTime;
					beginTime = Math.min(labelList.get(labelsPos).interval[0] - edge.getWeight(), labelList.get(labelList.get(labelsPos).labelListPredPos).interval[1]);
				}
//				if (label.labelListPredPos == 0)
//					beginTime -= extraTime;

				r.addSimpleReservation(new Reservation(edge, new double[] { beginTime, endTime }, 0, labelList.get(labelsPos).backwards));
				//edge.fixWindowsByReservation(beginTime, endTime, true);

				labelsPos = label.labelListPredPos;
				label = labelList.get(labelsPos);
				labelNodePos = label.labelNodePos;
				predNodePos = label.predNodePos;
			}
		else
			while (labelsPos != 0) {

				Edge edge = edgeByVertexPos(predNodePos, labelNodePos);

				if (labelNodePos == label.labelNodePos) {
					beginTime = labelList.get(labelsPos).interval[1];
					endTime = Math.max(beginTime + edge.getWeight(), labelList.get(labelList.get(labelsPos).labelListPredPos).interval[0]);
				} else {
					beginTime = endTime;
					endTime = Math.max(labelList.get(labelsPos).interval[1] + edge.getWeight(), labelList.get(labelList.get(labelsPos).labelListPredPos).interval[0]);
				}				
				if (labelNodePos == firstNodePos)
					endTime += extraTime;

				r.addSimpleReservation(new Reservation(edge, new double[] { beginTime, endTime }, 0, labelList.get(labelsPos).backwards));
				//edge.fixWindowsByReservation(beginTime, endTime, true);
				
				labelsPos = label.labelListPredPos;
				label = labelList.get(labelsPos);
				labelNodePos = label.labelNodePos;
				predNodePos = label.predNodePos;
			}
		return r;
	}
	
	/*private void readjustTempWindows(Route r) {	

		for (Reservation reservation : r.getReservations()) {
			double inTime = reservation.getStartTime(), outTime = reservation.getEndTime() + SAFETY_TIME;

			for (Edge edge : reservation.getEdge().getConflEdges(reservation.isBackwards(), r.isArrival())){
				//edge.addReservation(new Reservation(edge, new double[] {inTime, outTime}, aircraftID, reservation.isBackwards())); //test // not sorting correctly
				for (int j = 0; j < edge.getTempWindows().size(); j++) {
					if (outTime <= edge.getTempWindows().get(j).getStartTime())
						break;
					if (inTime < edge.getTempWindows().get(j).getEndTime()) {
						if (inTime < edge.getTempWindows().get(j).getStartTime() + SAFETY_TIME) {
							if (edge.getTempWindows().get(j).getEndTime() - SAFETY_TIME < outTime)
								edge.deleteTempWindow(j--);
							else
								edge.getTempWindows().get(j).setStartTime(outTime);
						} else {
							if (edge.getTempWindows().get(j).getEndTime() - SAFETY_TIME < outTime)
								edge.getTempWindows().get(j).setEndTime(inTime);
							else {
								edge.insertTempWindow(edge.getTempWindows().get(j).getStartTime(), inTime);
								edge.getTempWindows().get(j).setStartTime(outTime);
							}
						}
					}
					edge.sortWindows(true);
				}
			}
		}
		r.sortReservations();
	}*/
	
	public double getTotalRoutingTime() {
		
		double totalDelay = 0;		
		for (Route r : routes) {
			double total = r.getTotalTime();
			double min = r.getMinimumRouting();
			double delay = Math.round((total - min - (r.isArrival() ? 0 : r.getPushbackTime())) * 1000) / 1000;
			totalDelay += delay;
		}
		return totalDelay;
	}
	
	private boolean isOutgoingEdgeOfVertex(Edge e, Vertex v1, Vertex v2) {

		if (e.getSource() == v1 && e.getDestination() != v2)
			flip = false;
		else if (e.getDestination() == v1 && e.getSource() != v2)
			flip = true;
		else
			return false;
		return true;
	}
	
	private boolean dominates(Label higher, Label lower) {
		if (lower.activ)
			if (higher.labelNodePos == lower.labelNodePos)
				if (higher.interval[0] <= lower.interval[0])
					if (higher.interval[1] >= lower.interval[1]) {
						lower.activ = false;
						return true;
					}
		return false;
	}
	
	private Edge edgeByVertexPos(int vertex1, int vertex2) {
		for (Edge edge : edges)
			if (edge.getSource().getId() == vertex1 && edge.getDestination().getId() == vertex2 || edge.getSource().getId() == vertex2 && edge.getDestination().getId() == vertex1)
				return edge;
		return null;
	}
	
	/*private void printMovements(int i, boolean b) {
		List<Reservation> a = routes.get(i).getReservations();
		int p = routes.get(i).getPushbackTime();
		Collections.reverse(a);
		if (b)
			System.out.println((i + 1) + " " + Math.round(a.get(0).getStartTime()) + " " + Math.round(a.get(0).getEndTime()) + " " + (a.get(0).isBackwards() ? a.get(0).getEdge().getDestination().getId() + " " + a.get(0).getEdge().getSource().getId() : a.get(0).getEdge().getSource().getId() + " " + a.get(0).getEdge().getDestination().getId()));
		else 
			System.out.println((i + 1) + " " + Math.round(a.get(0).getStartTime()) + " " + (Math.round(a.get(0).getStartTime()) + p) + " " + (a.get(0).isBackwards() ? a.get(0).getEdge().getSource().getId() + " " + a.get(0).getEdge().getDestination().getId() : a.get(0).getEdge().getDestination().getId() + " " + a.get(0).getEdge().getSource().getId()));
			//System.out.println((i + 1) + " " + (Math.round(a.get(0).getStartTime()) + p) + " " + Math.round(a.get(0).getEndTime()) + " " + (a.get(0).isBackwards() ? a.get(0).getEdge().getSource().getId() + " " + a.get(0).getEdge().getDestination().getId() : a.get(0).getEdge().getDestination().getId() + " " + a.get(0).getEdge().getSource().getId()));
		
		
		for (Reservation r : a.subList(1, a.size()))
			System.out.println((i+1) + " " + Math.round(r.getStartTime()) + " " + Math.round(r.getEndTime()) + " " + ((b ? !r.isBackwards() : r.isBackwards()) ? r.getEdge().getSource().getId() + " " +  r.getEdge().getDestination().getId() : r.getEdge().getDestination().getId() + " " +  r.getEdge().getSource().getId()));
		Reservation l = a.get(a.size()-1);
		System.out.println((i+1) + " " +  Math.round(l.getEndTime()) + " " + (Math.round(l.getEndTime()) + 10) + " " + ((b ? l.isBackwards() : !l.isBackwards()) ? l.getEdge().getSource().getId() + " " +  l.getEdge().getSource().getId() : l.getEdge().getDestination().getId() + " " +  l.getEdge().getDestination().getId()));
		System.out.println((i+1) + " " +  (Math.round(l.getEndTime()) + 10) + " " + (Math.round(l.getEndTime()) + 11) + " " + ((b ? l.isBackwards() : !l.isBackwards()) ? l.getEdge().getSource().getId() + " " + -1 : l.getEdge().getDestination().getId() + " " +  -1));	
	}*/
}
