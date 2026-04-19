package game;

import java.util.ArrayList;
import java.util.List;

public class Node {

    private String name;
    private int capacity;
    private double x, y;

    private boolean isSource = false;
    private boolean isDestination = false;

    // For graph (adjacency list)
    private List<Edge> edges = new ArrayList<>();

    // For Dijkstra
    private double distance = Double.MAX_VALUE;
    private Node previous = null;

    public Node(String name, int capacity, double x, double y) {
        this.name = name;
        this.capacity = capacity;
        this.x = x;
        this.y = y;
    }

    // ===== Getters & Setters =====

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public int getCapacity() { return capacity; }
    public void setCapacity(int capacity) { this.capacity = capacity; }

    public double getX() { return x; }
    public double getY() { return y; }

    public boolean isSource() { return isSource; }
    public void setSource(boolean source) { isSource = source; }

    public boolean isDestination() { return isDestination; }
    public void setDestination(boolean destination) { isDestination = destination; }

    public List<Edge> getEdges() { return edges; }

    public double getDistance() { return distance; }
    public void setDistance(double distance) { this.distance = distance; }

    public Node getPrevious() { return previous; }
    public void setPrevious(Node previous) { this.previous = previous; }
}
