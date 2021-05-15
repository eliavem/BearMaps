import sun.reflect.generics.tree.ArrayTypeSignature;

import java.awt.geom.Point2D;
import java.util.*;
import java.util.logging.Level;

/**
 * Created by eliAvelar on 4/10/17.
 */
public class QuadTree {
    private static Node root;
    public LinkedList<Node> validNodes;
    private final int LEVEL = 7;

    ///Node Class
    public class Node implements Comparable<Node>{
        Double upperLeftLon;
        Double lowerRightLon;
        Double upperLeftLat;
        Double lowerRightLat;
        Node nodeParent;
        String nodeTitle;
        double lonDPPNode;
        private int nodeLevel = 0;
        Node left, middleLeft, middleRight, right; // Quad node

        Node(Node parent, Double upperLeftLat, Double upperLeftLon, Double lowerRightLat, Double lowerRightLon, String nodeTile, int nodeLevel) {
            this.lonDPPNode = (lowerRightLon - upperLeftLon) / 256;
            this.upperLeftLon = upperLeftLon;
            this.upperLeftLat = upperLeftLat;
            this.lowerRightLon = lowerRightLon;
            this.lowerRightLat = lowerRightLat;
            this.nodeTitle = nodeTile;
            this.nodeLevel = nodeLevel;
            nodeParent = parent;
        }

        // implement the comparator method
        @Override
        public int compareTo(Node node) {
            if (this.upperLeftLat.equals(node.upperLeftLat)){
                return 0;
            } else if (this.upperLeftLat < node.upperLeftLat) {
                return 1;
            } else {
                return -1;
            }
        }

        @Override
        public String toString() {
            return nodeTitle;
        }


public Node insertImage(Node parent) {
    if(parent.nodeLevel == LEVEL) {
        return null;

    } else if (parent.left == null) {
        parent.left = new Node(parent, parent.upperLeftLat, parent.upperLeftLon, (parent.upperLeftLat + parent.lowerRightLat)/2,
               (parent.upperLeftLon + parent.lowerRightLon) / 2, parent.nodeTitle + "1", parent.nodeLevel + 1);
        return insertImage(parent);

    } else if (parent.middleLeft == null) {
        parent.middleLeft = new Node(parent, parent.upperLeftLat, (parent.upperLeftLon + parent.lowerRightLon) / 2, (parent.upperLeftLat + parent.lowerRightLat) / 2,
                parent.lowerRightLon, parent.nodeTitle + "2", parent.nodeLevel + 1);
        return insertImage(parent);

    } else if (parent.middleRight == null) {
        parent.middleRight = new Node(parent, (parent.upperLeftLat + parent.lowerRightLat) / 2, parent.upperLeftLon, parent.lowerRightLat,
                (parent.upperLeftLon + parent.lowerRightLon) / 2, parent.nodeTitle + "3", parent.nodeLevel + 1);
        return insertImage(parent);

    } else if (parent.right == null){
        parent.right = new Node(parent, (parent.upperLeftLat + parent.lowerRightLat) / 2, (parent.upperLeftLon + parent.lowerRightLon) / 2, parent.lowerRightLat,
                parent.lowerRightLon, parent.nodeTitle + "4", parent.nodeLevel + 1);
        return insertImage(parent);
    } else {
        insertImage(parent.left);
        insertImage(parent.middleLeft);
        insertImage(parent.middleRight);
        insertImage(parent.right);
    }
    return null;
}
        // Methods to calculate P1 and P2

        public boolean intersectTile(Node explored, double queryULLON, double queryULLAT, double queryLRLON, double queryLRLAT) {
            return  !((explored.lowerRightLon < queryULLON) || (explored.upperLeftLon >  queryLRLON) ||
                    (explored.upperLeftLat <queryLRLAT) || (explored.lowerRightLat > queryULLAT));
        }
    }

    /// QuadTree constructor
    public QuadTree() {
        validNodes = new LinkedList();
        root = new Node (null, MapServer.ROOT_ULLAT, MapServer.ROOT_ULLON, MapServer.ROOT_LRLAT, MapServer.ROOT_LRLON, "", 0);
        root.insertImage(root); //call Insert Image so the rest of the tree is completed
    }

    public static Node getRoot() {
        return root;
    }

    public double getNewLat(double PUpperLat, double PLowerLat) {
        double newLon = (PUpperLat + PLowerLat) / 2;
        return newLon;
    }

    public double getNewLon(double PUpperLon, double PLowerLon) {
        double newLat = (PUpperLon + PLowerLon) / 2;
        return  newLat;
    }

    // Methods to check nodes 
    public void recursivelyCheckNodes(Node n, double queryLonDPP, double queryULLON, double queryULLAT, double queryLRLON, double queryLRLAT){
        if(n.nodeLevel == LEVEL){
            if(n.intersectTile(n, queryULLON, queryULLAT, queryLRLON, queryLRLAT)){
                validNodes.addLast(n);
            }
            return;
        }
        boolean P1_true = true;
        boolean P2_true = true;
        if(!n.intersectTile(n, queryULLON, queryULLAT, queryLRLON, queryLRLAT)){  //Notice the not "!"
            //Prune Branchs
            P1_true = false; 
            return;
        }

       // if(P1_true && !n.lonDPPSmaller(queryLonDPP)){  //Notice the not "!"
        if(P1_true && !(n.lonDPPNode <= queryLonDPP)){
            P2_true = false;  
            recursivelyCheckNodes(n.left, queryLonDPP, queryULLON, queryULLAT, queryLRLON, queryLRLAT);
            recursivelyCheckNodes(n.middleLeft, queryLonDPP, queryULLON, queryULLAT, queryLRLON, queryLRLAT);
            recursivelyCheckNodes(n.middleRight, queryLonDPP, queryULLON, queryULLAT, queryLRLON, queryLRLAT);
            recursivelyCheckNodes(n.right, queryLonDPP, queryULLON, queryULLAT, queryLRLON, queryLRLAT);
        }

        if(P1_true && P2_true){
            validNodes.addLast(n);
        }
    }


    public LinkedList<Node> getLinkedList(Node n, double queryLonDPP, double queryULLON, double queryULLAT, double queryLRLON, double queryLRLAT){
        validNodes.clear();
        recursivelyCheckNodes(n, queryLonDPP, queryULLON, queryULLAT, queryLRLON, queryLRLAT);
        return validNodes;
    }
    public String[][] sort(LinkedList<Node> nodesList) {
        Map<Double, ArrayList<Node>> sorted = new TreeMap<>();
        for (Node n : nodesList) {
            Double key = -1 * n.upperLeftLat;
            if (!sorted.containsKey(key)) {
                sorted.put(key, new ArrayList<>(Arrays.asList(n)));
            } else {
                sorted.get(key).add(n);
            }
        }
        int row = sorted.size();
        int column = nodesList.size() / row;
        String[][] finalList = new String[row][column];
        int r = 0;
        for (Double key : sorted.keySet()) {
            ArrayList<Node> x = sorted.get(key);
            for (int c = 0; c < x.size(); c += 1) {
                finalList[r][c] = "img/" + x.get(c).nodeTitle + ".png";
            }
            r += 1;
        }
        return finalList;
    }
}

