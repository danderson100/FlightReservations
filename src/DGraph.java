import java.util.*;
/**
 * File: DGraph.java
 *
 * Represents a directed graph. The nodes are represented as
 * integers ranging from 1 to num_nodes inclusive.
 * The weights are assumed to be >= zero. Each edge has two weights,
 * COST and TIME.
 *
 * Usage instructions:
 *
 * Construct a DGraph
 * DGraph graph = new DGraph(numNodes);
 *
 * Add an edge
 * graph.addEdge(v, w, weight);
 *
 * Other useful methods:
 * graph.getWeight(v,w)
 * graph.getNumNodes()
 * List<Integer> list = graph.getNeighbors(v);
 *
 */
public class DGraph {

    //an ArrayList to store all edges
    private final List<Edge> edges = new ArrayList<>();
    //this maps a city to a List of integers containing its neighbors
    private final Map<Integer, List<Integer>> neighborsMap = new HashMap<>();

    private final int numNodes;
    private final String airLineName;

    /*
     * Constructs an instance of the DGraph class with # nodes numNodes.
     */
    public DGraph(int numNodes, String airlineName) {
        this.numNodes = numNodes;
        this.airLineName = airlineName;
    }

    public String getAirlineName() {
        return airLineName;
    }

    /**
     * Adds the directed edge (v,w) to the graph including updating the node
     * count appropriately.
     *
     * @param v is the first node/city
     * @param w is the second node/city
     * @param cost is the monetary cost to travel from city V to city W
     * @param time is the time in hours to travel from city V to city W
     */
    public void addEdge(int v, int w, double time, double cost) {
        Edge edge = new Edge(v, w, time, cost);
        addNeighbors(v, w);
        edges.add(edge);
    }

    /*
     * Returns the number of nodes in this graph.
     */
    public int getNumNodes() {
        return numNodes;
    }

    // Returns the weight for the given edge.
    // Returns -1 if there is no edge between the nodes node1 and node2.
    public double getWeight(int node1, int node2, String choice) {
        double weight = -1;

        if (choice.equals("cost")) {
            //then calculate the monetary cost of this edge
            for (Edge edge : edges) {
                if (edge.node1 == node1 && edge.node2 == node2) {
                    weight = edge.cost;
                    break;
                }
            }
        } else if (choice.equals("time")) {
            //calculate the distance between the two nodes
            for (Edge edge : edges) {
                if (edge.node1 == node1 && edge.node2 == node2) {
                    weight = edge.time;
                    break;
                }
            }
        } else {
            //do both
        }


        return weight;
    }

    /*
     * Purpose: This private method takes in a node and that
     * node's neighbor, and adds it to their associated List
     * of neighbors.
     *
     * @param node, is the node having its neighbors updated
     *
     * @param neighbor, is the new neighbor
     */
    private void addNeighbors(int node, int neighbor) {
        if (node != neighbor) {
            if (neighborsMap.get(node) != null) {
                List<Integer> neighbors = neighborsMap.get(node);
                neighbors.add(neighbor);
                neighborsMap.replace(node, neighbors);
            } else {
                List<Integer> newNeighborsList = new ArrayList<>();
                newNeighborsList.add(neighbor);
                neighborsMap.put(node, newNeighborsList);
            }
        }
    }

    /**
     * For a given node returns a sorted list of all its neighbors.
     *
     * @param node
     *            Node identifier
     * @return A sorted list of node's neighbors.
     */
    public List<Integer> getNeighbors(int node) {
        Collections.sort(neighborsMap.get(node));
        return neighborsMap.get(node);
    }

    /* --------------------------------------- */
    /*
     * You should not need to touch anything below this line,
     * except for maybe the name edges in the for each loop just below
     * in the toDotString method if you named your collection of edges
     * differently.
     */
    // Create a dot representation of the graph.
    public String toDotString() {
        String dot_str = "digraph {\n";
        // Iterate over the edges in order.
        // The error is obvious as you need to use a proper data structure first
        // that stores edges.
        for (Edge e : edges) {
            dot_str += e.toDotString() + "\n";
        }
        return dot_str + "}\n";
    }

    /**
     * Immutable undirected edges.
     */
    public class Edge implements Comparable<Edge> {

        // Nodes in edge and weight on edge
        private final int node1;
        private final int node2;
        private final double time;
        private final double cost;

        /**
         * Stores the given nodes with smaller id first.
         *
         * @param node1
         * @param node2
         */
        public Edge(int node1, int node2, double time, double cost) {
            assert time >= 0.0;
            assert cost >= 0.0;
            this.node1 = node1;
            this.node2 = node2;
            this.time = time;
            this.cost = cost;
        }

        /**
         * @return an directed edge string in dot format
         */
        public String toDotString() {
            return "" + node1 + " -> " + node2 + " [Time(hours)=" + time
                    + " Cost=" + cost + "];";
        }

        /**
         * Lexicographical ordering on edges (node1,node2).
         */
        public int compareTo(Edge other) {
            if (this.equals(other)) {
                return 0; // this and other are equal
            } else if ((node1 < other.node1)
                    || (node1 == other.node1 && node2 < other.node2)) {
                return -1; // this is less than other
            } else {
                return 1; // this is greater than other
            }
        }

        /**
         * Lexicographical ordering on edges (node1,node2).
         */
        public boolean equals(Object o) {
            if (!(o instanceof Edge)) {
                return false;
            }
            Edge other = (Edge) o;
            return (node1 == other.node1) && (node2 == other.node2);
        }

        /**
         * Know number of nodes when read in input file, so can give each edge a
         * unique hash code.
         */
        public int hashCode() {
            return getNumNodes() * node1 + node2;
        }
    }

}
