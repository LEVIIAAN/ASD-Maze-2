import javax.swing.*;
import javax.swing.Timer;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.util.List;

// Main Application Class
public class GraphVisualizer extends JFrame {
    private GraphPanel graphPanel;
    private Graph graph;

    public GraphVisualizer() {
        setTitle("Advanced Graph Visualizer with Dijkstra");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1300, 850);
        setLocationRelativeTo(null);
        showWelcomeScreen();
    }

    private void showWelcomeScreen() {
        JPanel welcomePanel = new JPanel(new BorderLayout());
        welcomePanel.setBackground(new Color(30, 40, 60));

        JPanel centerPanel = new JPanel(new GridBagLayout());
        centerPanel.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(15, 15, 15, 15);
        gbc.gridx = 0;

        JLabel titleLabel = new JLabel("📊 GRAPH VISUALIZER PRO");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 48));
        titleLabel.setForeground(new Color(100, 200, 255));
        gbc.gridy = 0;
        centerPanel.add(titleLabel, gbc);

        JLabel subtitleLabel = new JLabel("With Dijkstra Algorithm & Custom Labels");
        subtitleLabel.setFont(new Font("Arial", Font.ITALIC, 20));
        subtitleLabel.setForeground(new Color(200, 200, 200));
        gbc.gridy = 1;
        centerPanel.add(subtitleLabel, gbc);

        JButton sampleButton = createStyledButton("📈 Load Sample Graph", new Color(50, 150, 250));
        gbc.gridy = 2;
        centerPanel.add(sampleButton, gbc);

        JButton inputButton = createStyledButton("⌨️ Input Weighted Graph", new Color(250, 150, 50));
        gbc.gridy = 3;
        centerPanel.add(inputButton, gbc);

        JButton helpButton = createStyledButton("❓ Help", new Color(150, 100, 200));
        gbc.gridy = 4;
        centerPanel.add(helpButton, gbc);

        welcomePanel.add(centerPanel, BorderLayout.CENTER);

        sampleButton.addActionListener(e -> loadSampleGraph());
        inputButton.addActionListener(e -> showWeightedMatrixInput());
        helpButton.addActionListener(e -> showHelpDialog());

        setContentPane(welcomePanel);
        revalidate();
    }

    private JButton createStyledButton(String text, Color bgColor) {
        JButton button = new JButton(text);
        button.setFont(new Font("Arial", Font.BOLD, 20));
        button.setPreferredSize(new Dimension(380, 60));
        button.setBackground(bgColor);
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));

        button.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) {
                button.setBackground(bgColor.brighter());
            }
            public void mouseExited(MouseEvent e) {
                button.setBackground(bgColor);
            }
        });
        return button;
    }

    private void loadSampleGraph() {
        int[][] weightMatrix = {
                {0, 4, 2, 0, 0, 0},
                {4, 0, 1, 5, 0, 0},
                {2, 1, 0, 8, 10, 0},
                {0, 5, 8, 0, 2, 6},
                {0, 0, 10, 2, 0, 3},
                {0, 0, 0, 6, 3, 0}
        };
        String[] labels = {"A", "B", "C", "D", "E", "F"};
        visualizeGraph(weightMatrix, labels);
    }

    private void showWeightedMatrixInput() {
        JDialog dialog = new JDialog(this, "Input Weighted Adjacency Matrix", true);
        dialog.setSize(900, 700);
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new BorderLayout(10, 10));

        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        topPanel.setBackground(new Color(240, 245, 250));
        JLabel sizeLabel = new JLabel("Nodes:");
        sizeLabel.setFont(new Font("Arial", Font.BOLD, 14));
        JSpinner sizeSpinner = new JSpinner(new SpinnerNumberModel(6, 2, 12, 1));
        topPanel.add(sizeLabel);
        topPanel.add(sizeSpinner);

        JTextArea matrixArea = new JTextArea(12, 35);
        matrixArea.setFont(new Font("Monospaced", Font.PLAIN, 14));
        matrixArea.setText(
                "0 4 2 0 0 0\n" +
                        "4 0 1 5 0 0\n" +
                        "2 1 0 8 10 0\n" +
                        "0 5 8 0 2 6\n" +
                        "0 0 10 2 0 3\n" +
                        "0 0 0 6 3 0"
        );
        JScrollPane matrixScroll = new JScrollPane(matrixArea);
        matrixScroll.setBorder(BorderFactory.createTitledBorder("Weight Matrix (0 = no edge)"));

        JPanel labelPanel = new JPanel(new BorderLayout(5, 5));
        labelPanel.setBackground(new Color(255, 250, 240));
        labelPanel.setBorder(BorderFactory.createTitledBorder("Node Labels"));
        JLabel labelInfo = new JLabel(" One label per line:");
        labelInfo.setFont(new Font("Arial", Font.PLAIN, 12));
        JTextArea labelArea = new JTextArea(12, 15);
        labelArea.setFont(new Font("Monospaced", Font.PLAIN, 14));
        labelArea.setText("A\nB\nC\nD\nE\nF");
        JScrollPane labelScroll = new JScrollPane(labelArea);
        labelPanel.add(labelInfo, BorderLayout.NORTH);
        labelPanel.add(labelScroll, BorderLayout.CENTER);

        JPanel infoPanel = new JPanel();
        infoPanel.setLayout(new BoxLayout(infoPanel, BoxLayout.Y_AXIS));
        infoPanel.setBackground(new Color(255, 250, 240));
        infoPanel.setBorder(BorderFactory.createTitledBorder("Instructions"));
        String[] instructions = {
                "• Enter weights (numbers)",
                "• 0 means no edge",
                "• Space-separated values",
                "• Symmetric for undirected",
                "• Custom labels supported",
                "• Run Dijkstra later!"
        };
        for (String inst : instructions) {
            JLabel lbl = new JLabel(inst);
            lbl.setFont(new Font("Arial", Font.PLAIN, 12));
            infoPanel.add(lbl);
            infoPanel.add(Box.createVerticalStrut(3));
        }

        JPanel buttonPanel = new JPanel(new FlowLayout());
        JButton visButton = createStyledButton("🎨 Visualize", new Color(50, 180, 100));
        visButton.setPreferredSize(new Dimension(180, 45));
        JButton cancelButton = createStyledButton("❌ Cancel", new Color(200, 80, 80));
        cancelButton.setPreferredSize(new Dimension(180, 45));
        buttonPanel.add(visButton);
        buttonPanel.add(cancelButton);

        visButton.addActionListener(e -> {
            try {
                String[] lines = matrixArea.getText().trim().split("\n");
                int n = lines.length;
                int[][] matrix = new int[n][n];

                for (int i = 0; i < n; i++) {
                    String[] values = lines[i].trim().split("\\s+");
                    if (values.length != n) throw new Exception("Matrix must be square!");
                    for (int j = 0; j < n; j++) {
                        matrix[i][j] = Integer.parseInt(values[j]);
                    }
                }

                String[] labelLines = labelArea.getText().trim().split("\n");
                String[] labels = new String[n];
                for (int i = 0; i < n; i++) {
                    labels[i] = i < labelLines.length ? labelLines[i].trim() : String.valueOf(i);
                }

                dialog.dispose();
                visualizeGraph(matrix, labels);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(dialog, "❌ Error: " + ex.getMessage());
            }
        });

        cancelButton.addActionListener(e -> dialog.dispose());

        JPanel centerPanel = new JPanel(new GridLayout(1, 2, 10, 10));
        centerPanel.add(matrixScroll);
        centerPanel.add(labelPanel);

        dialog.add(topPanel, BorderLayout.NORTH);
        dialog.add(centerPanel, BorderLayout.CENTER);
        dialog.add(infoPanel, BorderLayout.EAST);
        dialog.add(buttonPanel, BorderLayout.SOUTH);
        dialog.setVisible(true);
    }

    private void showHelpDialog() {
        String helpText =
                "📊 ADVANCED GRAPH VISUALIZER\n\n" +
                        "FEATURES:\n" +
                        "• Weighted edges with custom labels\n" +
                        "• Editable node labels (double-click)\n" +
                        "• Dijkstra's shortest path algorithm\n" +
                        "• Interactive visualization\n\n" +
                        "WEIGHTED MATRIX:\n" +
                        "• Numbers represent edge weights\n" +
                        "• 0 means no connection\n" +
                        "• Symmetric for undirected graphs\n\n" +
                        "DIJKSTRA ALGORITHM:\n" +
                        "• Finds shortest path between nodes\n" +
                        "• Click 'Run Dijkstra' button\n" +
                        "• Select source and target\n" +
                        "• Watch step-by-step visualization\n\n" +
                        "INTERACTIONS:\n" +
                        "• Drag nodes to move\n" +
                        "• Double-click to rename\n" +
                        "• Hover for information";

        JTextArea textArea = new JTextArea(helpText);
        textArea.setEditable(false);
        textArea.setFont(new Font("Arial", Font.PLAIN, 13));
        JScrollPane scroll = new JScrollPane(textArea);
        scroll.setPreferredSize(new Dimension(500, 400));
        JOptionPane.showMessageDialog(this, scroll, "Help", JOptionPane.INFORMATION_MESSAGE);
    }

    private void visualizeGraph(int[][] weightMatrix, String[] labels) {
        graph = new Graph(weightMatrix, labels);
        graphPanel = new GraphPanel(graph, this);
        setContentPane(graphPanel);
        revalidate();
        repaint();
    }

    public void returnToWelcome() {
        showWelcomeScreen();
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new GraphVisualizer().setVisible(true);
        });
    }
}

