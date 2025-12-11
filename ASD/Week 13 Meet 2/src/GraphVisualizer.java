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
        setTitle("Advanced Graph Visualizer with Dijkstra Animation");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1400, 900);
        setLocationRelativeTo(null);
        showWelcomeScreen();
    }

    private void showWelcomeScreen() {
        JPanel welcomePanel = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                // Gradient background
                GradientPaint gradient = new GradientPaint(0, 0, new Color(20, 30, 50),
                        0, getHeight(), new Color(40, 60, 90));
                g2d.setPaint(gradient);
                g2d.fillRect(0, 0, getWidth(), getHeight());

                // Decorative circles
                g2d.setColor(new Color(100, 150, 255, 30));
                for (int i = 0; i < 5; i++) {
                    int size = 100 + i * 80;
                    g2d.fillOval(getWidth() - 300 - i * 40, 100 + i * 60, size, size);
                }
            }
        };
        welcomePanel.setLayout(new GridBagLayout());

        JPanel centerPanel = new JPanel();
        centerPanel.setLayout(new BoxLayout(centerPanel, BoxLayout.Y_AXIS));
        centerPanel.setOpaque(false);

        JLabel titleLabel = new JLabel("📊 GRAPH VISUALIZER PRO");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 56));
        titleLabel.setForeground(new Color(100, 200, 255));
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel subtitleLabel = new JLabel("Dijkstra's Algorithm with Beautiful Animation");
        subtitleLabel.setFont(new Font("Arial", Font.ITALIC, 22));
        subtitleLabel.setForeground(new Color(200, 220, 255));
        subtitleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.Y_AXIS));
        buttonPanel.setOpaque(false);
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(40, 0, 0, 0));

        JButton sampleButton = createGlowButton("📈 Load Sample Graph", new Color(50, 150, 250));
        JButton inputButton = createGlowButton("⌨️ Input Custom Graph", new Color(250, 150, 50));
        JButton helpButton = createGlowButton("❓ Help & Guide", new Color(150, 100, 200));

        buttonPanel.add(sampleButton);
        buttonPanel.add(Box.createVerticalStrut(15));
        buttonPanel.add(inputButton);
        buttonPanel.add(Box.createVerticalStrut(15));
        buttonPanel.add(helpButton);

        centerPanel.add(titleLabel);
        centerPanel.add(Box.createVerticalStrut(20));
        centerPanel.add(subtitleLabel);
        centerPanel.add(buttonPanel);

        welcomePanel.add(centerPanel);

        sampleButton.addActionListener(e -> loadSampleGraph());
        inputButton.addActionListener(e -> showWeightedMatrixInput());
        helpButton.addActionListener(e -> showHelpDialog());

        setContentPane(welcomePanel);
        revalidate();
    }

    private JButton createGlowButton(String text, Color baseColor) {
        JButton button = new JButton(text) {
            private boolean hovered = false;

            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                Color bgColor = hovered ? baseColor.brighter() : baseColor;

                // Shadow effect
                g2d.setColor(new Color(0, 0, 0, 50));
                g2d.fillRoundRect(5, 5, getWidth() - 5, getHeight() - 5, 20, 20);

                // Button background with gradient
                GradientPaint gradient = new GradientPaint(0, 0, bgColor, 0, getHeight(), bgColor.darker());
                g2d.setPaint(gradient);
                g2d.fillRoundRect(0, 0, getWidth() - 5, getHeight() - 5, 20, 20);

                // Glow effect when hovered
                if (hovered) {
                    g2d.setColor(new Color(255, 255, 255, 30));
                    g2d.setStroke(new BasicStroke(3));
                    g2d.drawRoundRect(0, 0, getWidth() - 5, getHeight() - 5, 20, 20);
                }

                // Text
                g2d.setColor(Color.WHITE);
                g2d.setFont(getFont());
                FontMetrics fm = g2d.getFontMetrics();
                int x = (getWidth() - fm.stringWidth(getText())) / 2;
                int y = (getHeight() + fm.getAscent() - fm.getDescent()) / 2;
                g2d.drawString(getText(), x, y);
            }
        };

        button.setFont(new Font("Arial", Font.BOLD, 20));
        button.setPreferredSize(new Dimension(400, 65));
        button.setMaximumSize(new Dimension(400, 65));
        button.setAlignmentX(Component.CENTER_ALIGNMENT);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setContentAreaFilled(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));

        button.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) {
                ((JButton)e.getSource()).putClientProperty("hovered", true);
                button.repaint();
            }
            public void mouseExited(MouseEvent e) {
                ((JButton)e.getSource()).putClientProperty("hovered", false);
                button.repaint();
            }
        });

        return button;
    }

    private void loadSampleGraph() {
        int[][] weightMatrix = {
                {0, 7, 9, 0, 0, 14},
                {7, 0, 10, 15, 0, 0},
                {9, 10, 0, 11, 0, 2},
                {0, 15, 11, 0, 6, 0},
                {0, 0, 0, 6, 0, 9},
                {14, 0, 2, 0, 9, 0}
        };
        String[] labels = {"A", "B", "C", "D", "E", "F"};
        visualizeGraph(weightMatrix, labels);
    }

    private void showWeightedMatrixInput() {
        JDialog dialog = new JDialog(this, "Input Weighted Graph", true);
        dialog.setSize(950, 750);
        dialog.setLocationRelativeTo(this);

        JPanel mainPanel = new JPanel(new BorderLayout(15, 15));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        mainPanel.setBackground(new Color(245, 248, 252));

        // Top Panel
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 10));
        topPanel.setBackground(new Color(230, 240, 250));
        topPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(100, 150, 200), 2),
                BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));

        JLabel sizeLabel = new JLabel("Number of Nodes:");
        sizeLabel.setFont(new Font("Arial", Font.BOLD, 15));
        JSpinner sizeSpinner = new JSpinner(new SpinnerNumberModel(6, 2, 12, 1));
        sizeSpinner.setFont(new Font("Arial", Font.PLAIN, 15));

        topPanel.add(sizeLabel);
        topPanel.add(sizeSpinner);

        // Center Panel with Matrix Input
        JPanel centerPanel = new JPanel(new GridLayout(1, 2, 15, 0));
        centerPanel.setOpaque(false);

        JPanel matrixPanel = new JPanel(new BorderLayout(5, 5));
        matrixPanel.setBackground(Color.WHITE);
        matrixPanel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(new Color(100, 150, 200), 2),
                "Weight Matrix (0 = no edge)",
                0, 0, new Font("Arial", Font.BOLD, 14), new Color(50, 100, 150)
        ));

        JTextArea matrixArea = new JTextArea(15, 30);
        matrixArea.setFont(new Font("Consolas", Font.PLAIN, 14));
        matrixArea.setBackground(new Color(250, 252, 255));
        matrixArea.setCaretColor(new Color(50, 100, 150));
        matrixArea.setText(
                "0 7 9 0 0 14\n" +
                        "7 0 10 15 0 0\n" +
                        "9 10 0 11 0 2\n" +
                        "0 15 11 0 6 0\n" +
                        "0 0 0 6 0 9\n" +
                        "14 0 2 0 9 0"
        );
        JScrollPane matrixScroll = new JScrollPane(matrixArea);
        matrixPanel.add(matrixScroll, BorderLayout.CENTER);

        JPanel labelPanel = new JPanel(new BorderLayout(5, 5));
        labelPanel.setBackground(Color.WHITE);
        labelPanel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(new Color(150, 100, 200), 2),
                "Node Labels",
                0, 0, new Font("Arial", Font.BOLD, 14), new Color(100, 50, 150)
        ));

        JTextArea labelArea = new JTextArea(15, 12);
        labelArea.setFont(new Font("Consolas", Font.PLAIN, 14));
        labelArea.setBackground(new Color(255, 250, 255));
        labelArea.setText("A\nB\nC\nD\nE\nF");
        JScrollPane labelScroll = new JScrollPane(labelArea);
        labelPanel.add(labelScroll, BorderLayout.CENTER);

        centerPanel.add(matrixPanel);
        centerPanel.add(labelPanel);

        // Info Panel
        JPanel infoPanel = new JPanel();
        infoPanel.setLayout(new BoxLayout(infoPanel, BoxLayout.Y_AXIS));
        infoPanel.setBackground(new Color(255, 250, 235));
        infoPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(200, 150, 50), 2),
                BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));

        JLabel infoTitle = new JLabel("📝 Quick Guide:");
        infoTitle.setFont(new Font("Arial", Font.BOLD, 14));
        infoTitle.setForeground(new Color(150, 100, 0));
        infoPanel.add(infoTitle);
        infoPanel.add(Box.createVerticalStrut(8));

        String[] tips = {
                "• Matrix must be square (NxN)",
                "• Use numbers for weights",
                "• 0 means no connection",
                "• Symmetric = undirected graph",
                "• One label per line",
                "• Try Dijkstra after creating!"
        };

        for (String tip : tips) {
            JLabel tipLabel = new JLabel(tip);
            tipLabel.setFont(new Font("Arial", Font.PLAIN, 12));
            tipLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
            infoPanel.add(tipLabel);
            infoPanel.add(Box.createVerticalStrut(4));
        }

        // Button Panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));
        buttonPanel.setOpaque(false);

        JButton createButton = createStyledButton("🎨 Create Graph", new Color(50, 180, 100));
        JButton cancelButton = createStyledButton("❌ Cancel", new Color(220, 80, 80));

        buttonPanel.add(createButton);
        buttonPanel.add(cancelButton);

        createButton.addActionListener(e -> {
            try {
                String[] lines = matrixArea.getText().trim().split("\n");
                int n = lines.length;
                int[][] matrix = new int[n][n];

                for (int i = 0; i < n; i++) {
                    String[] values = lines[i].trim().split("\\s+");
                    if (values.length != n) throw new Exception("Matrix must be square! Row " + (i+1) + " has " + values.length + " values.");
                    for (int j = 0; j < n; j++) {
                        matrix[i][j] = Integer.parseInt(values[j]);
                        if (matrix[i][j] < 0) throw new Exception("Weights must be non-negative!");
                    }
                }

                String[] labelLines = labelArea.getText().trim().split("\n");
                String[] labels = new String[n];
                for (int i = 0; i < n; i++) {
                    labels[i] = i < labelLines.length && !labelLines[i].trim().isEmpty()
                            ? labelLines[i].trim() : String.valueOf(i);
                }

                dialog.dispose();
                visualizeGraph(matrix, labels);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(dialog,
                        "❌ Invalid Input!\n\n" + ex.getMessage(),
                        "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        cancelButton.addActionListener(e -> dialog.dispose());

        mainPanel.add(topPanel, BorderLayout.NORTH);
        mainPanel.add(centerPanel, BorderLayout.CENTER);
        mainPanel.add(infoPanel, BorderLayout.EAST);
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);

        dialog.add(mainPanel);
        dialog.setVisible(true);
    }

    private JButton createStyledButton(String text, Color bgColor) {
        JButton button = new JButton(text);
        button.setFont(new Font("Arial", Font.BOLD, 16));
        button.setPreferredSize(new Dimension(200, 50));
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

    private void showHelpDialog() {
        String helpText =
                "📊 ADVANCED GRAPH VISUALIZER\n\n" +
                        "✨ FEATURES:\n" +
                        "• Weighted graphs with custom node labels\n" +
                        "• Drag nodes to rearrange layout\n" +
                        "• Double-click nodes to rename\n" +
                        "• Dijkstra's algorithm with smooth animation\n" +
                        "• View adjacency/weight matrix\n\n" +
                        "🔍 DIJKSTRA'S ALGORITHM:\n" +
                        "• Finds shortest path between two nodes\n" +
                        "• Click 'Run Dijkstra' button\n" +
                        "• Select source and destination nodes\n" +
                        "• Watch beautiful step-by-step visualization\n" +
                        "• See total distance and path sequence\n\n" +
                        "⌨️ INPUT FORMAT:\n" +
                        "• Weight Matrix: space-separated numbers\n" +
                        "• 0 means no edge exists\n" +
                        "• Symmetric matrix for undirected graphs\n" +
                        "• Labels: one per line\n\n" +
                        "🖱️ INTERACTIONS:\n" +
                        "• Drag: Move nodes around\n" +
                        "• Double-click: Rename node\n" +
                        "• Hover: View node information\n" +
                        "• Click Matrix: View weight table";

        JTextArea textArea = new JTextArea(helpText);
        textArea.setEditable(false);
        textArea.setFont(new Font("Arial", Font.PLAIN, 13));
        textArea.setBackground(new Color(250, 250, 255));
        textArea.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JScrollPane scrollPane = new JScrollPane(textArea);
        scrollPane.setPreferredSize(new Dimension(550, 450));

        JOptionPane.showMessageDialog(this, scrollPane, "Help & Guide", JOptionPane.INFORMATION_MESSAGE);
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
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }

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
        int centerX = 700;
        int centerY = 420;
        int radius = Math.min(300, 220 + numNodes * 15);

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
        if (nodeId >= 0 && nodeId < labels.length) {
            labels[nodeId] = newLabel;
            nodes.get(nodeId).setLabel(newLabel);
        }
    }
}

// Node Class
class Node {
    private int id;
    private int x, y;
    private String label;
    private static final int RADIUS = 38;
    private Color color;
    private Color textColor;
    private boolean highlighted;
    private boolean inPath;
    private boolean isProcessing;
    private int distance = Integer.MAX_VALUE;

    public Node(int id, int x, int y, String label) {
        this.id = id;
        this.x = x;
        this.y = y;
        this.label = label;
        this.color = new Color(100, 150, 250);
        this.textColor = Color.WHITE;
        this.highlighted = false;
        this.inPath = false;
        this.isProcessing = false;
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
    public Color getTextColor() { return textColor; }
    public void setColor(Color color) { this.color = color; }
    public void setTextColor(Color color) { this.textColor = color; }
    public boolean isHighlighted() { return highlighted; }
    public void setHighlighted(boolean highlighted) { this.highlighted = highlighted; }
    public boolean isInPath() { return inPath; }
    public void setInPath(boolean inPath) { this.inPath = inPath; }
    public boolean isProcessing() { return isProcessing; }
    public void setProcessing(boolean processing) { this.isProcessing = processing; }
    public int getDistance() { return distance; }
    public void setDistance(int distance) { this.distance = distance; }

    public boolean contains(int px, int py) {
        int dx = px - x;
        int dy = py - y;
        return dx * dx + dy * dy <= (RADIUS + 5) * (RADIUS + 5);
    }

    public void resetVisualization() {
        color = new Color(100, 150, 250);
        textColor = Color.WHITE;
        inPath = false;
        isProcessing = false;
        distance = Integer.MAX_VALUE;
    }
}

// Edge Class
class Edge {
    private Node source;
    private Node target;
    private int weight;
    private Color color;
    private boolean inPath;
    private boolean isExploring;
    private float alpha = 1.0f;

    public Edge(Node source, Node target, int weight) {
        this.source = source;
        this.target = target;
        this.weight = weight;
        this.color = new Color(150, 150, 180);
        this.inPath = false;
        this.isExploring = false;
    }

    public Node getSource() { return source; }
    public Node getTarget() { return target; }
    public int getWeight() { return weight; }
    public Color getColor() { return color; }
    public void setColor(Color color) { this.color = color; }
    public boolean isInPath() { return inPath; }
    public void setInPath(boolean inPath) { this.inPath = inPath; }
    public boolean isExploring() { return isExploring; }
    public void setExploring(boolean exploring) { this.isExploring = exploring; }
    public float getAlpha() { return alpha; }
    public void setAlpha(float alpha) { this.alpha = alpha; }

    public void resetVisualization() {
        color = new Color(150, 150, 180);
        inPath = false;
        isExploring = false;
        alpha = 1.0f;
    }
}

// Dijkstra Algorithm with Animation Support
class DijkstraAlgorithm {
    private Graph graph;
    private int[] distances;
    private int[] previous;
    private boolean[] visited;
    private List<Integer> finalPath;
    private List<DijkstraStep> steps;

    public DijkstraAlgorithm(Graph graph) {
        this.graph = graph;
        this.distances = new int[graph.getNumNodes()];
        this.previous = new int[graph.getNumNodes()];
        this.visited = new boolean[graph.getNumNodes()];
        this.steps = new ArrayList<>();
        this.finalPath = new ArrayList<>();
    }

    public boolean findShortestPath(int source, int target) {
        int n = graph.getNumNodes();
        Arrays.fill(distances, Integer.MAX_VALUE);
        Arrays.fill(previous, -1);
        Arrays.fill(visited, false);
        distances[source] = 0;

        // Add initial step
        steps.add(new DijkstraStep(source, -1, 0, "Starting at " + graph.getLabels()[source]));

        for (int i = 0; i < n; i++) {
            int u = getMinDistanceNode();
            if (u == -1) break;

            visited[u] = true;
            steps.add(new DijkstraStep(u, -1, distances[u], "Visiting " + graph.getLabels()[u] + " (distance: " + distances[u] + ")"));

            if (u == target) break;

            int[][] weights = graph.getWeightMatrix();
            for (int v = 0; v < n; v++) {
                if (!visited[v] && weights[u][v] > 0) {
                    int newDist = distances[u] + weights[u][v];
                    steps.add(new DijkstraStep(u, v, newDist, "Exploring edge " + graph.getLabels()[u] + " → " + graph.getLabels()[v]));

                    if (newDist < distances[v]) {
                        distances[v] = newDist;
                        previous[v] = u;
                        steps.add(new DijkstraStep(v, u, newDist, "Updated " + graph.getLabels()[v] + " distance to " + newDist));
                    }
                }
            }
        }

        if (distances[target] == Integer.MAX_VALUE) {
            return false;
        }

        // Build final path
        finalPath.clear();
        int current = target;
        while (current != -1) {
            finalPath.add(0, current);
            current = previous[current];
        }

        return true;
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

    public List<DijkstraStep> getSteps() { return steps; }
    public List<Integer> getFinalPath() { return finalPath; }
    public int getDistance(int node) { return distances[node]; }
}

// Dijkstra Step for Animation
class DijkstraStep {
    int currentNode;
    int exploringNode;
    int distance;
    String description;

    public DijkstraStep(int current, int exploring, int dist, String desc) {
        this.currentNode = current;
        this.exploringNode = exploring;
        this.distance = dist;
        this.description = desc;
    }
}

// GraphPanel with Enhanced UI
class GraphPanel extends JPanel {
    private Graph graph;
    private GraphVisualizer mainApp;
    private Node draggedNode;
    private Node hoveredNode;
    private DijkstraAlgorithm dijkstra;
    private List<DijkstraStep> dijkstraSteps;
    private int currentStep = 0;
    private Timer animationTimer;
    private JLabel statusLabel;
    private JProgressBar progressBar;
    private boolean isAnimating = false;

    public GraphPanel(Graph graph, GraphVisualizer mainApp) {
        this.graph = graph;
        this.mainApp = mainApp;
        setLayout(null);
        setBackground(new Color(30, 40, 60));
        setupUI();
        setupMouseListeners();
    }

    private void setupUI() {
        // Modern styled buttons with icons
        JButton backButton = createModernButton("🏠", "Back");
        backButton.setBounds(20, 20, 90, 45);
        backButton.addActionListener(e -> {
            if (animationTimer != null) animationTimer.stop();
            mainApp.returnToWelcome();
        });
        add(backButton);

        JButton matrixButton = createModernButton("📊", "Matrix");
        matrixButton.setBounds(120, 20, 110, 45);
        matrixButton.addActionListener(e -> showMatrixDialog());
        add(matrixButton);

        JButton dijkstraButton = createModernButton("🔍", "Dijkstra");
        dijkstraButton.setBounds(240, 20, 130, 45);
        dijkstraButton.addActionListener(e -> {
            if (!isAnimating) runDijkstra();
        });
        add(dijkstraButton);

        JButton resetButton = createModernButton("🔄", "Reset");
        resetButton.setBounds(380, 20, 110, 45);
        resetButton.addActionListener(e -> resetVisualization());
        add(resetButton);

        // Status Label
        statusLabel = new JLabel("Ready to visualize");
        statusLabel.setBounds(20, 75, 600, 30);
        statusLabel.setFont(new Font("Arial", Font.BOLD, 14));
        statusLabel.setForeground(new Color(200, 220, 255));
        add(statusLabel);

        // Progress Bar
        progressBar = new JProgressBar(0, 100);
        progressBar.setBounds(20, 110, 470, 20);
        progressBar.setStringPainted(true);
        progressBar.setForeground(new Color(100, 200, 150));
        progressBar.setBackground(new Color(50, 60, 80));
        progressBar.setVisible(false);
        add(progressBar);
    }

    private JButton createModernButton(String icon, String text) {
        JButton button = new JButton(icon + " " + text);
        button.setFont(new Font("Arial", Font.BOLD, 14));
        button.setBackground(new Color(70, 90, 120));
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(100, 120, 150), 2),
                BorderFactory.createEmptyBorder(5, 10, 5, 10)
        ));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));

        button.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) {
                button.setBackground(new Color(90, 110, 140));
            }
            public void mouseExited(MouseEvent e) {
                button.setBackground(new Color(70, 90, 120));
            }
        });

        return button;
    }

    private void setupMouseListeners() {
        addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if (isAnimating) return;
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
                if (e.getClickCount() == 2 && !isAnimating) {
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
                if (draggedNode != null && !isAnimating) {
                    draggedNode.setX(e.getX());
                    draggedNode.setY(e.getY());
                    repaint();
                }
            }

            @Override
            public void mouseMoved(MouseEvent e) {
                if (isAnimating) return;
                hoveredNode = null;
                for (Node node : graph.getNodes()) {
                    boolean wasHighlighted = node.isHighlighted();
                    node.setHighlighted(node.contains(e.getX(), e.getY()));
                    if (node.isHighlighted()) {
                        hoveredNode = node;
                        if (!wasHighlighted) repaint();
                    } else if (wasHighlighted) {
                        repaint();
                    }
                }
            }
        });
    }

    private void editNodeLabel(Node node) {
        String newLabel = JOptionPane.showInputDialog(this,
                "Enter new label for node " + node.getLabel() + ":",
                "Edit Node Label",
                JOptionPane.QUESTION_MESSAGE);

        if (newLabel != null && !newLabel.trim().isEmpty()) {
            graph.updateNodeLabel(node.getId(), newLabel.trim());
            repaint();
        }
    }

    private void runDijkstra() {
        String[] nodeLabels = graph.getLabels();

        String source = (String) JOptionPane.showInputDialog(this,
                "Select source node:",
                "Dijkstra Algorithm - Source",
                JOptionPane.QUESTION_MESSAGE,
                null,
                nodeLabels,
                nodeLabels[0]);

        if (source == null) return;

        String target = (String) JOptionPane.showInputDialog(this,
                "Select target node:",
                "Dijkstra Algorithm - Target",
                JOptionPane.QUESTION_MESSAGE,
                null,
                nodeLabels,
                nodeLabels[nodeLabels.length - 1]);

        if (target == null) return;

        int sourceIdx = -1, targetIdx = -1;
        for (int i = 0; i < nodeLabels.length; i++) {
            if (nodeLabels[i].equals(source)) sourceIdx = i;
            if (nodeLabels[i].equals(target)) targetIdx = i;
        }

        if (sourceIdx == -1 || targetIdx == -1) {
            JOptionPane.showMessageDialog(this, "Invalid node selection!");
            return;
        }

        resetVisualization();

        dijkstra = new DijkstraAlgorithm(graph);
        boolean pathExists = dijkstra.findShortestPath(sourceIdx, targetIdx);

        if (!pathExists) {
            JOptionPane.showMessageDialog(this,
                    "No path exists between " + source + " and " + target + "!",
                    "No Path Found",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        dijkstraSteps = dijkstra.getSteps();
        currentStep = 0;
        isAnimating = true;
        progressBar.setVisible(true);
        progressBar.setMaximum(dijkstraSteps.size());

        animateDijkstra(sourceIdx, targetIdx);
    }

    private void animateDijkstra(int source, int target) {
        if (animationTimer != null) {
            animationTimer.stop();
        }

        animationTimer = new Timer(600, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (currentStep < dijkstraSteps.size()) {
                    DijkstraStep step = dijkstraSteps.get(currentStep);

                    // Update status
                    statusLabel.setText("Step " + (currentStep + 1) + "/" + dijkstraSteps.size() + ": " + step.description);
                    progressBar.setValue(currentStep + 1);

                    // Visualize current node
                    Node currentNode = graph.getNodes().get(step.currentNode);
                    currentNode.setProcessing(true);
                    currentNode.setColor(new Color(255, 180, 50));
                    currentNode.setDistance(step.distance);

                    // Visualize exploring edge
                    if (step.exploringNode != -1) {
                        highlightEdgeExploring(step.currentNode, step.exploringNode);
                    }

                    currentStep++;
                    repaint();
                } else {
                    // Animation complete - show final path
                    animationTimer.stop();
                    showFinalPath(source, target);
                }
            }
        });

        animationTimer.start();
    }

    private void showFinalPath(int source, int target) {
        List<Integer> path = dijkstra.getFinalPath();

        if (path != null && !path.isEmpty()) {
            // Reset all nodes
            for (Node node : graph.getNodes()) {
                node.setProcessing(false);
                node.setColor(new Color(100, 150, 250));
            }

            // Highlight path nodes
            for (int nodeId : path) {
                Node node = graph.getNodes().get(nodeId);
                node.setInPath(true);
                node.setColor(new Color(100, 255, 150));
            }

            // Highlight path edges
            for (int i = 0; i < path.size() - 1; i++) {
                highlightEdgeInPath(path.get(i), path.get(i + 1));
            }

            repaint();

            // Show result dialog
            StringBuilder pathStr = new StringBuilder();
            for (int i = 0; i < path.size(); i++) {
                pathStr.append(graph.getLabels()[path.get(i)]);
                if (i < path.size() - 1) pathStr.append(" → ");
            }

            int distance = dijkstra.getDistance(target);

            String message = String.format(
                    "✅ Shortest Path Found!\n\n" +
                            "Path: %s\n\n" +
                            "Total Distance: %d\n" +
                            "Number of Hops: %d",
                    pathStr.toString(),
                    distance,
                    path.size() - 1
            );

            statusLabel.setText("✅ Path found: " + pathStr.toString() + " (Distance: " + distance + ")");

            JOptionPane.showMessageDialog(this,
                    message,
                    "Dijkstra Result",
                    JOptionPane.INFORMATION_MESSAGE);
        }

        isAnimating = false;
        progressBar.setVisible(false);
    }

    private void highlightEdgeExploring(int from, int to) {
        for (Edge edge : graph.getEdges()) {
            if ((edge.getSource().getId() == from && edge.getTarget().getId() == to) ||
                    (edge.getSource().getId() == to && edge.getTarget().getId() == from)) {
                edge.setExploring(true);
                edge.setColor(new Color(255, 200, 100));
                break;
            }
        }
    }

    private void highlightEdgeInPath(int from, int to) {
        for (Edge edge : graph.getEdges()) {
            if ((edge.getSource().getId() == from && edge.getTarget().getId() == to) ||
                    (edge.getSource().getId() == to && edge.getTarget().getId() == from)) {
                edge.setInPath(true);
                edge.setColor(new Color(100, 255, 150));
                break;
            }
        }
    }

    private void resetVisualization() {
        if (animationTimer != null) {
            animationTimer.stop();
        }

        for (Node node : graph.getNodes()) {
            node.resetVisualization();
        }

        for (Edge edge : graph.getEdges()) {
            edge.resetVisualization();
        }

        currentStep = 0;
        isAnimating = false;
        statusLabel.setText("Ready to visualize");
        progressBar.setVisible(false);
        progressBar.setValue(0);
        repaint();
    }

    private void showMatrixDialog() {
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this),
                "Weight Matrix", true);
        dialog.setSize(700, 600);
        dialog.setLocationRelativeTo(this);

        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        mainPanel.setBackground(new Color(245, 248, 252));

        JLabel titleLabel = new JLabel("📊 Adjacency Weight Matrix");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 20));
        titleLabel.setHorizontalAlignment(SwingConstants.CENTER);
        titleLabel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        int[][] matrix = graph.getWeightMatrix();
        int n = matrix.length;
        String[] labels = graph.getLabels();

        JPanel matrixPanel = new JPanel(new GridLayout(n + 1, n + 1, 3, 3));
        matrixPanel.setBackground(Color.WHITE);
        matrixPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Empty corner cell
        JLabel cornerLabel = new JLabel("");
        cornerLabel.setOpaque(true);
        cornerLabel.setBackground(new Color(230, 240, 250));
        matrixPanel.add(cornerLabel);

        // Header row
        for (int i = 0; i < n; i++) {
            JLabel label = new JLabel(labels[i], SwingConstants.CENTER);
            label.setFont(new Font("Arial", Font.BOLD, 16));
            label.setOpaque(true);
            label.setBackground(new Color(200, 220, 255));
            label.setBorder(BorderFactory.createLineBorder(new Color(100, 150, 200), 2));
            matrixPanel.add(label);
        }

        // Data rows
        for (int i = 0; i < n; i++) {
            JLabel rowLabel = new JLabel(labels[i], SwingConstants.CENTER);
            rowLabel.setFont(new Font("Arial", Font.BOLD, 16));
            rowLabel.setOpaque(true);
            rowLabel.setBackground(new Color(200, 220, 255));
            rowLabel.setBorder(BorderFactory.createLineBorder(new Color(100, 150, 200), 2));
            matrixPanel.add(rowLabel);

            for (int j = 0; j < n; j++) {
                JLabel cellLabel = new JLabel(String.valueOf(matrix[i][j]), SwingConstants.CENTER);
                cellLabel.setFont(new Font("Consolas", Font.BOLD, 15));
                cellLabel.setOpaque(true);
                cellLabel.setBorder(BorderFactory.createLineBorder(Color.GRAY, 1));

                if (matrix[i][j] > 0) {
                    cellLabel.setBackground(new Color(200, 255, 200));
                    cellLabel.setForeground(new Color(0, 100, 0));
                } else {
                    cellLabel.setBackground(Color.WHITE);
                    cellLabel.setForeground(Color.GRAY);
                }

                matrixPanel.add(cellLabel);
            }
        }

        JScrollPane scrollPane = new JScrollPane(matrixPanel);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(100, 150, 200), 2));

        mainPanel.add(titleLabel, BorderLayout.NORTH);
        mainPanel.add(scrollPane, BorderLayout.CENTER);

        dialog.add(mainPanel);
        dialog.setVisible(true);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        // Draw gradient background
        GradientPaint gradient = new GradientPaint(
                0, 0, new Color(30, 40, 60),
                0, getHeight(), new Color(50, 70, 100)
        );
        g2d.setPaint(gradient);
        g2d.fillRect(0, 0, getWidth(), getHeight());

        drawEdges(g2d);
        drawNodes(g2d);
        drawInfo(g2d);
    }

    private void drawEdges(Graphics2D g2d) {
        g2d.setStroke(new BasicStroke(4, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));

        for (Edge edge : graph.getEdges()) {
            Node src = edge.getSource();
            Node tgt = edge.getTarget();

            Color edgeColor = edge.getColor();
            if (edge.isInPath()) {
                edgeColor = new Color(100, 255, 150);
                g2d.setStroke(new BasicStroke(6, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            } else if (edge.isExploring()) {
                edgeColor = new Color(255, 200, 100);
                g2d.setStroke(new BasicStroke(5, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            } else {
                g2d.setStroke(new BasicStroke(3));
            }

            g2d.setColor(edgeColor);
            g2d.drawLine(src.getX(), src.getY(), tgt.getX(), tgt.getY());

            // Draw weight label with background
            int midX = (src.getX() + tgt.getX()) / 2;
            int midY = (src.getY() + tgt.getY()) / 2;

            String weightStr = String.valueOf(edge.getWeight());
            g2d.setFont(new Font("Arial", Font.BOLD, 16));
            FontMetrics fm = g2d.getFontMetrics();
            int textWidth = fm.stringWidth(weightStr);

            // Draw background circle for weight
            g2d.setColor(new Color(255, 255, 255));
            g2d.fillOval(midX - 18, midY - 18, 36, 36);

            if (edge.isInPath()) {
                g2d.setColor(new Color(100, 255, 150));
            } else {
                g2d.setColor(new Color(200, 220, 255));
            }
            g2d.setStroke(new BasicStroke(2));
            g2d.drawOval(midX - 18, midY - 18, 36, 36);

            // Draw weight text
            g2d.setColor(new Color(40, 60, 100));
            g2d.drawString(weightStr, midX - textWidth / 2, midY + 6);
        }
    }

    private void drawNodes(Graphics2D g2d) {
        for (Node node : graph.getNodes()) {
            int x = node.getX();
            int y = node.getY();
            int r = node.getRadius();

            // Draw glow effect for highlighted nodes
            if (node.isHighlighted() || node.isProcessing()) {
                g2d.setColor(new Color(255, 255, 100, 80));
                g2d.fillOval(x - r - 8, y - r - 8, 2 * r + 16, 2 * r + 16);
            }

            // Draw shadow
            g2d.setColor(new Color(0, 0, 0, 60));
            g2d.fillOval(x - r + 3, y - r + 3, 2 * r, 2 * r);

            // Draw node circle with gradient
            Color nodeColor = node.getColor();
            GradientPaint nodeGradient = new GradientPaint(
                    x, y - r, nodeColor.brighter(),
                    x, y + r, nodeColor
            );
            g2d.setPaint(nodeGradient);
            g2d.fillOval(x - r, y - r, 2 * r, 2 * r);

            // Draw border
            g2d.setColor(Color.WHITE);
            g2d.setStroke(new BasicStroke(3));
            g2d.drawOval(x - r, y - r, 2 * r, 2 * r);

            // Draw label
            g2d.setColor(node.getTextColor());
            g2d.setFont(new Font("Arial", Font.BOLD, 20));
            String label = node.getLabel();
            FontMetrics fm = g2d.getFontMetrics();
            int textX = x - fm.stringWidth(label) / 2;
            int textY = y + fm.getAscent() / 2 - 2;
            g2d.drawString(label, textX, textY);

            // Draw distance for Dijkstra visualization
            if (node.isProcessing() && node.getDistance() != Integer.MAX_VALUE) {
                g2d.setFont(new Font("Arial", Font.BOLD, 12));
                g2d.setColor(new Color(255, 255, 100));
                String distStr = "d=" + node.getDistance();
                int distX = x - fm.stringWidth(distStr) / 2;
                g2d.drawString(distStr, distX, y + r + 20);
            }
        }
    }

    private void drawInfo(Graphics2D g2d) {
        g2d.setColor(new Color(200, 220, 255));
        g2d.setFont(new Font("Arial", Font.BOLD, 15));

        int startY = getHeight() - 100;
        g2d.drawString("📊 Weighted Graph Visualization", 20, startY);
        g2d.drawString("🔵 Nodes: " + graph.getNumNodes(), 20, startY + 25);
        g2d.drawString("➡️ Edges: " + graph.getEdges().size(), 20, startY + 50);

        g2d.setFont(new Font("Arial", Font.PLAIN, 13));
        g2d.setColor(new Color(180, 200, 230));
        g2d.drawString("💡 Double-click to rename node", getWidth() - 280, startY + 25);
        g2d.drawString("🖱️ Drag nodes to rearrange", getWidth() - 280, startY + 50);
    }
}