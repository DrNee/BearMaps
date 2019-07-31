import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Graph for storing all of the intersection (vertex) and road (edge) information.
 * Uses your GraphBuildingHandler to convert the XML files into a graph. Your
 * code must include the vertices, adjacent, distance, closest, lat, and lon
 * methods. You'll also need to include instance variables and methods for
 * modifying the graph (e.g. addNode and addEdge).
 *
 * @author Alan Yao, Josh Hug
 */

public class GraphDB {
    /**
     * Your instance variables for storing the graph. You should consider
     * creating helper classes, e.g. Node, Edge, etc.
     */
    HashMap<Long, Node> idToNode = new HashMap<Long, Node>();
    // dictionary of id and nodes
    HashMap<Long, Edge> idToEdge = new HashMap<Long, Edge>();
    // dictionary of id and edge

    /**
     * Example constructor shows how to create and start an XML parser.
     * You do not need to modify this constructor, but you're welcome to do so.
     *
     * @param dbPath Path to the XML file to be parsed.
     */

    public GraphDB(String dbPath) {
        try {
            File inputFile = new File(dbPath);
            FileInputStream inputStream = new FileInputStream(inputFile);
            // GZIPInputStream stream = new GZIPInputStream(inputStream);

            SAXParserFactory factory = SAXParserFactory.newInstance();
            SAXParser saxParser = factory.newSAXParser();
            GraphBuildingHandler gbh = new GraphBuildingHandler(this);
            saxParser.parse(inputStream, gbh);
        } catch (ParserConfigurationException | SAXException | IOException e) {
            e.printStackTrace();
        }
        clean();
    }

    // Edges are not between two nodes - rather they can go through many nodes (ways)
    // Since we know the nodes that the way goes through are connected in linear order,
    // we can update the adj list of each node that the edge goes through

    /**
     * Helper to process strings into their "cleaned" form, ignoring punctuation and capitalization.
     *
     * @param s Input string.
     * @return Cleaned string.
     */
    static String cleanString(String s) {
        return s.replaceAll("[^a-zA-Z ]", "").toLowerCase();
    }

    static double distance(double lonV, double latV, double lonW, double latW) {
        double phi1 = Math.toRadians(latV);
        double phi2 = Math.toRadians(latW);
        double dphi = Math.toRadians(latW - latV);
        double dlambda = Math.toRadians(lonW - lonV);

        double a = Math.sin(dphi / 2.0) * Math.sin(dphi / 2.0);
        a += Math.cos(phi1) * Math.cos(phi2) * Math.sin(dlambda / 2.0) * Math.sin(dlambda / 2.0);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return 3963 * c;
    }

    static double bearing(double lonV, double latV, double lonW, double latW) {
        double phi1 = Math.toRadians(latV);
        double phi2 = Math.toRadians(latW);
        double lambda1 = Math.toRadians(lonV);
        double lambda2 = Math.toRadians(lonW);

        double y = Math.sin(lambda2 - lambda1) * Math.cos(phi2);
        double x = Math.cos(phi1) * Math.sin(phi2);
        x -= Math.sin(phi1) * Math.cos(phi2) * Math.cos(lambda2 - lambda1);
        return Math.toDegrees(Math.atan2(y, x));
    }

    /**
     * Remove nodes with no connections from the graph.
     * While this does not guarantee that any two nodes in the remaining graph are connected,
     * we can reasonably assume this since typically roads are connected.
     */
    private void clean() {
        ArrayList<Long> connectionlessIDs = new ArrayList<>();
        for (Node n : idToNode.values()) {
            if (n.adjacentNodes.isEmpty()) {
                // has no adjacent nodes
                connectionlessIDs.add(n.id);
                // add to list of removed nodes
            }
        }
        for (long id : connectionlessIDs) {
            idToNode.remove(id);
            // remove those nodes
        }
    }

    /**
     * Returns an iterable of all vertex IDs in the graph.
     *
     * @return An iterable of id's of all vertices in the graph.
     */
    Iterable<Long> vertices() {
        return idToNode.keySet();
        // ids are the keys
    }

    /**
     * Returns ids of all vertices adjacent to v.
     *
     * @param v The id of the vertex we are looking adjacent to.
     * @return An iterable of the ids of the neighbors of v.
     */
    Iterable<Long> adjacent(long v) {
        ArrayList<Long> adjacentIDs = new ArrayList<>();
        // list to store adj nodes
        for (Node n : idToNode.get(v).adjacentNodes) {
            // iterate through adj nodes
            adjacentIDs.add(n.id);
            // add adj node to list
        }
        return adjacentIDs;
        //return that list
    }

