import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.*;

/*
 * AUTHOR: David Anderson
 * FILE: Main.java
 * PURPOSE: This is the primary class for my flight and hotel reservations program. It takes as input Sparse Matrix
 * files and creates directed, doubly-weighted graphs where nodes represent cities and edges represent
 * flight paths with varying costs and durations in hours. Users can choose a flight based on speed or cost, or
 * a mix of both for the optimal choice.
 *
 * USAGE:
 * java Main infile1.mtx infile2.mtx infile3.mtx
 *
 * where infile is the name of an input file in the following format:
 *
 * ----------- EXAMPLE INPUT -------------
 * Input file:
 *
 * American Airlines
 * %%MatrixMarket matrix coordinate real general
 * %-------------------------------------------------------------------------------
 * %
 * % Flying times and costs between some US cities.
 * % 1: Seattle, WA
 * % 2: Salt Lake City, UT
 * % 3: San Francisco, CA
 * % 4: Las Vegas, NV
 * % 5: Los Angeles, CA
 * %
 * % author: David Anderson
 * % kind: directed weighted graph
 * %-------------------------------------------------------------------------------
 * 5 5 20
 * 1 2 1.92 210.50
 * 2 1 2.1 210.50
 * 1 5 2.5 245.66
 * 5 1 2.67 245.66
 * 2 5 1.92 304.10
 * 5 2 1.55 304.10
 * 2 4 1.33 100.0
 * 4 2 1.33 100.0
 * 1 4 2.4 204.8
 * 4 1 2.5 204.8
 * 1 3 1.2 430.75
 * 3 1 1.2 430.75
 * 2 3 1.0 150.95
 * 3 2 1.0 150.95
 * 5 4 1.4 505.0
 * 4 5 1.4 505.0
 * 3 4 2.3 318.43
 * 4 3 2.3 318.43
 * 5 3 0.9 99.0
 * 3 5 0.9 99.0
 * -------------------------------------------
 */
public class Main {
    //temporary global variable to quit the program
    private static boolean quitApp = false;

    private static final String DB_NAME = "flightres.db";
    private static final String CONNECTION_STRING = "jdbc:sqlite:" + DB_NAME;

    public static void main(String[] args) {
        //initialize the scanner and user collection we'll be using throughout the app
        Scanner scanner = new Scanner(System.in);
        //add them all to the database
        GenerateFlightsDB gen = new GenerateFlightsDB(args);
        gen.generateFlights();

        DGraph unitedDgraph = null;
        DGraph deltaDgraph = null;
        DGraph americanDgraph = null;

        try {
            unitedDgraph = read(args[0]);
            deltaDgraph = read(args[1]);
            americanDgraph = read(args[2]);
        } catch (IOException e) {
            System.out.println("Error: " + e);
            System.exit(1);
        }

        List<DGraph> dGraphList = new ArrayList<>();
        dGraphList.add(unitedDgraph);
        dGraphList.add(deltaDgraph);
        dGraphList.add(americanDgraph);
        //begin user section of program
        welcomeMsg(scanner, dGraphList, null);

        scanner.close();
    }

    /**
     * Purpose: This is the first user interface where a user either logs in with an existing
     * account or creates a new account. Right now it uses simple storage but when finished
     * it will use database and password hashing to store user info.
     * @param scanner, the IO scanner object.
     * @param dGraphList, the list of all directed graphs which will become flights
     */
    private static void welcomeMsg(Scanner scanner, List<DGraph> dGraphList, User currUser) {
        System.out.println("Welcome! Please select an option: ");
        System.out.println("\t1. Login\n \t2. Create Account");

        int loginSelection;
        try {
            loginSelection = scanner.nextInt();
            switch (loginSelection) {
                case 1 -> currUser = login(scanner);
                case 2 -> {
                    createAccount(scanner);
                    currUser = login(scanner);
                }
                default -> {
                    System.out.println("Error! Please try again");
                    welcomeMsg(scanner, dGraphList, currUser);
                }
            }
        } catch (InputMismatchException e) {
            System.out.println("Error: " + e);
            System.exit(-1);
        }
        if (currUser != null) {
            successFulLoginInterface(scanner, currUser, dGraphList);
        }
        scanner.close();

    }

