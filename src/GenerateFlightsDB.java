import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.*;

public class GenerateFlightsDB {

    private static final String DB_NAME = "flightres.db";
    private static final String CONNECTION_STRING = "jdbc:sqlite:" + DB_NAME;

    private final String[] filenames;

    public GenerateFlightsDB(String[] filenames) {
        this.filenames = filenames;
    }

    public void generateFlights() {
        List<DGraph> dGraphList = generateDGraphs();

        findAvailableFlights(1, 5, dGraphList);

//        for (int startCity = 1; startCity < 5; startCity++) {
//            for (int arrivalCity = startCity+1; arrivalCity < 6; arrivalCity++) {
//                //generates all flights from 1 to 5, 2 to 5, 3 to 5, etc...
//                findAvailableFlights(startCity, arrivalCity, dGraphList);
//            }
//        }
    }

    private List<DGraph> generateDGraphs() {

        List<DGraph> dGraphList = new ArrayList<>();

        DGraph unitedDgraph;
        DGraph deltaDgraph;
        DGraph americanDgraph;

        try {
            unitedDgraph = read(filenames[0]);
            americanDgraph = read(filenames[1]);
            deltaDgraph = read(filenames[2]);

            dGraphList.add(unitedDgraph);
            dGraphList.add(americanDgraph);
            dGraphList.add(deltaDgraph);

        } catch (IOException e) {
            System.out.println("Error " + e);

        }
        return dGraphList;

    }

    /**
     * Purpose:
     *
     * @param departCity
     * @param arrivalCity
     * @param dGraphList
     */
    private void findAvailableFlights(int departCity, int arrivalCity, List<DGraph> dGraphList) {
        //stores all the airline flights, so it should be size 3 at the end
       // Map<String, List<Flight>> airlineFlights = new HashMap<>();
        //this stores one airline's flights
        List<Flight> flights;

        for (DGraph dGraph : dGraphList) {
            //iterates over the three airlines
            Flight flight = new Flight(dGraph, 5, departCity, arrivalCity, dGraph.getAirlineName());
            flight.chooseNextCity(departCity);

            //find all permutations where the last city = arrivalCity
            flights = getAllFlights(departCity, arrivalCity, dGraph, flight);

           // String airline = dGraph.getAirlineName();
          //  airlineFlights.put(airline, flights);
            storeFlights(flights);
        }



    }

    /**
     * Purpose:
     *
     * @param departCity
     * @param arrivalCity
     * @param dGraph
     * @param currFlight
     * @return
     */
    private List<Flight> getAllFlights(int departCity, int arrivalCity, DGraph dGraph, Flight currFlight) {
        //this will store the order in which the flight reaches each city.
        List<Integer> visitOrder = new ArrayList<>();
        //we add departcity here because it will always depart from the same city
        visitOrder.add(departCity);

        List<Flight> flights = new ArrayList<>();

        return getAllFlightsHelper(dGraph, currFlight, visitOrder, arrivalCity, 0, flights);
    }

    /**
     * Purpose:
     *
     * @param dGraph
     * @param currFlight
     * @param visitOrder
     * @param arrivalCity
     * @param count
     * @param flights
     * @return
     */
    private List<Flight> getAllFlightsHelper(DGraph dGraph, Flight currFlight, List<Integer> visitOrder,
                                             int arrivalCity, int count, List<Flight> flights) {

        List<Integer> neighbors = dGraph.getNeighbors(visitOrder.get(count));
        //base case - if there are no permutations that end at the arrival city left
        if (currFlight.currentCity() == arrivalCity) {
            //IMPORTANT!!!! We need to make a copy of the flight and add THAT to flights
            //Otherwise the pointer continues to point at the currFlight object and updates it with the others.
            Flight flight = new Flight();
            flight.copyOtherIntoSelf(currFlight);
            //FIXME: This isn't storing the correct departure/arrival times
            flight.setDepartTime();
            flights.add(flight);

        } else {
            //recursive case
            for (Integer neighbor : neighbors) {
                if (currFlight.isCityAvailable(neighbor)) {
                    //choose
                    currFlight.chooseNextCity(neighbor);
                    visitOrder.add(neighbor);
                    //explore
                    getAllFlightsHelper(dGraph, currFlight, visitOrder, arrivalCity, count + 1, flights);
                    //un-choose
                    currFlight.unchooseLastCity();
                    visitOrder.remove(neighbor);
                }
            }
        }
        return flights;
    }

    private static void storeFlights(List<Flight> flights) {
        String ID = "";
        int idNum = 100;
        for (Flight flight : flights) {
            String airline = "";
            try {
                String airlineName = flight.getAirlineName();
                switch (airlineName) {
                    case "United" -> airline = "UnitedFlights";
                    case "American" -> airline = "AmericanFlights";
                    case "Delta" -> airline = "DeltaFlights";
                }
                ID = airline.charAt(0) + String.valueOf(idNum);
                Connection conn = DriverManager.getConnection(CONNECTION_STRING);
                Statement statement = conn.createStatement();
                String query = "INSERT INTO flights (airline, duration, cost, numStops, obj, ID) " +
                        "VALUES('" + airline + "', '" + flight.getTime() + "', '" + flight.getCost() +
                        "', '" + flight.getNumStops() +"', '" + flight + "', '" + ID + "')";

//                String query = "INSERT INTO users (username, pass, email, phone) " +
//                        "VALUES('" + name + "', '" + hashedPW + "', '" + email + "', '" + phone +"')";
                statement.executeUpdate(query);
            } catch (SQLException e) {
                System.out.println("Error: " + e);
            }
            idNum++;
        }

    }

    /**
     * Purpose: This method takes in an .mtx Sparse matrix file and reads the data
     * to create a DGraph object containing all of the nodes, edges, and weights.
     * The edges hold two weights: cost and time.
     *
     * @param filename, is the name of the .mtx file in the working directory
     * @return dGraph, is the Directed Graph with all the graph information
     * @throws IOException, will be thrown if the file cannot be found
     */
    private DGraph read(String filename) throws IOException {

        Scanner scanner = new Scanner(new File(filename));
        String line = "";
        //TODO: CLEAN THIS UP
        String airlineNameStr = scanner.nextLine();
        String[] airlineSplit = airlineNameStr.split("\\s");
        String airlineName = airlineSplit[0];
        // read comment lines if any
        boolean comment = true;
        while (comment) {
            line = scanner.nextLine();
            comment = line.startsWith("%");
        }
        String[] numCitiesStr = line.split("\\s+");
        int numCities = Integer.parseInt(numCitiesStr[0]);
        DGraph dGraph = new DGraph(numCities, airlineName);
        //data section
        while (scanner.hasNext()) {
            line = scanner.nextLine();
            if (line == null) break;
            String[] str = line.split("\\s+");
            int i = Integer.parseInt(str[0].trim());
            int j = Integer.parseInt(str[1].trim());

            double time = Double.parseDouble(str[2].trim());
            double cost = Double.parseDouble((str[3].trim()));
            dGraph.addEdge(i, j, time, cost);
        }
        scanner.close();
        return dGraph;
    }
}