    /**
     * Returns the great-circle distance between vertices v and w in miles.
     * Assumes the lon/lat methods are implemented properly.
     * <a href="https://www.movable-type.co.uk/scripts/latlong.html">Source</a>.
     *
     * @param v The id of the first vertex.
     * @param w The id of the second vertex.
     * @return The great-circle distance between the two locations from the graph.
     */
    double distance(long v, long w) {
        return distance(lon(v), lat(v), lon(w), lat(w));
    }

    /**
     * Returns the initial bearing (angle) between vertices v and w in degrees.
     * The initial bearing is the angle that, if followed in a straight line
     * along a great-circle arc from the starting point, would take you to the
     * end point.
     * Assumes the lon/lat methods are implemented properly.
     * <a href="https://www.movable-type.co.uk/scripts/latlong.html">Source</a>.
     *
     * @param v The id of the first vertex.
     * @param w The id of the second vertex.
     * @return The initial bearing between the vertices.
     */
    double bearing(long v, long w) {
        return bearing(lon(v), lat(v), lon(w), lat(w));
    }

    /**
     * Returns the vertex closest to the given longitude and latitude.
     *
     * @param lon The target longitude.
     * @param lat The target latitude.
     * @return The id of the node in the graph closest to the target.
     */
    long closest(double lon, double lat) {
        double minDist = Double.MAX_VALUE;
        // running min value for compares
        long closestId = 0;
        // running id of closest vertex so far
        for (Node n : idToNode.values()) {
            // iterate through graph's nodes
            double dist = distance(lon, lat, n.longitude, n.latitude);
            // compute distance from node to lat and lon
            if (dist < minDist) {
                minDist = dist;
                closestId = n.id;
                // if smaller than running min, update minDist and closestId
            }
        }
        return closestId;
    }

    /**
     * Gets the longitude of a vertex.
     *
     * @param v The id of the vertex.
     * @return The longitude of the vertex.
     */
    double lon(long v) {
        return idToNode.get(v).longitude;
    }

    /**
     * Gets the latitude of a vertex.
     *
     * @param v The id of the vertex.
     * @return The latitude of the vertex.
     */
    double lat(long v) {
        return idToNode.get(v).latitude;
    }

    void addNode(long id, double lon, double lat) {
        idToNode.put(id, new Node(id, lon, lat));
        // add to nodes map
    }

    void addEdge(long id, ArrayList<Node> connected) {
        idToEdge.put(id, new Edge(id, connected));
        // add to edge map
        // creating the edge (the way) will update
        // all the adj lists of each reference node
        // in the way block

    }

    class Node implements Comparable<Node> {
        long id;
        double longitude;
        double latitude;
        double distFromSource;
        // will be set in A*, given a source
        double estimatedDistanceToGoal;
        // will be computed in A*, given a target
        Node parent;
        // previous node in shortest path traversal

        ArrayList<Node> adjacentNodes = new ArrayList<>();

        Node(long id, double lon, double lat) {
            this.id = id;
            this.longitude = lon;
            this.latitude = lat;
        }

        void addAdjNode(Node adj) {
            adjacentNodes.add(adj);
            // update adj list
        }

        @Override
        public int compareTo(Node o) {
            return Double.compare(distFromSource + estimatedDistanceToGoal,
                    o.distFromSource + o.estimatedDistanceToGoal);
            // priority is known distance + heuristic
        }
    }

    class Edge {
        long id; // way id
        ArrayList<Node> refNodes; // all the nd ref nodes

        Edge(long id, ArrayList<Node> refNodes) {
            this.id = id;
            this.refNodes = refNodes;
            connectNodes(refNodes); // update adj list of each node
        }

        void connectNodes(ArrayList<Node> connectedNodes) {
            // update the adj list of each reference node
            // in a way block because they are connected
            // in linear order
            // nd ref 1 <--> nd ref 2 <--> .... <--> nd ref k

            if (connectedNodes.size() == 1) {
                return;
            }
            for (int i = 0; i < connectedNodes.size(); i++) {
                Node refNode = connectedNodes.get(i);
                // get nd ref node
                if (i == 0) {
                    refNode.addAdjNode(connectedNodes.get(i + 1));
                    // nd ref 1 connects to nd ref 2
                } else if (i == connectedNodes.size() - 1) {
                    refNode.addAdjNode(connectedNodes.get(i - 1));
                    // nd ref k connects to nd ref k - 1 (k is last)
                } else {
                    refNode.addAdjNode(connectedNodes.get(i + 1));
                    refNode.addAdjNode(connectedNodes.get(i - 1));
                    // nd ref node i in between connect to
                    // nd ref node i + 1 and i - 1
                }

            }
        }
    }
}