    /**
     * Purpose: ADDME
     * @param scanner
     * @param currUser
     * @param dGraphList
     */
    private static void successFulLoginInterface(Scanner scanner, User currUser,
                                                 List<DGraph> dGraphList) {
        do {
            System.out.println("Welcome to Reservations!\nWhat would you like to book " +
                    "(type the number for your choice)?");
            System.out.println(" 1. Hotel\n 2. Flight\n 3. Both\n 4. Quit App");
            int selection = 0;
            try {
                selection = scanner.nextInt();
            } catch (InputMismatchException e) {
                System.out.println("Error. Please try again.");
                welcomeMsg(scanner, dGraphList, currUser);
            }

            switch (selection) {
                case 1 -> bookHotel(scanner);
                case 2 -> bookAirline(scanner, dGraphList);
                case 3 -> {
                    bookHotel(scanner);
                    bookAirline(scanner, dGraphList);
                }
                case 4 ->  {
                    System.out.println("Goodbye!");
                    quitApp = true;
                }
                default -> {
                    System.out.println("Error. Try again.");
                    welcomeMsg(scanner, dGraphList, currUser);
                }
            }
        } while (!quitApp);
    }

    /**
     * Purpose: ADD HERE
     * @param scanner
     */
    private static void createAccount(Scanner scanner) {
        //if account creation is successful, call login
        System.out.println("Please enter your desired username: ");
        String username = scanner.next();
        System.out.println("Enter a password: ");
        String pass = scanner.next();
        User user = new User(username, pass);
        System.out.println("Please enter your email: ");
        String email = scanner.next();
        user.setEmail(email);
        System.out.println("Please enter a phone number: ");
        String phone = scanner.next();
        user.setPhone(phone);
        //add login to DB
        if (user.generateLogin()) {
            System.out.println("User " + username + " added!");
        } else {
            System.out.println("Account creation failed. Try again.");
            createAccount(scanner);
        }



    }

    /**
     * Purpose ADD HERe
     * @param scanner
     * @return
     */
    private static User login(Scanner scanner) {
        boolean login = false;
        User attempt;

        do {
            System.out.println("Log in: ");
            System.out.println("Please enter your username: ");
            String username = scanner.next();
            System.out.println("Please enter your password: ");
            String pass = scanner.next();

            attempt = new User(username, pass);

            boolean check = attempt.validateLogin(username, pass);

            if (!check) {
                System.out.println("Login failed. What would you like to do?");
                System.out.println("\t1. Try again");
                System.out.println("\t2. Create Account");
                System.out.println("\t3. Exit");
                int choice = scanner.nextInt();
                switch (choice) {
                    case 1 -> login(scanner);
                    case 2 -> createAccount(scanner);
                    case 3 -> {
                        System.out.println("Goodbye!");
                        System.exit(1);
                    }
                }

            } else {
                List<String> otherCreds = attempt.getCredentials(username);
                attempt.setEmail(otherCreds.get(0));
                attempt.setPhone(otherCreds.get(1));
                System.out.println("Login in as " + username);
                login = true;
            }

        } while (!login);

        return attempt;
    }

    /**
     * Purpose: add here
     * @param scanner
     * @param dGraphList
     */
    private static void bookAirline(Scanner scanner, List<DGraph> dGraphList) {
        int departCity = -1;
        int arrivalCity = -1;

        System.out.println("Please select your departure CITY: ");
        printCities();
        try {
            departCity = scanner.nextInt();
        } catch (InputMismatchException e) {
            System.out.println("Error: " + e);
        }
        System.out.println("Where are you going (arrival CITY)? ");
        printCities();
        try {
            arrivalCity = scanner.nextInt();
        } catch (InputMismatchException e) {
            System.out.println("Error: " + e);
        }
        boolean isNonstop = false;
        System.out.println("Please enter departure date in THIS FORMAT (MM/DD/YYYY): ");
        String departureDate = scanner.next();
        System.out.println("Please enter return date in THIS FORMAT (MM/DD/YYYY): ");
        String returnDate = scanner.next();
        System.out.println("Nonstop only? Type YES or NO");
        String nonstop = scanner.next().toUpperCase();
        if (nonstop.equals("YES")) {
            isNonstop = true;
        }
        //we need to provide a list of all available flights
        //findAvailableFlights(departCity, arrivalCity, isNonstop, dGraphList);


    }
    //short helper method to print the city names
    private static void printCities() {
        System.out.println("\t 1. Seattle, WA");
        System.out.println("\t 2. Salt Lake City, UT");
        System.out.println("\t 3. San Francisco, CA");
        System.out.println("\t 4. Las Vega, NV");
        System.out.println("\t 5. Los Angeles, CA");
    }

//    /**
//     * Purpose:
//     * @param departCity
//     * @param arrivalCity
//     * @param nonstop
//     * @param dGraphList
//     */
//    private static void findAvailableFlights(int departCity, int arrivalCity,
//                                             boolean nonstop, List<DGraph> dGraphList) {
//        //stores all the airline flights, so it should be size 3 at the end
//        Map<String, List<Flight>> airlineFlights = new HashMap<>();
//        //this stores one airline's flights
//        List<Flight> flights = null;
//
//        for (DGraph dGraph : dGraphList) {
//            //iterates over the three airlines
//            Flight flight = new Flight(dGraph, 5, departCity, arrivalCity, nonstop, dGraph.getAirlineName());
//            flight.chooseNextCity(departCity);
//
//            if (nonstop) {
//                flights = getNonstopFlights(arrivalCity, flight);
//            } else {
//                //find all permutations where the last city = arrivalCity
//                flights = getAllFlights(departCity, arrivalCity, dGraph, flight);
//            }
//            String airline = dGraph.getAirlineName();
//            airlineFlights.put(airline, flights);
//
//        }
//        printFlights(airlineFlights);
//        checkOrganizationMethod(flights, airlineFlights, nonstop);
//
//    }