// Graph Class
class Graph {
    private int[][] weightMatrix;
    private int numNodes;
    private String[] labels;
    private List<Node> nodes;
    private List<Edge> edges;

    public Graph(int[][] weightMatrix, String[] labels) {
        this.weightMatrix = weightMatrix;
        this.numNodes = weightMatrix.length;
        this.labels = labels;
        this.nodes = new ArrayList<>();
        this.edges = new ArrayList<>();
        createNodes();
        createEdges();
    }

    private void createNodes() {
        int centerX = 650;
        int centerY = 400;
        int radius = Math.min(280, 200 + numNodes * 12);

        for (int i = 0; i < numNodes; i++) {
            double angle = 2 * Math.PI * i / numNodes - Math.PI / 2;
            int x = centerX + (int)(Math.cos(angle) * radius);
            int y = centerY + (int)(Math.sin(angle) * radius);
            nodes.add(new Node(i, x, y, labels[i]));
        }
    }

    private void createEdges() {
        for (int i = 0; i < numNodes; i++) {
            for (int j = i; j < numNodes; j++) {
                if (weightMatrix[i][j] > 0) {
                    edges.add(new Edge(nodes.get(i), nodes.get(j), weightMatrix[i][j]));
                }
            }
        }
    }

    public int[][] getWeightMatrix() { return weightMatrix; }
    public int getNumNodes() { return numNodes; }
    public List<Node> getNodes() { return nodes; }
    public List<Edge> getEdges() { return edges; }
    public String[] getLabels() { return labels; }

