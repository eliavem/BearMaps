import org.xml.sax.SAXException;

import java.io.File;
import java.io.IOException;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

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
    /** Your instance variables for storing the graph. You should consider
     * creating helper classes, e.g. Node, Edge, etc. */

    /**
     * Example constructor shows how to create and start an XML parser.
     * You do not need to modify this constructor, but you're welcome to do so.
     * @param dbPath Path to the XML file to be parsed.
     */

    static ArrayList<Vertex> vlist;
    static Map<Long, Vertex> idVertexMap;
    class Vertex {
        long id;
        double lat;
        double lon;
        LinkedList<Vertex> neighbors;
        String tag;

        Vertex(String id, String lon, String lat) {
            long iD = Long.parseLong(id);
            double lati = Double.parseDouble(lat);
            double longi = Double.parseDouble(lon);

            this.id = iD;
            this.lat = lati;
            this.lon = longi;
            this.neighbors = new LinkedList<>();
            this.tag = "";

        }

        //Checks of this vertex is already a neighbor with that vertex.
        public boolean checkIfAlreadyNeighbor(Vertex that) {
            for (Vertex i: this.neighbors) {
                if (i == that) {
                    return true;
                }
            }
            return false;
        }

        //Call the "checkIfAlreadyNeighbor" method before calling this one.
        public void addNeighbor(Vertex v) {
            neighbors.add(v);
        }

        public void setTag(String tag) {
            this.tag = tag;
        }
    }

    public Vertex addVertex(String id, String lon, String lat) {
        Vertex v = new Vertex(id, lon, lat);
        vlist.add(v);
        idVertexMap.put(v.id, v);
        return v;
    }

    public Vertex findVertex(String id) {
        long iD = Long.parseLong(id);
        if (idVertexMap.containsKey(iD)) {
            return idVertexMap.get(iD);
        }
        return null;
    }

    public GraphDB(String dbPath) {
        vlist = new ArrayList<>();
        idVertexMap = new HashMap<>();
        try {
            File inputFile = new File(dbPath);
            SAXParserFactory factory = SAXParserFactory.newInstance();
            SAXParser saxParser = factory.newSAXParser();
            GraphBuildingHandler gbh = new GraphBuildingHandler(this);
            saxParser.parse(inputFile, gbh);
        } catch (ParserConfigurationException | SAXException | IOException e) {
            e.printStackTrace();
        }
        clean();
    }

    /**
     * Helper to process strings into their "cleaned" form, ignoring punctuation and capitalization.
     * @param s Input string.
     * @return Cleaned string.
     */
    static String cleanString(String s) {
        return s.replaceAll("[^a-zA-Z ]", "").toLowerCase();
    }

    /**
     *  Remove nodes with no connections from the graph.
     *  While this does not guarantee that any two nodes in the remaining graph are connected,
     *  we can reasonably assume this since typically roads are connected.
     */
    private void clean() {
        for (int i = 0; i < vlist.size(); i++) {
            if (vlist.get(i).neighbors.size() < 1) {
                vlist.remove(i);
                i--;
            }
        }
    }

    /** Returns an iterable of all vertex IDs in the graph. */
    Iterable<Long> vertices() {
        ArrayList result = new ArrayList<Long>();
        for (int i = 0; i < vlist.size(); i++) {
            result.add(i, vlist.get(i).id);
        }
        return result;
    }

    /** Returns ids of all vertices adjacent to v. */
    Iterable<Long> adjacent(long v) {
        LinkedList<Long> result = new LinkedList<>();
        if (idVertexMap.containsKey(v)) {
            for (int j = 0; j < idVertexMap.get(v).neighbors.size(); j++) {
                result.add(idVertexMap.get(v).neighbors.get(j).id);
            }
        }
        return result;
    }

    //sqrt(londiff^2 + latdiff^2)
    double distance(long v, long w) {
        if (v == w) {
            return 0.0;
        }

        Vertex v1 = idVertexMap.get(v);
        Vertex v2 = idVertexMap.get(w);

        return  Math.sqrt(Math.pow((v1.lon - v2.lon), 2) + Math.pow(v1.lat - v2.lat, 2));
    }

    /** Returns the vertex id closest to the given longitude and latitude. */
    long closest(double lon, double lat)  {
        double minDistance = Double.POSITIVE_INFINITY;
        long id = 0;
        for (Vertex i: vlist) {
            double vdistance = Math.sqrt(Math.pow(lon - i.lon, 2) + Math.pow(lat - i.lat, 2));
            if (vdistance < minDistance) {
                minDistance = vdistance;
                id = i.id;
            }
        }
        return id;
    }

    /** Longitude of vertex v. */
    double lon(long v) {
        double longitude;

        longitude = idVertexMap.get(v).lon;
        return longitude;
    }

    /** Latitude of vertex v. */
    double lat(long v) {
        double latitude = 0;
        latitude = idVertexMap.get(v).lat;
        return latitude;
    }

}
