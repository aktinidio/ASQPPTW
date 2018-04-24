package routing;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import jxl.Sheet;
import jxl.Workbook;
import jxl.read.biff.BiffException;
import jxl.write.WritableCell;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;
import jxl.write.WriteException;

public class Controller {

	private final List<Edge> edges = new ArrayList<Edge>();
	private final List<Vertex> nodes = new ArrayList<Vertex>();
	private List<Flight> flights;
	private ASQPPTW qpptw;
	private String[][] gates;

 	public Controller(int day) {	//Not in parallel (flights list is shared)
		try {
			initialiseZurichGraph(day);
			//initialiseArlandaGraphForMinDist();
			//initialiseArlandaGraph(day, "Arlanda.xls", 606);
		} catch (BiffException | IOException e) {
		}	
	}
 	
	public void exectue() {
		long stratTime = System.currentTimeMillis();
		qpptw.execute(flights);
		System.out.println((System.currentTimeMillis() - stratTime));
	}
	
	private void initialiseZurichGraph(int day) throws BiffException, IOException {
		Workbook w = Workbook.getWorkbook(new File("Zurich.xls"));
		Sheet s0 = w.getSheet(0), s1 = w.getSheet(day), s2 = w.getSheet(8), s3 = w.getSheet(9), s4 = w.getSheet(10); //s5 = w.getSheet(11); //temp
		Integer[] destNode = new Integer[s2.getRows()];
		int[] nonStands = new int[s3.getRows()];
		
		// Read destination nodes
		for (int i = 0; i < s2.getRows(); i++)
			destNode[i] = Integer.parseInt(s2.getCell(0, i).getContents());
			
		// Read non stands
		for (int i = 0; i < s3.getRows(); i++)
			nonStands[i] = Integer.parseInt(s3.getCell(0, i).getContents());
		
		// Create nodes
		for (int i = 0; i < 424; i++)
			nodes.add(new Vertex(i));

		// Create nodes + coordinates - TEMP
//		for (int i = 0; i < 424; i++) {
//			nodes.add(new Vertex(i, Double.parseDouble(s5.getCell(0, i).getContents()), Double.parseDouble(s5.getCell(1, i).getContents())));
//		}
		
		// Read connection cost
		for (int i = 0; i < s4.getRows(); i++) {
			nodes.get(nonStands[i]).setConnectionsSize(s4.getColumns());
			for (int j = 0; j < s4.getColumns(); j++)
				nodes.get(nonStands[i]).setConnectionCost(j, Integer.parseInt(s4.getCell(j,i).getContents()));
		}

		// Read distance of edges
		for (int i = 1; i < s0.getRows(); i++)
			edges.add(new Edge(nodes.get(Integer.parseInt(s0.getCell(0, i).getContents())), nodes.get(Integer.parseInt(s0.getCell(1, i).getContents())), Double.parseDouble(s0.getCell(2, i).getContents())));
		
		qpptw = new ASQPPTW(edges, nodes, destNode);
		//qpptw = new QPPTW(edges, nodes);
		flights = new ArrayList<Flight>();		

		int source, target, pushTime, k = 0;
		double time;
		boolean arrival;//, flag = true;

		for (int i = 1; i < s1.getRows(); i++) {
			time = Integer.parseInt(s1.getCell(0, i).getContents());
			source = Integer.parseInt(s1.getCell(1, i).getContents());
			target = Integer.parseInt(s1.getCell(2, i).getContents());
			arrival = Integer.parseInt(s1.getCell(3, i).getContents()) == 1;
			pushTime = arrival ? 0 : Integer.parseInt(s1.getCell(4, i).getContents()) * 40 + 160;
			
//			if (arrival) 
//				if(flag ^= true)
//					continue;
			
			Route r = qpptw.runSimpleQpptw(arrival ? source : target, arrival ? target : source, arrival, arrival ? 0 : 1000, pushTime);
			r.sortReservations();

			flights.add(new Flight(k++, source, target, time, arrival, pushTime, r.getTotalTime()));
		}		
		qpptw = new ASQPPTW(edges, nodes, destNode);
		//qpptw = new QPPTW(edges, nodes);
	}
	