    public void updateNodeLabel(int nodeId, String newLabel) {
        labels[nodeId] = newLabel;
        nodes.get(nodeId).setLabel(newLabel);
    }
}

// Node Class
class Node {
    private int id;
    private int x, y;
    private String label;
    private static final int RADIUS = 35;
    private Color color;
    private boolean highlighted;
    private boolean inPath;

    public Node(int id, int x, int y, String label) {
        this.id = id;
        this.x = x;
        this.y = y;
        this.label = label;
        this.color = new Color(100, 150, 250);
        this.highlighted = false;
        this.inPath = false;
    }

    public int getId() { return id; }
    public int getX() { return x; }
    public int getY() { return y; }
    public void setX(int x) { this.x = x; }
    public void setY(int y) { this.y = y; }
    public String getLabel() { return label; }
    public void setLabel(String label) { this.label = label; }
    public int getRadius() { return RADIUS; }
    public Color getColor() { return color; }
    public void setColor(Color color) { this.color = color; }
    public boolean isHighlighted() { return highlighted; }
    public void setHighlighted(boolean highlighted) { this.highlighted = highlighted; }
    public boolean isInPath() { return inPath; }
    public void setInPath(boolean inPath) { this.inPath = inPath; }

    public boolean contains(int px, int py) {
        int dx = px - x;
        int dy = py - y;
        return dx * dx + dy * dy <= RADIUS * RADIUS;
    }

    public void resetVisualization() {
        color = new Color(100, 150, 250);
        inPath = false;
    }
}

// Edge Class
class Edge {
    private Node source;
    private Node target;
    private int weight;
    private Color color;
    private boolean inPath;

