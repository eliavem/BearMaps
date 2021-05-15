import java.util.PriorityQueue;
import java.util.LinkedList;
import java.util.HashMap;
import java.util.Map;

/**
 * This class provides a shortestPath method for finding routes between two points
 * on the map. 
 */
public class Router {
    /**
     * Return a LinkedList of <code>Node</code>s representing the shortest path from st to dest.
     */

    // Algorithm reference taken from http://www.redblobgames.com/
    static double startTime;
    public static LinkedList<Long>
        shortestPath(GraphDB g, double stlon, double stlat, double destlon, double destlat) {
        long idClosestStartPoint = g.closest(stlon, stlat);
        long idClosestEndPoint = g.closest(destlon, destlat);
        PriorityQueue<IdAndPriority> frontier = new PriorityQueue<>();
        IdAndPriority first = new IdAndPriority(idClosestStartPoint, 0.0);
        frontier.add(first);
        Map<Long, Long> cameFrom = new HashMap<>(); //<Id current, Id previous>
        Map<Long, Double> costSoFar = new HashMap<>();  //<Id, cost in distance traveled>.
        cameFrom.put(idClosestStartPoint, null);
        costSoFar.put(idClosestStartPoint, 0.0);
        while (!frontier.isEmpty()) {
            IdAndPriority current = frontier.poll();
            if (current == null) {
                break;
            }
            if (current.id == idClosestEndPoint) {
                //Found Destination!
                LinkedList<Long> tempPathBackwards
                        = new LinkedList<>();  //its reversed, so reverse it before return.
                tempPathBackwards.add(idClosestEndPoint);
                Long iDprevious = cameFrom.get(current.id);
                tempPathBackwards.add(iDprevious);
                for (int i = 0; i < cameFrom.size(); i++) {
                    Long tIDprevious = cameFrom.get(iDprevious);
                    if (iDprevious == null) {
                        break;
                    }
                    tempPathBackwards.add(tIDprevious);
                    iDprevious = tIDprevious;
                }
                LinkedList<Long> result = new LinkedList<>();
                for (int i = tempPathBackwards.size() - 1; i >= 0; i--) {
                    if (tempPathBackwards.get(i) == null) {
                        continue;
                    }
                    result.add(tempPathBackwards.get(i));
                }
                double endTime = System.currentTimeMillis() - startTime;
                return result;
            }
            for (Long nextID : g.adjacent(current.id)) {
                double newCost = costSoFar.get(current.id) + g.distance(current.id, nextID);
                if (!costSoFar.containsKey(nextID) || newCost < costSoFar.get(nextID)) {
                    costSoFar.put(nextID, newCost);
                    double priority = newCost + g.distance(idClosestEndPoint, nextID);
                    frontier.add(new IdAndPriority(nextID, priority));
                    cameFrom.put(nextID, current.id);
                }
            }
        }

        return new LinkedList<Long>();
    }

    public static class IdAndPriority implements  Comparable<IdAndPriority> {
        Long id;
        double priority;

        public IdAndPriority(long id, double p) {
            this.id = id;
            this.priority = p;
        }
        @Override
        public int compareTo(IdAndPriority other) {
            if (this.equals(other)) {
                return 0;
            } else if (this.priority > other.priority) {
                return 1;
            } else {
                return -1;
            }

        }
    }

}