	@SuppressWarnings("unused")
	private void initialiseArlandaGraphForMinDist() throws BiffException, IOException, WriteException {
		Workbook w = Workbook.getWorkbook(new File("Arlanda for minTimes.xls"));
		Sheet s0 = w.getSheet(0), s2 = w.getSheet(1), s3 = w.getSheet(2), s4 = w.getSheet(3);
		int[] stands = new int[s2.getRows()], runways = new int[s3.getRows()], nonStands = new int[s4.getRows()];
		
		// Read stands
		for (int i = 0; i < s2.getRows(); i++)
			stands[i] = Integer.parseInt(s2.getCell(0, i).getContents());
		
		// Read runways
//		for (int i = 0; i < s3.getRows(); i++)
//			runways[i] = Integer.parseInt(s3.getCell(0, i).getContents());
		
		// Read non stands
		for (int i = 0; i < s4.getRows(); i++)
			nonStands[i] = Integer.parseInt(s4.getCell(0, i).getContents());

		// Read nodes
		for (int i = 0; i < 667; i++)
			nodes.add(new Vertex(i));

		// Read distance of edges
		for (int i = 1; i < s0.getRows(); i++)
			edges.add(new Edge(nodes.get(Integer.parseInt(s0.getCell(0, i).getContents())), nodes.get(Integer.parseInt(s0.getCell(1, i).getContents())), Double.parseDouble(s0.getCell(2, i).getContents())));

		//qpptw = new QPPTW(edges, nodes);

		WritableWorkbook ww = Workbook.createWorkbook(new File("output.xls"));
		WritableSheet ws = ww.createSheet("results", 0);

		for (int i = 0; i < stands.length; i++) {
			//System.out.println("--------- Node " + nonStands[i] + " ------------");
			for (int j = 0; j < nonStands.length; j++) {
				if (nonStands[j] ==  stands[i]){
					ws.addCell((WritableCell) new jxl.write.Label(i, j, "" + 0));
					continue;
				}
				System.out.println(stands[i] + " "+nonStands[j]);
				Route r = qpptw.runSimpleQpptw(stands[i], nonStands[j], true, 0, 0);
				r.sortReservations();

				ws.addCell((WritableCell) new jxl.write.Label(i, j, "" + (int) r.getTotalTime()));

//				System.out.println((int) r.getTotalTime());
			}
		}
		ww.write();
		ww.close();
	}
	
	@SuppressWarnings("unused")
	private void initialiseArlandaGraph(int day, String name, int noOfNodes) throws BiffException, IOException {
		
		Workbook w = Workbook.getWorkbook(new File(name));
		Sheet s0 = w.getSheet(0), s1 = w.getSheet(day), s2 = w.getSheet(8), s3 = w.getSheet(9), s4 = w.getSheet(10); //s5 = w.getSheet(11); //temp
		Integer[] destNode = new Integer[s2.getRows()];
		int[] nonStands = new int[s3.getRows()];
		
		// Read destination nodes
		for (int i = 0; i < s2.getRows(); i++)
			destNode[i] = Integer.parseInt(s2.getCell(0, i).getContents());
					
		// Read non stands
		for (int i = 0; i < s3.getRows(); i++)
			nonStands[i] = Integer.parseInt(s3.getCell(0, i).getContents());

		// Read nodes
		for (int i = 0; i < noOfNodes; i++)
			nodes.add(new Vertex(i));
		
		// Read connection cost
		for (int i = 0; i < s4.getRows(); i++) {
			nodes.get(nonStands[i]).setConnectionsSize(s4.getColumns());
			for (int j = 0; j < s4.getColumns(); j++)
				nodes.get(nonStands[i]).setConnectionCost(j, Integer.parseInt(s4.getCell(j,i).getContents()));
		}

		// Read distance of edges
		for (int i = 1; i < s0.getRows(); i++)
			edges.add(new Edge(nodes.get(Integer.parseInt(s0.getCell(0, i).getContents())), nodes.get(Integer.parseInt(s0.getCell(1, i).getContents())), Double.parseDouble(s0.getCell(2, i).getContents())));
		
		//qpptw = new ASQPPTW(edges, nodes, destNode);
		//qpptw = new QPPTW(edges, nodes);
		flights = new ArrayList<Flight>();		

		int source, target, pushTime, k = 0;
		double time;
		boolean arrival;

		for (int i = 1; i < s1.getRows(); i++) {
			time = Integer.parseInt(s1.getCell(0, i).getContents());
			source = Integer.parseInt(s1.getCell(1, i).getContents());
			target = Integer.parseInt(s1.getCell(2, i).getContents());
			arrival = Integer.parseInt(s1.getCell(3, i).getContents()) == 1;
			pushTime = arrival ? 0 : Integer.parseInt(s1.getCell(4, i).getContents()) * 40 + 160;
			
			if (arrival) continue;
			
			Route r = qpptw.runSimpleQpptw(arrival ? source : target, arrival ? target : source, arrival, arrival ? 0 : 1000, pushTime);
			r.sortReservations();

			flights.add(new Flight(k++, source, target, time, arrival, pushTime, r.getTotalTime()));
		}
		//qpptw = new ASQPPTW(edges, nodes, destNode);
		//qpptw = new QPPTW(edges, nodes);
	}
	
