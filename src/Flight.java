import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.TreeSet;
import java.util.Random;
import java.sql.Time;

/*
 * Usage instructions:
 *
 * The Flight class represents a sequence of cities being visited in
 * a particular order.  Each city is represented with an integer between
 * 1 and numCities inclusive.
 *
 * Construct a Flight as follows:
 *     Flight myFlight = new Flight(numCities);
 *
 * To put a city into the sequence do the following:
 *     myFlight.chooseNextCity(city);
 *
 * To remove the last city that was put into the sequence
 * do the following:
 *     myFlight.unchooseLastCity();
 *
 * To see if the Flight is a valid Flight with all of the cities,
 * call isPossible() and pass in a directed graph that indicates
 * which cities are connected.
 *     myFlight.isPossible(graph)
 *
 * To determine the cost of a Flight, call
 *     myFlight.FlightCost(graph)
 *
 * There are some other handy routines in the below you might
 * want to check out while implementing PA5.
 */
public class Flight {

    private List<Integer> visitOrder;
    private TreeSet<Integer> citiesLeft;

    private int startCity;
    private int arrivalCity;

    private boolean nonstop;
    private DGraph dGraph;
    private String airlineName;
    private String departTime;
    private String arrivalTime;

    private double time;
    private double cost;

    private int numStops;
    //holds T if seat is available and F if seat is taken
    private boolean[] seatAvailable;

    // Constructor that initializes the citiesLeft with all cities 1 through
    // numCities inclusive
    public Flight(DGraph dGraph, int numCities, int startCity, int arrivalCity, String airlineName) {
        this.startCity = startCity;
        this.arrivalCity = arrivalCity;
        this.dGraph = dGraph;

        this.airlineName = airlineName;
        //FIXME figure out why -2
        this.numStops = -2;
        //120 is the total number of seats
        seatAvailable = new boolean[120];
        //keeps track of the cities we have visited so far
        visitOrder = new ArrayList<>();
        //orders the list of cities left for easier traversal
        citiesLeft = new TreeSet<>();
        //since all cities (1 - numCities) are available at time of construction
        for (int i = 1; i <= numCities; i++) {
            citiesLeft.add(i);
        }
        //since all seats are available at time of construction
        for (int i = 0; i < 120; i++) {
            seatAvailable[i] = true;
        }

    }

    //empty constructor which allows us to use copyOtherIntoSelf to make a copy
    public Flight() {

    }

    public void setDepartTime() {
        final Random random = new Random();
        final int millisInDay = 24*60*60*1000;
        Time time = new Time(random.nextInt(millisInDay));
        departTime = String.valueOf(time).substring(0, 5);
        setArrivalTime();
    }

    public String getDepartTime() {
        return departTime;
    }

    private void setArrivalTime() {
        String[] splitStr = departTime.split(":");
        String hour = splitStr[0];
        String min = splitStr[1];
        double departTimeAsDouble = Double.parseDouble(hour + "." + min);
        double arriveTime = time + departTimeAsDouble;
        String arrivalStr = String.format("%.2f", arriveTime);
        arrivalTime = arrivalStr.replace(".", ":");
    }

    public String getArrivalTime() {
        return arrivalTime;
    }



    //returns a String containing the airline company that offers this flight
    public String getAirlineName() {
        return airlineName;
    }

    // Copy another flight into this object for storage (deep copy).
    public void copyOtherIntoSelf(Flight flightSoFar) {
        // Making a copy of the set and list in this data structure.
        citiesLeft = new TreeSet<>(flightSoFar.citiesLeft);
        visitOrder = new ArrayList<>(flightSoFar.visitOrder);
        startCity = flightSoFar.startCity;
        arrivalCity = flightSoFar.arrivalCity;
        dGraph = flightSoFar.dGraph;
        time = flightSoFar.time;
        cost = flightSoFar.cost;
        airlineName = flightSoFar.airlineName;
        numStops = flightSoFar.numStops;
        nonstop = flightSoFar.nonstop;
        departTime = flightSoFar.departTime;
        arrivalTime = flightSoFar.arrivalTime;
    }

