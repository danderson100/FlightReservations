import java.io.File;
import java.io.IOException;
import java.sql.*;
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
 * java Main
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
        //TODO: Un-comment this to add flights to the database
//        GenerateFlightsDB gen = new GenerateFlightsDB(args);
//        gen.generateFlights();

        //begin user section of program
        welcomeMsg(scanner, null);

        scanner.close();
    }

    /**
     * Purpose: This is the first user interface where a user either logs in with an existing
     * account or creates a new account. Right now it uses simple storage but when finished
     * it will use database and password hashing to store user info.
     * @param scanner, the IO scanner object.
     */
    private static void welcomeMsg(Scanner scanner, User currUser) {
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
                    welcomeMsg(scanner, currUser);
                }
            }
        } catch (InputMismatchException e) {
            System.out.println("Error: " + e);
            System.exit(-1);
        }
        if (currUser != null) {
            successFulLoginInterface(scanner, currUser);
        }
        scanner.close();

    }

    /**
     * Purpose: ADDME
     * @param scanner
     * @param currUser
     */
    private static void successFulLoginInterface(Scanner scanner, User currUser) {
        do {
            System.out.println("Welcome to Reservations!\nWhat would you like to book " +
                    "(type the number for your choice)?");
            System.out.println(" 1. Hotel\n 2. Flight\n 3. Both\n 4. Quit App");
            int selection = 0;
            try {
                selection = scanner.nextInt();
            } catch (InputMismatchException e) {
                System.out.println("Error. Please try again.");
                welcomeMsg(scanner, currUser);
            }

            switch (selection) {
                case 1 -> bookHotel(scanner);
                case 2 -> bookAirline(scanner);
                case 3 -> {
                    bookHotel(scanner);
                    bookAirline(scanner);
                }
                case 4 ->  {
                    System.out.println("Goodbye!");
                    quitApp = true;
                }
                default -> {
                    System.out.println("Error. Try again.");
                    welcomeMsg(scanner, currUser);
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
                loginFailed(scanner);
            } else {
                List<String> otherCreds = attempt.getCredentials(username);
                attempt.setEmail(otherCreds.get(0));
                attempt.setPhone(otherCreds.get(1));
                System.out.println("Logged in as " + username);
                login = true;
            }

        } while (!login);

        return attempt;
    }

    private static void loginFailed(Scanner scanner) {

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
    }

    /**
     * Purpose: add here
     * @param scanner
     */
    private static void bookAirline(Scanner scanner) {
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
        //FIXME: Now that we have a DB of flights, we can pull flights with correct info
        getFlights(departCity, arrivalCity, isNonstop);
        //we need to provide a list of all available flights
        //findAvailableFlights(departCity, arrivalCity, isNonstop, dGraphList);

    }

    private static List<Flight> getFlights(int departCity, int arrivalCity, boolean nonstop) {

        List<Flight> relevantFlights = new ArrayList<>();
        Flight flight = new Flight();
        flight.setDepartCity(departCity);
        flight.setArrivalCity(arrivalCity);
        flight.setNonStop(nonstop);

        String checkPassQuery;
        try {
            Connection conn = DriverManager.getConnection(CONNECTION_STRING);
            Statement statement = conn.createStatement();
            if (!nonstop) {
                checkPassQuery = "SELECT * FROM flights WHERE dCity='" + departCity + "' AND aCity='" + arrivalCity +"'";
            } else {
                checkPassQuery = "SELECT * FROM flights WHERE dCity='" + departCity + "' AND aCity='" + arrivalCity +
                        "' AND numStops='0'";
            }

            ResultSet results = statement.executeQuery(checkPassQuery);
            while (results.next()) {
                double duration = results.getDouble("duration");
                String cost = results.getString("cost");
                String ID = results.getString("ID");
                String airline = results.getString("airline");
                //FIXME find out how to remove as List
                //String visitOrder = results.getString("visitOrder");
                String departTime = results.getString("dTime");

                flight.setDepartTime(departTime);
                flight.setID(ID);


            }
        } catch (SQLException e) {
            System.out.println("Error: " + e);
        }

        return relevantFlights;
    }
    //short helper method to print the city names
    private static void printCities() {
        System.out.println("\t 1. Seattle, WA");
        System.out.println("\t 2. Salt Lake City, UT");
        System.out.println("\t 3. San Francisco, CA");
        System.out.println("\t 4. Las Vega, NV");
        System.out.println("\t 5. Los Angeles, CA");
    }

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
     * Purpose:
     * @param scanner
     */
    private static void bookHotel(Scanner scanner) {
        System.out.println("Booking hotel....");
    }

}