	/*private void initialiseZurichGraphFast(int day) throws BiffException, IOException {

	for (int i = 0; i < 424; i++)
		nodes.add(new Vertex(i));

	Scanner scanner = new Scanner(new File("dest.txt"));
	Integer [] destNode = new Integer [110];
	int c = 0;
	while(scanner.hasNextInt())
		destNode[c++] = scanner.nextInt();
	
	scanner = new Scanner(new File("stand.txt"));
	int[] nonStands = new int [317];
	c = 0;
	while(scanner.hasNextInt())
		nonStands[c++] = scanner.nextInt();
	
	scanner = new Scanner(new File("ConCost.txt"));
	for (int i = 0; i < 317; i++) {
		nodes.get(nonStands[i]).setConnectionsSize(110);
		for (int j = 0; j < 110; j++)
			nodes.get(nonStands[i]).setConnectionCost(j, scanner.nextInt());
	}
	
	scanner = new Scanner(new File("Edge.txt"));
	for (int i = 0; i < 507; i++)
		edges.add(new Edge(nodes.get(scanner.nextInt()), nodes.get(scanner.nextInt()), scanner.nextDouble()));
	
	scanner = new Scanner(new File("Data.txt"));

	qpptw = new ASQPPTW(edges, nodes, destNode);
	//qpptw = new QPPTW(edges, nodes);
	flights = new ArrayList<Flight>();		

	int source, target, pushTime, k = 0;
	double time;
	boolean arrival;//, flag = true;

	for (int i = 1; i < 5609; i++) {		
		time = scanner.nextInt();
		source = scanner.nextInt();
		target = scanner.nextInt();
		arrival = scanner.nextInt() == 1;
		pushTime = arrival ? scanner.nextInt()*0 : scanner.nextInt() * 40 + 160;
		
//		if (arrival) 
//			if(flag ^= true)
//				continue;
//		Route r = qpptw.runSimpleQpptw(arrival ? source : target, arrival ? target : source, arrival, arrival ? 0 : 1000, pushTime);
//		r.sortReservations();

		if(i<800)
			flights.add(new Flight(k++, source, target, time, arrival, pushTime, 10000));
	}scanner.close();
	qpptw = new ASQPPTW(edges, nodes, destNode);
	//qpptw = new QPPTW(edges, nodes);
	//System.out.println((System.currentTimeMillis() - stratTime));
}*/
	
	@SuppressWarnings("unused")
	private void printMinimum() {
		for (int i = 0; i < gates[1].length; i++){
			//System.out.println(gates[1].length + "   "+ Integer.parseInt(gates[1][20]));
			
			Route r = qpptw.runSimpleQpptw(Integer.parseInt(gates[1][i]), 11, true, 500,0);
			r.sortReservations();
			System.out.print(r.getTotalTime()+"	");
			for (Edge e : edges) e.clearWindows(true);
			
			r = qpptw.runSimpleQpptw(Integer.parseInt(gates[1][i]), 42, false, 0,0);
			r.sortReservations();
			System.out.println(r.getTotalTime());
			for (Edge e : edges) e.clearWindows(true);
		}
	}
	
	public void sortFlights() {
		Collections.sort(flights, new Comparator<Flight>() {
			@Override		
			public int compare(Flight f2, Flight f1) {
				int drift = 0;
				return Double.compare(f1.getTime() - (f1.isArrival() ? 0 : (f1.getMinimumRoutingTime() + drift)),  f2.getTime() - (f2.isArrival() ? 0 : (f2.getMinimumRoutingTime() + drift)));
			}
		});
	}
}