import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.util.Scanner;

import static org.junit.Assert.*;

public class TestCases {

    @Test
    public void testDGraph() {
        DGraph dgraph = new DGraph(5, "Delta");
        int x = dgraph.getNumNodes();
        System.out.println("Expected 5, got " + x);
        assertEquals(5, x);
    }

//    @Test
//    public void testDGraphReadIn() {
//        DGraph dGraph = null;
//        try {
//            dGraph = read("./PublicTestCases/flights/united.mtx");
//        } catch (IOException e) {
//            System.out.println("Error: " + e);
//        }
//        assert dGraph != null;
//        int x = dGraph.getNumNodes();
//        System.out.println("Expected 5, got " + x);
//        assertEquals(5, x);
//    }
//
//    @Test
//    public void testDGraphAirlineName() {
//        DGraph dGraph = null;
//        try {
//            dGraph = read("./PublicTestCases/flights/united.mtx");
//        } catch (IOException e) {
//            System.out.println("Error: " + e);
//        }
//        assert dGraph != null;
//        String airlineName = dGraph.getAirlineName();
//        System.out.println("Expected United, got " + airlineName);
//        assertEquals("United", airlineName);
//    }
//
//    @Test
//    public void testDGraphEdgeTime() {
//        DGraph dGraph = null;
//        try {
//            dGraph = read("./PublicTestCases/flights/united.mtx");
//        } catch (IOException e) {
//            System.out.println("Error: " + e);
//        }
//        assert dGraph != null;
//        double time = dGraph.getWeight(1, 2, "time");
//        System.out.println("Expected 1.7, got " + time);
//        assertEquals(1.7, time, 0);
//    }
//
//    @Test
//    public void testDGraphEdgeCost() {
//        DGraph dGraph = null;
//        try {
//            dGraph = read("./PublicTestCases/flights/united.mtx");
//        } catch (IOException e) {
//            System.out.println("Error: " + e);
//        }
//        assert dGraph != null;
//        double cost = dGraph.getWeight(1, 2, "cost");
//        System.out.println("Expected 220.5, got " + cost);
//        assertEquals(220.5, cost, 0);
//    }

    @Test
    public void testBCryptCheckPass() {
        String pass = "123456";
        String hashedPass = BCrypt.hashpw(pass, BCrypt.gensalt());

        boolean check = BCrypt.checkpw(pass, hashedPass);
        assertTrue(check);
    }

    @Test
    public void testAirlineRes() {
        AirlineRes airlineRes = new AirlineRes(null, "Delta", new Flight(), false);
        String airlineName = airlineRes.getAirline();
        System.out.println("Should be Delta, got " + airlineName);
        assertEquals("Delta", airlineName);

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
    public DGraph read(String filename) throws IOException {

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


