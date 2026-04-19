package game;

import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextInputDialog;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.text.Text;
import javafx.scene.shape.Line;

import java.util.*;

public class TreasureHunt {

    // ===== NODE =====
    static class Node {
        String name;
        int capacity;
        double x, y;

        boolean isSource = false;
        boolean isDestination = false;

        Node(String name, int capacity, double x, double y) {
            this.name = name;
            this.capacity = capacity;
            this.x = x;
            this.y = y;
        }
    }

    // ===== EDGE =====
    static class Edge {
        Node from;
        Node to;
        int weight;

        Edge(Node from, Node to, int weight) {
            this.from = from;
            this.to = to;
            this.weight = weight;
        }
    }

    // ===== PATH RESULT =====
    static class PathResult {
        List<Node> path;
        double distance;

        PathResult(List<Node> path, double distance) {
            this.path = path;
            this.distance = distance;
        }
    }

    // ===== HUNTER RESULT =====
    static class HunterResult {
        String hunterName;
        String destinationName;
        List<Node> path;
        double distance;
        double fuelCost;
        double foodCost;
        double totalExpenses;
        double bagCapacity;
        double totalTreasureValue;
        double totalTreasureWeight;
        double profit;
        List<String> itemsTaken; // description strings

        HunterResult(String hunterName) {
            this.hunterName = hunterName;
            this.itemsTaken = new ArrayList<>();
        }
    }

    // ===== CONSTANT COSTS =====
    private static final double FUEL_COST_PER_KM = 40.0;
    private static final double FOOD_COST_PER_NODE = 100.0;

