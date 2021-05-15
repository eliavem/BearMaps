import java.util.ArrayList;
import java.util.LinkedList;
import java.util.HashMap;
import java.util.Map;

/**
 * This class provides all code necessary to take a query box and produce
 * a query result. The getMapRaster method must return a Map containing all
 * seven of the required fields, otherwise the front end code will probably
 * not draw the output correctly.
 */
public class Rasterer {
    QuadTree quadTree;
   
    public Rasterer(String imgRoot) {
        quadTree = new QuadTree();
    }

    /**
     * Takes a user query and finds the grid of images that best matches the query. These
     * images will be combined into one big image (rastered) by the front end. <br>
     * <p>
     *     The grid of images must obey the following properties, where image in the
     *     grid is referred to as a "tile".
     *     <ul>
     *         <li>The tiles collected must cover the most longitudinal distance per pixel
     *         (LonDPP) possible, while still covering less than or equal to the amount of
     *         longitudinal distance per pixel in the query box for the user viewport size. </li>
     *         <li>Contains all tiles that intersect the q uery bounding box that fulfill the
     *         above condition.</li>
     *         <li>The tiles must be arranged in-order to reconstruct the full image.</li>
     *     </ul>
     * </p>
     * @param params Map of the HTTP GET request's query parameters - the query box and
     *               the user viewport width and height.
     *
     * @return A map of results for the front end as specified:
     * "render_grid"   -> String[][], the files to display
     * "raster_ul_lon" -> Number, the bounding upper left longitude of the rastered image <br>
     * "raster_ul_lat" -> Number, the bounding upper left latitude of the rastered image <br>
     * "raster_lr_lon" -> Number, the bounding lower right longitude of the rastered image <br>
     * "raster_lr_lat" -> Number, the bounding lower right latitude of the rastered image <br>
     * "depth"         -> Number, the 1-indexed quadtree depth of the nodes of the rastered image.
     *                    Can also be interpreted as the length of the numbers in the image
     *                    string. <br>
     * "query_success" -> Boolean, whether the query was able to successfully complete. Don't
     *                    forget to set this to true! <br>
     * @see #//REQUIRED_RASTER_REQUEST_PARAMS
     */
    public Map<String, Object> getMapRaster(Map<String, Double> params) {

        double queryLRLON = params.get("lrlon");
        double queryULLON = params.get("ullon");
        double queryULLAT = params.get("ullat");
        double queryLRLAT = params.get("lrlat");
        double width = params.get("w");
        double height = params.get("h");
        double queryLonDPP = (queryLRLON - queryULLON) / width;
        LinkedList<QuadTree.Node> linkedList =
                quadTree.getLinkedList(quadTree.getRoot(),
                        queryLonDPP, queryULLON,  queryULLAT, queryLRLON, queryLRLAT);

        // Array to be returned with the desired tails 
        String[][] renderGrid = quadTree.sort(linkedList);

        double rasterULLon = linkedList.get(0).upperLeftLon;
        double rasterULLat = linkedList.get(0).upperLeftLat;
        double rasterLRLon = linkedList.get(linkedList.size() - 1).lowerRightLon;  
        double rasterLRLat = linkedList.get(linkedList.size() - 1).lowerRightLat;  
        String d = "" + linkedList.get(0).nodeTitle;
        int depth =  d.length();
        boolean querySuccess = true;
        Map<String, Object> results = new HashMap<>();
        results.put("render_grid", renderGrid);
        results.put("raster_ul_lon", rasterULLon);
        results.put("raster_ul_lat", rasterULLat);
        results.put("raster_lr_lon", rasterLRLon);
        results.put("raster_lr_lat", rasterLRLat);
        results.put("depth", depth);
        results.put("query_success", querySuccess);
        return results;
    }


    // Method that gets the number of columns for the size of the array 
    public int[] getRowsCol(LinkedList<QuadTree.Node> list) {
        Map<Double, ArrayList<QuadTree.Node>> groupByLat = new HashMap<>();
        for (QuadTree.Node i : list) {
            if (!groupByLat.containsKey(i.upperLeftLat)) {
                ArrayList<QuadTree.Node> newgroup = new ArrayList<>();
                newgroup.add(i);
                groupByLat.put(i.upperLeftLat, newgroup);
            } else {
                groupByLat.get(i.upperLeftLat).add(i);
            }
        }
        int[] result = new int[] {groupByLat.size(),
                groupByLat.get((list.get(0)).upperLeftLat).size()}; //uniqueLats.size()/list.size()
        return result;
    }

    public void printContents(QuadTree L) {
        QuadTree.Node n = L.getRoot();
        while (n.right != null) {
            n = n.right;
        }
    }
}