    public Edge(Node source, Node target, int weight) {
        this.source = source;
        this.target = target;
        this.weight = weight;
        this.color = new Color(150, 150, 150);
        this.inPath = false;
    }

    public Node getSource() { return source; }
    public Node getTarget() { return target; }
    public int getWeight() { return weight; }
    public Color getColor() { return color; }
    public void setColor(Color color) { this.color = color; }
    public boolean isInPath() { return inPath; }
    public void setInPath(boolean inPath) { this.inPath = inPath; }

    public void resetVisualization() {
        color = new Color(150, 150, 150);
        inPath = false;
    }
}

// Dijkstra Algorithm Class
class DijkstraAlgorithm {
    private Graph graph;
    private int[] distances;
    private int[] previous;
    private boolean[] visited;
    private List<Integer> path;

    public DijkstraAlgorithm(Graph graph) {
        this.graph = graph;
        this.distances = new int[graph.getNumNodes()];
        this.previous = new int[graph.getNumNodes()];
        this.visited = new boolean[graph.getNumNodes()];
        this.path = new ArrayList<>();
    }

    public List<Integer> findShortestPath(int source, int target) {
        int n = graph.getNumNodes();
        Arrays.fill(distances, Integer.MAX_VALUE);
        Arrays.fill(previous, -1);
        Arrays.fill(visited, false);
        distances[source] = 0;

        for (int i = 0; i < n; i++) {
            int u = getMinDistanceNode();
            if (u == -1) break;
            visited[u] = true;

            int[][] weights = graph.getWeightMatrix();
            for (int v = 0; v < n; v++) {
                if (!visited[v] && weights[u][v] > 0) {
                    int newDist = distances[u] + weights[u][v];
                    if (newDist < distances[v]) {
                        distances[v] = newDist;
                        previous[v] = u;
                    }
                }
            }
        }

        if (distances[target] == Integer.MAX_VALUE) {
            return null;
        }

        path.clear();
        int current = target;
        while (current != -1) {
            path.add(0, current);
            current = previous[current];
        }
        return path;
    }

    private int getMinDistanceNode() {
        int minDist = Integer.MAX_VALUE;
        int minNode = -1;
        for (int i = 0; i < graph.getNumNodes(); i++) {
            if (!visited[i] && distances[i] < minDist) {
                minDist = distances[i];
                minNode = i;
            }
        }
        return minNode;
    }

    public int getDistance(int node) {
        return distances[node];
    }
}

// GraphPanel Class
class GraphPanel extends JPanel {
    private Graph graph;
    private GraphVisualizer mainApp;
    private Node draggedNode;
    private Node hoveredNode;
    private DijkstraAlgorithm dijkstra;
    private List<Integer> currentPath;
    private int pathStep = 0;
    private Timer animationTimer;

    public GraphPanel(Graph graph, GraphVisualizer mainApp) {
        this.graph = graph;
        this.mainApp = mainApp;
        setLayout(null);
        setBackground(new Color(30, 40, 60));
        setupUI();
        setupMouseListeners();
    }

    private void setupUI() {
        JButton backButton = new JButton("🏠 Back");
        backButton.setBounds(20, 20, 120, 40);
        styleButton(backButton, new Color(100, 100, 150));
        backButton.addActionListener(e -> mainApp.returnToWelcome());
        add(backButton);

        JButton matrixButton = new JButton("📊 Matrix");
        matrixButton.setBounds(150, 20, 130, 40);
        styleButton(matrixButton, new Color(50, 150, 200));
        matrixButton.addActionListener(e -> showMatrixDialog());
        add(matrixButton);

        JButton dijkstraButton = new JButton("🔍 Run Dijkstra");
        dijkstraButton.setBounds(290, 20, 170, 40);
        styleButton(dijkstraButton, new Color(220, 100, 50));
        dijkstraButton.addActionListener(e -> runDijkstra());
        add(dijkstraButton);

        JButton resetButton = new JButton("🔄 Reset");
        resetButton.setBounds(470, 20, 120, 40);
        styleButton(resetButton, new Color(150, 50, 150));
        resetButton.addActionListener(e -> resetVisualization());
        add(resetButton);
    }

    private void styleButton(JButton btn, Color bg) {
        btn.setFont(new Font("Arial", Font.BOLD, 14));
        btn.setBackground(bg);
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
    }

