package game;

public class Edge {

    private Node from;
    private Node to;
    private int weight;

    // Optional: store UI reference (for updating later)
    // private Line line;

    public Edge(Node from, Node to, int weight) {
        this.from = from;
        this.to = to;
        this.weight = weight;
    }

    public Node getFrom() { return from; }
    public Node getTo() { return to; }
    public int getWeight() { return weight; }

    public void setWeight(int weight) { this.weight = weight; }

    @Override
    public String toString() {
        return from.getName() + " -> " + to.getName() + " (" + weight + "km)";
    }
}
