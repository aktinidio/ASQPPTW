package routing;

public class Conflict {
	int route1, route2, res1, res2;
	
	public Conflict(int routeID1, int reservationID1, int routeID2, int reservationID2) {
		route1 = routeID1;
		route2 = routeID2;
		res1 = reservationID1;
		res2 = reservationID2;
	}

	public int getRouteID1() {
		return route1;
	}

	public int getRouteID2() {
		return route2;
	}

	public int getReservationID1() {
		return res1;
	}

	public int getReservationID2() {
		return res2;
	}
}