    private void setupMouseListeners() {
        addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                for (Node node : graph.getNodes()) {
                    if (node.contains(e.getX(), e.getY())) {
                        draggedNode = node;
                        break;
                    }
                }
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                draggedNode = null;
            }

            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    for (Node node : graph.getNodes()) {
                        if (node.contains(e.getX(), e.getY())) {
                            editNodeLabel(node);
                            break;
                        }
                    }
                }
            }
        });

        addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseDragged(MouseEvent e) {
                if (draggedNode != null) {
                    draggedNode.setX(e.getX());
                    draggedNode.setY(e.getY());
                    repaint();
                }
            }

            @Override
            public void mouseMoved(MouseEvent e) {
                hoveredNode = null;
                for (Node node : graph.getNodes()) {
                    node.setHighlighted(node.contains(e.getX(), e.getY()));
                    if (node.isHighlighted()) hoveredNode = node;
                }
                repaint();
            }
        });
    }

    private void editNodeLabel(Node node) {
        String newLabel = JOptionPane.showInputDialog(this,
                "Enter new label for node " + node.getId() + ":",
                node.getLabel());
        if (newLabel != null && !newLabel.trim().isEmpty()) {
            graph.updateNodeLabel(node.getId(), newLabel.trim());
            repaint();
        }
    }

    private void runDijkstra() {
        String[] nodeLabels = graph.getLabels();
        String source = (String) JOptionPane.showInputDialog(this,
                "Select source node:", "Dijkstra - Source",
                JOptionPane.QUESTION_MESSAGE, null, nodeLabels, nodeLabels[0]);

        if (source == null) return;

        String target = (String) JOptionPane.showInputDialog(this,
                "Select target node:", "Dijkstra - Target",
                JOptionPane.QUESTION_MESSAGE, null, nodeLabels, nodeLabels[nodeLabels.length-1]);

        if (target == null) return;

        int sourceIdx = Arrays.asList(nodeLabels).indexOf(source);
        int targetIdx = Arrays.asList(nodeLabels).indexOf(target);

        dijkstra = new DijkstraAlgorithm(graph);
        currentPath = dijkstra.findShortestPath(sourceIdx, targetIdx);

        if (currentPath == null) {
            JOptionPane.showMessageDialog(this, "No path exists!");
            return;
        }

        resetVisualization();
        animatePath();

        int distance = dijkstra.getDistance(targetIdx);
        JOptionPane.showMessageDialog(this,
                "Shortest path: " + getPathString(currentPath) + "\nTotal distance: " + distance,
                "Dijkstra Result", JOptionPane.INFORMATION_MESSAGE);
    }

    private void animatePath() {
        pathStep = 0;
        if (animationTimer != null) animationTimer.stop();

        animationTimer = new Timer(500, new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (pathStep < currentPath.size()) {
                    int nodeId = currentPath.get(pathStep);
                    Node node = graph.getNodes().get(nodeId);
                    node.setColor(new Color(255, 200, 50));
                    node.setInPath(true);

                    if (pathStep > 0) {
                        int prevId = currentPath.get(pathStep - 1);
                        highlightEdge(prevId, nodeId);
                    }

                    pathStep++;
                    repaint();
                } else {
                    animationTimer.stop();
                }
            }
        });
        animationTimer.start();
    }

    private void highlightEdge(int from, int to) {
        for (Edge edge : graph.getEdges()) {
            if ((edge.getSource().getId() == from && edge.getTarget().getId() == to) ||
                    (edge.getSource().getId() == to && edge.getTarget().getId() == from)) {
                edge.setColor(new Color(255, 200, 50));
                edge.setInPath(true);
                break;
            }
        }
    }

    private String getPathString(List<Integer> path) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < path.size(); i++) {
            sb.append(graph.getLabels()[path.get(i)]);
            if (i < path.size() - 1) sb.append(" → ");
        }
        return sb.toString();
    }

    private void resetVisualization() {
        for (Node node : graph.getNodes()) {
            node.resetVisualization();
        }
        for (Edge edge : graph.getEdges()) {
            edge.resetVisualization();
        }
        currentPath = null;
        pathStep = 0;
        if (animationTimer != null) animationTimer.stop();
        repaint();
    }

    private void showMatrixDialog() {
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this),
                "Weight Matrix", true);
        dialog.setSize(600, 550);
        dialog.setLocationRelativeTo(this);

        int[][] matrix = graph.getWeightMatrix();
        int n = matrix.length;
        String[] labels = graph.getLabels();

        JPanel matrixPanel = new JPanel(new GridLayout(n + 1, n + 1, 2, 2));
        matrixPanel.setBackground(Color.WHITE);
        matrixPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        matrixPanel.add(new JLabel(""));
        for (int i = 0; i < n; i++) {
            JLabel lbl = new JLabel(labels[i], SwingConstants.CENTER);
            lbl.setFont(new Font("Arial", Font.BOLD, 14));
            matrixPanel.add(lbl);
        }

        for (int i = 0; i < n; i++) {
            JLabel rowLbl = new JLabel(labels[i], SwingConstants.CENTER);
            rowLbl.setFont(new Font("Arial", Font.BOLD, 14));
            matrixPanel.add(rowLbl);

            for (int j = 0; j < n; j++) {
                JLabel cellLbl = new JLabel(String.valueOf(matrix[i][j]), SwingConstants.CENTER);
                cellLbl.setFont(new Font("Monospaced", Font.PLAIN, 14));
                cellLbl.setOpaque(true);
                cellLbl.setBorder(BorderFactory.createLineBorder(Color.GRAY));
                cellLbl.setBackground(matrix[i][j] > 0 ? new Color(200, 255, 200) : Color.WHITE);
                matrixPanel.add(cellLbl);
            }
        }

        dialog.add(new JScrollPane(matrixPanel));
        dialog.setVisible(true);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        drawEdges(g2d);
        drawNodes(g2d);
        drawInfo(g2d);
    }

    private void drawEdges(Graphics2D g2d) {
        g2d.setStroke(new BasicStroke(3));
        for (Edge edge : graph.getEdges()) {
            Node src = edge.getSource();
            Node tgt = edge.getTarget();

            g2d.setColor(edge.isInPath() ? new Color(255, 200, 50) : edge.getColor());
            g2d.drawLine(src.getX(), src.getY(), tgt.getX(), tgt.getY());

            int midX = (src.getX() + tgt.getX()) / 2;
            int midY = (src.getY() + tgt.getY()) / 2;
            g2d.setFont(new Font("Arial", Font.BOLD, 16));
            g2d.setColor(Color.WHITE);
            g2d.fillOval(midX - 15, midY - 15, 30, 30);
            g2d.setColor(new Color(50, 50, 100));
            g2d.drawString(String.valueOf(edge.getWeight()), midX - 8, midY + 6);
        }
    }

    private void drawNodes(Graphics2D g2d) {
        for (Node node : graph.getNodes()) {
            int x = node.getX();
            int y = node.getY();
            int r = node.getRadius();

            if (node.isHighlighted()) {
                g2d.setColor(new Color(255, 255, 100));
                g2d.fillOval(x - r - 5, y - r - 5, 2*r + 10, 2*r + 10);
            }

            g2d.setColor(node.getColor());
            g2d.fillOval(x - r, y - r, 2 * r, 2 * r);

            g2d.setColor(Color.WHITE);
            g2d.setStroke(new BasicStroke(3));
            g2d.drawOval(x - r, y - r, 2 * r, 2 * r);

            g2d.setColor(Color.WHITE);
            g2d.setFont(new Font("Arial", Font.BOLD, 18));
            String label = node.getLabel();
            FontMetrics fm = g2d.getFontMetrics();
            int textX = x - fm.stringWidth(label) / 2;
            int textY = y + fm.getAscent() / 2 - 2;
            g2d.drawString(label, textX, textY);
        }
    }

    private void drawInfo(Graphics2D g2d) {
        g2d.setColor(Color.WHITE);
        g2d.setFont(new Font("Arial", Font.PLAIN, 14));

        g2d.drawString("📊 Weighted Graph", 20, getHeight() - 60);
        g2d.drawString("🔵 Nodes: " + graph.getNumNodes(), 20, getHeight() - 40);
        g2d.drawString("➡️ Edges: " + graph.getEdges().size(), 20, getHeight() - 20);

        g2d.drawString("💡 Double-click node to rename", getWidth() - 280, getHeight() - 40);
        g2d.drawString("🖱️ Drag nodes to move", getWidth() - 280, getHeight() - 20);
    }
}