    /**
     * Purpose:
     * @param flights
     * @param airlineFlights
     * @param nonstop
     */
    private static void checkOrganizationMethod(List<Flight> flights, Map<String, List<Flight>> airlineFlights,
                                                boolean nonstop) {
        System.out.println("How would you like to order the available DEPARTURE flights?");
        System.out.println("\t1. Cost (Cheapest to Most Expensive)");
        System.out.println("\t2. Time (Fastest to Longest)");
        System.out.println("\t3. Blend (Best overall flight)");
        Scanner scanner = new Scanner(System.in);
        int choice = 0;
        try {
            choice = scanner.nextInt();
        } catch (InputMismatchException e) {
            System.out.println("Error: " + e);
        }

        organizeFlights(flights, airlineFlights, nonstop, choice);
    }

    /**
     * Purpose:
     * @param flights
     * @param airlineFlights
     * @param nonstop
     * @param choice
     */
    private static void organizeFlights(List<Flight> flights, Map<String, List<Flight>> airlineFlights,
                                        boolean nonstop, int choice) {

        Map<Double, Flight> storage = new TreeMap<>();

        switch (choice) {
            case 1 -> System.out.println("chose cheapest");
            case 2 -> System.out.println("Chose fastest");
            case 3 -> System.out.println("Chose the blend");
        }
    }

    /**
     * Purpose: A short helper method that prints the available flights from each airline.
     * @param airlineFlights, is the Map of ArrayLists of flights from each airline.
     */
    private static void printFlights(Map<String, List<Flight>> airlineFlights) {
        for (String airline : airlineFlights.keySet()) {
            List<Flight> currAirlineFlights = airlineFlights.get(airline);
            for (Flight flight : currAirlineFlights) {
                System.out.println(flight.toString());
            }
        }
    }

    /**
     * Purpose:
     * @param arrivalCity
     * @param currFlight
     * @return
     */
    private static List<Flight> getNonstopFlights(int arrivalCity, Flight currFlight) {
        List<Flight> flights = new ArrayList<>();
        currFlight.chooseNextCity(arrivalCity);
        Flight flight = new Flight();
        flight.copyOtherIntoSelf(currFlight);
        flight.setDepartTime();
        flights.add(flight);

        storeFlights(flights);
        return flights;
    }

    private static void storeFlights(List<Flight> flights) {
        for (Flight flight : flights) {
            String airline = "";
            try {
                String airlineName = flight.getAirlineName();
                switch (airlineName) {
                    case "United" -> airline = "UnitedFlights";
                    case "American" -> airline = "AmericanFlights";
                    case "Delta" -> airline = "DeltaFlights";
                }
                Connection conn = DriverManager.getConnection(CONNECTION_STRING);
                Statement statement = conn.createStatement();
                String query = "INSERT INTO " + airline + " (flight) " + "VALUES('" +
                        flight + "')";
                statement.executeQuery(query);
            } catch (SQLException e) {
                System.out.println("Error: " + e);
            }
        }

    }

    /**
     * Purpose:
     * @param departCity
     * @param arrivalCity
     * @param dGraph
     * @param currFlight
     * @return
     */
    private static List<Flight> getAllFlights(int departCity, int arrivalCity, DGraph dGraph, Flight currFlight) {
        //this will store the order in which the flight reaches each city.
        List<Integer> visitOrder = new ArrayList<>();
        //we add departcity here because it will always depart from the same city
        visitOrder.add(departCity);

        List<Flight> flights = new ArrayList<>();

        return getAllFlightsHelper(dGraph, currFlight, visitOrder, arrivalCity, 0, flights);
    }

    /**
     * Purpose:
     * @param dGraph
     * @param currFlight
     * @param visitOrder
     * @param arrivalCity
     * @param count
     * @param flights
     * @return
     */
    private static List<Flight> getAllFlightsHelper(DGraph dGraph, Flight currFlight, List<Integer> visitOrder,
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

    /**
     * Purpose:
     * @param scanner
     */
    private static void bookHotel(Scanner scanner) {
        System.out.println("Booking hotel....");
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
    public static DGraph read(String filename) throws IOException {

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