    // Has the given city been put in the sequence yet or is it
    // still available? Return true if it is not in the sequence
    // and is still available.
    public boolean isCityAvailable(int city) {
        return citiesLeft.contains(city);
    }

    //returns TRUE if seat is available and FALSE if not
    public boolean isSeatAvailable(int index) {
        return seatAvailable[index];
    }

    //if one of the seats isn't available they can't make that reservation.
    public boolean bookSeats(List<Integer> seats) {
        for (Integer seat : seats) {
            if (!seatAvailable[seat]) {
                return false;

            }
        }
        return true;
    }

    //check if our current city is the destination. If so, we can add the flight
    public int currentCity() {
        return visitOrder.get(visitOrder.size() - 1);
    }

    // Put the given city next in the Flight list.
    public void chooseNextCity(int next) {
        assert isCityAvailable(next);
        visitOrder.add(next);
        citiesLeft.remove(next);
        numStops++;
    }

    // Take off the last city from the list.
    public void unchooseLastCity() {
        assert visitOrder.size() > 0;
        //we use index (visitOrder.size() -1) because we need to take the LAST city visited for backtracking
        int city = visitOrder.get(visitOrder.size() - 1);
        visitOrder.remove(visitOrder.size() - 1);
        citiesLeft.add(city);
        numStops--;
    }

    public double getTime() {
        return time;
    }

    public double getCost() {
        return cost;
    }

    // Returns Double.MAXVALUE if can't find connections.
    // FIXME: this whole method is not clear and needs rewritten
    public double flightCost(DGraph graph, String choice) {
        double cost = 0;
        if (visitOrder.size() == 0) {
            return Double.MAX_VALUE;
        } else if (visitOrder.size() == 1) {
            return 0;
        }
        int prevCity = visitOrder.get(0);

        // visit second city through last and add Flight weights
        for (int i = 1; i < visitOrder.size(); i++) {
            double weight = graph.getWeight(prevCity, visitOrder.get(i), choice);
            prevCity = visitOrder.get(i);
            if (weight < 0) {
                cost = Double.MAX_VALUE;
                break;
            } else {
                cost += weight;
            }
        }

        if (choice.equals("time")) {
            this.time = cost;
        } else {
            this.cost = cost;
        }
        return cost;
    }

    public int getNumStops() {
        return numStops;
    }

    // Provide an ordered list of all of the cities left.
    public List<Integer> citiesLeft() {
        return new ArrayList<>(citiesLeft);
    }

    private String getCityName(int cityNum) {
        String city = "ERROR";
        switch (cityNum) {
            case 1 -> city = "Seattle, WA";
            case 2 -> city = "Salt Lake City, UT";
            case 3 -> city = "San Francisco, CA";
            case 4 -> city = "Las Vegas, NV";
            case 5 -> city = "Los Angeles, CA";
        }

        return city;
    }

    // Print out the Flight and its total cost, time, and number of stops.
    public String toString() {
        double time = flightCost(dGraph, "time");
        double cost = flightCost(dGraph, "cost");
        NumberFormat dollarFormatter = NumberFormat.getCurrencyInstance(Locale.US);
        String formattedCost = dollarFormatter.format(cost);
        //TODO: remove this after testing
        System.out.println("--------------");
        String str = "";
        str += "visitOrder = " + visitOrder;
        str += ", citiesLeft = " + citiesLeft;
        String stops = numStops + " stops.";
        if (numStops == 0) {
            stops = "Nonstop.";
        }
        return dGraph.getAirlineName() + ": DEPARTURE FLIGHT:  This flight from " + getCityName(startCity) +
                " to " + getCityName(arrivalCity) + " departs at: " + departTime + " and takes " + String.format("%.1f", time) +
                " hour(s). It costs " + formattedCost + ". " + stops + "\n" + str;
    }

}