    public static Scene create(App app) {

        BorderPane root = new BorderPane();

        // ===== TOP =====
        Text title = new Text("Treasure Hunt Game");
        HBox top = new HBox(title);
        top.setAlignment(Pos.CENTER);
        top.setStyle("-fx-padding: 10; -fx-font-size: 18px;");
        root.setTop(top);

        // ===== CANVAS =====
        Pane canvas = new Pane();
        canvas.setPrefSize(100, 100);

        Image bg = new Image(
                Objects.requireNonNull(
                        TreasureHunt.class.getResource("/images/OnePieceMap.png")
                ).toExternalForm()
        );

        BackgroundImage bgImg = new BackgroundImage(
                bg,
                BackgroundRepeat.NO_REPEAT,
                BackgroundRepeat.NO_REPEAT,
                BackgroundPosition.CENTER,
                new BackgroundSize(100, 100, true, true, true, false)
        );

        canvas.setBackground(new Background(bgImg));
        root.setCenter(canvas);

        // ===== BUTTONS =====
        Button addNodeBtn = new Button("Add Node");
        Button addEdgeBtn = new Button("Add Edge");
        Button setSourceBtn = new Button("Set Source");
        Button setDestBtn = new Button("Set Destinations");
        Button startGameBtn = new Button("Start Game");

        VBox controls = new VBox(15, addNodeBtn, addEdgeBtn, setSourceBtn, setDestBtn, startGameBtn);
        controls.setAlignment(Pos.CENTER);
        controls.setStyle("-fx-padding: 10;");
        root.setLeft(controls);

        // ===== SCOREBOARD =====
        VBox scoreboard = new VBox(10);
        scoreboard.setAlignment(Pos.CENTER);
        scoreboard.setStyle("-fx-padding: 15;");
        scoreboard.setPrefWidth(260);

        ImageView luffyIcon = new ImageView(new Image(
                Objects.requireNonNull(TreasureHunt.class.getResource("/images/Luffy.png")).toExternalForm()
        ));
        luffyIcon.setFitWidth(110);
        luffyIcon.setFitHeight(150);

        ImageView lawIcon = new ImageView(new Image(
                Objects.requireNonNull(TreasureHunt.class.getResource("/images/Law.png")).toExternalForm()
        ));
        lawIcon.setFitWidth(170);
        lawIcon.setFitHeight(150);

        ImageView blackbeardIcon = new ImageView(new Image(
                Objects.requireNonNull(TreasureHunt.class.getResource("/images/Blackbeard.png")).toExternalForm()
        ));
        blackbeardIcon.setFitWidth(170);
        blackbeardIcon.setFitHeight(150);

        Text luffyText = new Text("Luffy");
        Text lawText = new Text("Law");
        Text blackbeardText = new Text("Blackbeard");

        scoreboard.getChildren().addAll(

                luffyIcon,
                luffyText,

                lawIcon,
                lawText,

                blackbeardIcon,
                blackbeardText
        );

        root.setRight(scoreboard);

        // ===== DATA =====
        List<Node> nodes = new ArrayList<>();
        List<Edge> edges = new ArrayList<>();

        final char[] currentName = {'A'};

        final boolean[] addNodeMode = {false};
        final boolean[] addEdgeMode = {false};
        final boolean[] setSourceMode = {false};
        final boolean[] setDestMode = {false};

        final Node[] selectedNode = {null};
        final Node[] sourceNode = {null};
        final int[] destCount = {0};

        // Keep references to destination nodes
        final Node[] caveNode = {null};
        final Node[] mountainNode = {null};
        final Node[] seaFloorNode = {null};

        Random random = new Random();

        // ===== BUTTON ACTIONS =====
        addNodeBtn.setOnAction(e -> {
            resetModes(addNodeMode, addEdgeMode, setSourceMode, setDestMode);
            addNodeMode[0] = true;
        });

        addEdgeBtn.setOnAction(e -> {
            resetModes(addNodeMode, addEdgeMode, setSourceMode, setDestMode);
            addEdgeMode[0] = true;
        });

        setSourceBtn.setOnAction(e -> {
            resetModes(addNodeMode, addEdgeMode, setSourceMode, setDestMode);
            setSourceMode[0] = true;
        });

        setDestBtn.setOnAction(e -> {
            resetModes(addNodeMode, addEdgeMode, setSourceMode, setDestMode);
            setDestMode[0] = true;
            destCount[0] = 0;
        });

        startGameBtn.setOnAction(e -> {
            if (sourceNode[0] == null || caveNode[0] == null || mountainNode[0] == null || seaFloorNode[0] == null) {
                // scoreboard stays static, so no updating it
                javafx.scene.control.Alert alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.WARNING);
                alert.setTitle("Missing Setup");
                alert.setHeaderText("You must set the source and all 3 destinations first!");
                alert.setContentText("Please place:\n• 1 Source\n• Cave\n• Mountain\n• Sea-Floor");
                alert.showAndWait();
                return;
            }

            // Run full simulation
            Map<String, HunterResult> results = runTreasureHuntSimulation(
                    nodes, edges,
                    sourceNode[0],
                    caveNode[0], mountainNode[0], seaFloorNode[0]
            );

            HunterResult luffy = results.get("Luffy");
            HunterResult law = results.get("Law");
            HunterResult blackbeard = results.get("Blackbeard");

            // Determine winner
            HunterResult winner = luffy;
            if (law.profit > winner.profit) winner = law;
            if (blackbeard.profit > winner.profit) winner = blackbeard;

            // ===== POPUP WINDOW WITH WINNER IMAGE =====
            javafx.scene.control.Alert alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.INFORMATION);
            alert.setTitle("Treasure Hunt Results");
            alert.setHeaderText("Winner: " + winner.hunterName + " (" + winner.destinationName + ")");

            // Winner image
            String imgPath = "/images/" + winner.hunterName + ".png";
            ImageView winnerImg = new ImageView(new Image(
                    Objects.requireNonNull(TreasureHunt.class.getResource(imgPath)).toExternalForm()
            ));
            winnerImg.setFitWidth(180);
            winnerImg.setFitHeight(220);

            alert.setGraphic(winnerImg);

            // Popup text
            StringBuilder popupMsg = new StringBuilder();

            popupMsg.append("Profit: ").append(String.format("%.1f", winner.profit)).append("k\n\n");
            popupMsg.append("Hunter Results\n\n");

            popupMsg.append("Luffy → ").append(formatHunterSummary(luffy)).append("\n\n");
            popupMsg.append("Law → ").append(formatHunterSummary(law)).append("\n\n");
            popupMsg.append("Blackbeard → ").append(formatHunterSummary(blackbeard)).append("\n");

            alert.setContentText(popupMsg.toString());
            alert.setResizable(true);
            alert.getDialogPane().setPrefSize(520, 500);
            alert.showAndWait();
        });

        // ===== CLICK HANDLER =====
        canvas.setOnMouseClicked(e -> {

            double x = e.getX();
            double y = e.getY();

            // ===== ADD NODE =====
            if (addNodeMode[0]) {

                if (currentName[0] > 'Z') return;

                int cap = 20 + random.nextInt(6);

                Node node = new Node(String.valueOf(currentName[0]), cap, x, y);
                nodes.add(node);

                drawNode(canvas, node);

                currentName[0]++;

            }

            // ===== ADD EDGE =====
            else if (addEdgeMode[0]) {

                Node clicked = findNode(nodes, x, y);
                if (clicked == null) return;

                if (selectedNode[0] == null) {
                    selectedNode[0] = clicked;
                } else {

                    Node from = selectedNode[0];
                    Node to = clicked;

                    TextInputDialog dialog = new TextInputDialog();
                    dialog.setHeaderText("Enter edge weight (km)");

                    dialog.showAndWait().ifPresent(val -> {
                        try {
                            int w = Integer.parseInt(val);

                            edges.add(new Edge(from, to, w));
                            drawEdge(canvas, from, to, w);

                        } catch (Exception ex) {
                            System.out.println("Invalid weight");
                        }
                    });

                    selectedNode[0] = null;
                }
            }

            // ===== SET SOURCE =====
            else if (setSourceMode[0]) {

                Node n = findNode(nodes, x, y);
                if (n == null) return;

                if (sourceNode[0] != null) {
                    System.out.println("Source already selected!");
                    return;
                }

                n.isSource = true;
                n.name = "East-Blue";
                n.capacity = 0;

                sourceNode[0] = n;

                drawNode(canvas, n);
                System.out.println("Source set: East-Blue");
            }

            // ===== SET DESTINATIONS =====
       
else if (setDestMode[0]) {

    Node n = findNode(nodes, x, y);
    if (n == null) return;

    // prevent selecting source
    if (n.isSource) {
        System.out.println("Cannot set source as destination!");
        return;
    }

    // prevent duplicate click
    if (n.isDestination) {
        System.out.println("Already a destination!");
        return;
    }

    // limit to 3
    if (destCount[0] >= 3) {
        System.out.println("All destinations already selected!");
        return;
    }

    n.isDestination = true;
    n.capacity = 0;

    if (destCount[0] == 0) {
        n.name = "Cave";
        caveNode[0] = n;
    } else if (destCount[0] == 1) {
        n.name = "Mountain";
        mountainNode[0] = n;
    } else {
        n.name = "Sea-Floor";
        seaFloorNode[0] = n;
    }

    destCount[0]++;

    drawNode(canvas, n);

    System.out.println("Destination set: " + n.name);

    if (destCount[0] == 3) {
        setDestMode[0] = false;
    }
}


        });

        return new Scene(root, 1150, 650);
    }

    // ===== RESET MODES =====
    private static void resetModes(boolean[]... modes) {
        for (boolean[] m : modes) m[0] = false;
    }

    // ===== REMOVE OLD GRAPHICS =====
    private static void removeNodeGraphics(Pane canvas, Node node) {
        canvas.getChildren().removeIf(n -> {
            if (n instanceof ImageView iv) {
                double centerX = iv.getX() + iv.getFitWidth() / 2;
                double centerY = iv.getY() + iv.getFitHeight() / 2;
                return Math.abs(centerX - node.x) < 1 && Math.abs(centerY - node.y) < 1;
            }
            if (n instanceof Text t) {
                return t.getText().contains(node.name);
            }
            return false;
        });
    }

    // ===== DRAW NODE =====
    private static void drawNode(Pane canvas, Node node) {

        removeNodeGraphics(canvas, node);

        String iconPath;
        int size = 60;

        if (node.isSource) {
            iconPath = "/images/Source.png";
            size = 80;
        } else if (node.isDestination) {
            switch (node.name) {
                case "Cave":
                    iconPath = "/images/Cave.png";
                    break;
                case "Mountain":
                    iconPath = "/images/Mountain.png";
                    break;
                case "Sea-Floor":
                    iconPath = "/images/SeaFloor.png";
                    break;
                default:
                    iconPath = "/images/Destination.png";
            }
            size = 80;
        } else {
            iconPath = "/images/Node.png";
        }

        Image img = new Image(
                Objects.requireNonNull(
                        TreasureHunt.class.getResource(iconPath)
                ).toExternalForm()
        );

        ImageView icon = new ImageView(img);
        icon.setFitWidth(size);
        icon.setFitHeight(size);
        icon.setX(node.x - size / 2);
        icon.setY(node.y - size / 2);

        Text info = new Text(node.x - 40, node.y - size / 2 - 10,
                node.name + " (" + node.capacity + "kg)");
        info.setVisible(false);

        icon.setOnMouseEntered(e -> info.setVisible(true));
        icon.setOnMouseExited(e -> info.setVisible(false));

        canvas.getChildren().addAll(icon, info);
    }

    // ===== DRAW EDGE =====
    private static void drawEdge(Pane canvas, Node from, Node to, int w) {

        Line line = new Line(from.x, from.y, to.x, to.y);
        Text label = new Text((from.x + to.x) / 2, (from.y + to.y) / 2, String.valueOf(w));

        canvas.getChildren().addAll(line, label);
    }

    // ===== FIND NODE (bounding box) =====
    private static Node findNode(List<Node> nodes, double x, double y) {

        for (Node n : nodes) {
            double half = 40; // supports up to 80px icons

            boolean inside =
                    x >= n.x - half &&
                            x <= n.x + half &&
                            y >= n.y - half &&
                            y <= n.y + half;

            if (inside) return n;
        }
        return null;
    }

    // ===== SCOREBOARD UPDATE =====
    private static void updateScoreboard(
            Text nodeCountText,
            Text edgeCountText,
            Text sourceText,
            Text destText,
            List<Node> nodes,
            List<Edge> edges,
            Node sourceNode,
            int destCount
    ) {
        nodeCountText.setText("Nodes: " + nodes.size());
        edgeCountText.setText("Edges: " + edges.size());
        sourceText.setText("Source: " + (sourceNode != null ? sourceNode.name : "None"));
        destText.setText("Destinations: " + destCount + " / 3");
    }

    // ===== DIJKSTRA SHORTEST PATH =====
    private static PathResult dijkstra(List<Node> nodes, List<Edge> edges, Node source, Node target) {

        Map<Node, Double> dist = new HashMap<>();
        Map<Node, Node> parent = new HashMap<>();

        for (Node n : nodes) {
            dist.put(n, Double.POSITIVE_INFINITY);
            parent.put(n, null);
        }

        dist.put(source, 0.0);

        PriorityQueue<Node> pq = new PriorityQueue<>(Comparator.comparingDouble(dist::get));
        pq.add(source);

        while (!pq.isEmpty()) {
            Node u = pq.poll();

            if (u == target) break;

            for (Edge e : edges) {
                if (e.from == u) {
                    Node v = e.to;
                    double newDist = dist.get(u) + e.weight;

                    if (newDist < dist.get(v)) {
                        dist.put(v, newDist);
                        parent.put(v, u);
                        pq.add(v);
                    }
                }
            }
        }

        List<Node> path = new ArrayList<>();
        Node step = target;

        if (parent.get(step) == null && step != source) {
            return new PathResult(path, Double.POSITIVE_INFINITY);
        }

        while (step != null) {
            path.add(step);
            step = parent.get(step);
        }

        Collections.reverse(path);

        return new PathResult(path, dist.get(target));
    }

    // ===== PATH TO STRING =====
    private static String pathToString(List<Node> path) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < path.size(); i++) {
            sb.append(path.get(i).name);
            if (i < path.size() - 1) sb.append(" → ");
        }
        return sb.toString();
    }

    // ===== TREASURE TABLES =====
    // Cave (A1..J1)
    private static final int[] CAVE_WEIGHTS = {2, 3, 7, 8, 7, 2, 4, 3, 9, 2};
    private static final int[] CAVE_PRICES = {5, 7, 10, 14, 19, 4, 9, 10, 23, 2};

    // Mountain (A2..J2)
    private static final int[] MOUNTAIN_WEIGHTS = {5, 4, 6, 9, 7, 3, 2, 1, 10, 12};
    private static final int[] MOUNTAIN_PRICES = {15, 9, 19, 34, 19, 10, 7, 13, 29, 40};

    // Sea-Floor (A3..J3)
    private static final int[] SEAFLOOR_WEIGHTS = {3, 2, 2, 9, 11, 12, 7, 8, 1, 10};
    private static final int[] SEAFLOOR_PRICES = {8, 4, 3, 17, 28, 30, 19, 21, 3, 29};

    // ===== 0/1 KNAPSACK =====
    private static void knapsack01(int[] weights, int[] values, double capacity,
                                   HunterResult result, String itemPrefix) {

        int n = weights.length;
        int W = (int) Math.round(capacity);

        int[][] dp = new int[n + 1][W + 1];

        for (int i = 1; i <= n; i++) {
            int wt = weights[i - 1];
            int val = values[i - 1];
            for (int w = 0; w <= W; w++) {
                if (wt <= w) {
                    dp[i][w] = Math.max(dp[i - 1][w], dp[i - 1][w - wt] + val);
                } else {
                    dp[i][w] = dp[i - 1][w];
                }
            }
        }

        int w = W;
        int totalVal = dp[n][W];
        int totalWt = 0;

        for (int i = n; i >= 1; i--) {
            if (dp[i][w] != dp[i - 1][w]) {
                int wt = weights[i - 1];
                int val = values[i - 1];
                result.itemsTaken.add(itemPrefix + (char) ('A' + (i - 1)) +
                        " (w=" + wt + ", p=" + val + ")");
                totalWt += wt;
                w -= wt;
            }
        }

        result.totalTreasureValue = totalVal;
        result.totalTreasureWeight = totalWt;
    }

    // ===== FRACTIONAL KNAPSACK =====
    private static void fractionalKnapsack(int[] weights, int[] values, double capacity,
                                           HunterResult result, String itemPrefix) {

        int n = weights.length;
        List<Integer> idx = new ArrayList<>();
        for (int i = 0; i < n; i++) idx.add(i);

        idx.sort((i, j) -> {
            double r1 = (double) values[i] / weights[i];
            double r2 = (double) values[j] / weights[j];
            return Double.compare(r2, r1);
        });

        double remaining = capacity;
        double totalVal = 0;
        double totalWt = 0;

        for (int i : idx) {
            if (remaining <= 0) break;
            if (weights[i] <= remaining) {
                remaining -= weights[i];
                totalVal += values[i];
                totalWt += weights[i];
                result.itemsTaken.add(itemPrefix + (char) ('A' + i) +
                        " (w=" + weights[i] + ", p=" + values[i] + ")");
            } else {
                double fraction = remaining / weights[i];
                double val = values[i] * fraction;
                double wt = weights[i] * fraction;
                totalVal += val;
                totalWt += wt;
                result.itemsTaken.add(itemPrefix + (char) ('A' + i) +
                        " (fraction " + String.format("%.2f", fraction) +
                        ", w=" + String.format("%.2f", wt) +
                        ", p=" + String.format("%.2f", val) + ")");
                remaining = 0;
            }
        }

        result.totalTreasureValue = totalVal;
        result.totalTreasureWeight = totalWt;
    }

    // ===== RUN FULL TREASURE HUNT SIMULATION =====
    private static Map<String, HunterResult> runTreasureHuntSimulation(
            List<Node> nodes,
            List<Edge> edges,
            Node source,
            Node cave,
            Node mountain,
            Node seaFloor
    ) {
        Map<String, HunterResult> map = new HashMap<>();
        HunterResult luffy = new HunterResult("Luffy");
        HunterResult law = new HunterResult("Law");
        HunterResult blackbeard = new HunterResult("Blackbeard");

        map.put("Luffy", luffy);
        map.put("Law", law);
        map.put("Blackbeard", blackbeard);

        // Randomly assign destinations
        List<Node> dests = new ArrayList<>();
        dests.add(cave);
        dests.add(mountain);
        dests.add(seaFloor);
        Collections.shuffle(dests);

        Node luffyDest = dests.get(0);
        Node lawDest = dests.get(1);
        Node blackDest = dests.get(2);

        simulateHunter(nodes, edges, source, luffyDest, luffy);
        simulateHunter(nodes, edges, source, lawDest, law);
        simulateHunter(nodes, edges, source, blackDest, blackbeard);

        return map;
    }

    // ===== SIMULATE SINGLE HUNTER =====
    private static void simulateHunter(
            List<Node> nodes,
            List<Edge> edges,
            Node source,
            Node dest,
            HunterResult result
    ) {
        result.destinationName = dest.name;

        PathResult pr = dijkstra(nodes, edges, source, dest);
        result.path = pr.path;
        result.distance = pr.distance;

        if (pr.path.isEmpty() || pr.distance == Double.POSITIVE_INFINITY) {
            result.fuelCost = 0;
            result.foodCost = 0;
            result.totalExpenses = 0;
            result.bagCapacity = 0;
            result.totalTreasureValue = 0;
            result.totalTreasureWeight = 0;
            result.profit = -1_000_000; // impossible path
            return;
        }

        // Fuel cost
        result.fuelCost = pr.distance * FUEL_COST_PER_KM;

        // Food cost: each node on path
      int intermediateNodes = pr.path.size() - 2; // remove source + destination

if (intermediateNodes < 0) intermediateNodes = 0;

result.foodCost = intermediateNodes * FOOD_COST_PER_NODE;

        // Magic bag capacity from node before destination
        Node bagNode = null;
        if (pr.path.size() >= 2) {
            bagNode = pr.path.get(pr.path.size() - 2);
        }

        if (bagNode == null) {
            result.bagCapacity = 0;
            result.totalTreasureValue = 0;
            result.totalTreasureWeight = 0;
            result.profit = -1_000_000;
            return;
        }

        result.bagCapacity = bagNode.capacity;

        // Choose treasure based on destination
        result.itemsTaken.clear();

        if (dest.name.equals("Cave")) {
            knapsack01(CAVE_WEIGHTS, CAVE_PRICES, result.bagCapacity, result, "A1-");
        } else if (dest.name.equals("Mountain")) {
            knapsack01(MOUNTAIN_WEIGHTS, MOUNTAIN_PRICES, result.bagCapacity, result, "A2-");
        } else if (dest.name.equals("Sea-Floor")) {
            fractionalKnapsack(SEAFLOOR_WEIGHTS, SEAFLOOR_PRICES, result.bagCapacity, result, "A3-");
        } else {
            result.totalTreasureValue = 0;
            result.totalTreasureWeight = 0;
        }

        result.profit = result.totalTreasureValue - result.totalExpenses;
    }

    // ===== FORMAT HUNTER SUMMARY =====
    private static String formatHunterSummary(HunterResult r) {
        if (r.path == null || r.path.isEmpty() || r.profit < -999_999) {
            return r.hunterName + ": No valid path.";
        }

        return r.hunterName + " -> " + r.destinationName +
                ", Dist=" + String.format("%.1f", r.distance) +
                ", Bag=" + r.bagCapacity +
                "kg, ItemsValue=" + String.format("%.1f", r.totalTreasureValue) +
                "k, Expenses=" + String.format("%.1f", r.totalExpenses) +
                "k, Profit=" + String.format("%.1f", r.profit) + "k";
    }
}
