package org.example;

import javax.sound.sampled.*;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.border.TitledBorder;
import javax.swing.plaf.basic.BasicComboBoxUI;
import java.awt.*;
import java.awt.event.*;
import java.net.URL;
import java.util.*;
import java.util.List;
import javax.swing.Timer;

public class HexMaze extends JPanel {

    // --- KONFIGURASI BOBOT (WEIGHTS) ---
    private final int COST_GRASS = 1;
    private final int COST_MUD = 5;
    private final int COST_WATER = 10;

    // --- TIPE SEL ---
    private final int WALL = 0;
    private final int GRASS = 1;
    private final int MUD = 2;
    private final int WATER = 3;
    private final int SOLUTION = 99;

    // --- VARIABEL GLOBAL ---
    private int rows = 25;
    private int cols = 25;
    private int delayMs = 25;

    // Warna Neon & Terrain
    private final Color COL_BG = new Color(15, 15, 20);
    private final Color COL_WALL = new Color(25, 25, 35);
    private final Color COL_WALL_BORDER = new Color(40, 40, 50);
    private final Color COL_GRASS = new Color(34, 139, 34);
    private final Color COL_MUD = new Color(139, 69, 19);
    private final Color COL_WATER = new Color(25, 25, 112);
    private final Color COL_VISITED = new Color(0, 240, 255, 180);
    private final Color COL_VISITED_BORDER = new Color(150, 255, 255);
    private final Color COL_START = new Color(0, 255, 0);
    private final Color COL_FINISH_RED = new Color(255, 50, 50);
    private final Color COL_PAWN = new Color(255, 215, 0);
    private final Color COL_PAWN_BORDER = new Color(50, 50, 0);

    private float animTime = 0f;
    private Timer renderTimer;

    private int[][] gridType;
    private Point[][] parents;
    private Point startNode;
    private Point endNode;
    private Point pawnPosition = null;

    private JLabel lblStatSteps;
    private JLabel lblStatCost;

    // Rendering Dinamis Hexagon
    private double hexRadius, hexWidth, hexHeight, offsetX, offsetY;

    private Timer searchTimer;
    private Timer pathTimer;
    private boolean isSolving = false;

    public HexMaze() {
        setBackground(COL_BG);
        generateMaze(rows, cols);
        renderTimer = new Timer(16, e -> { animTime += 0.1f; if (animTime > 100) animTime = 0; repaint(); });
        renderTimer.start();
    }

    public void setStatLabels(JLabel steps, JLabel cost) {
        this.lblStatSteps = steps;
        this.lblStatCost = cost;
    }

    private void playWinSound() {
        try {
            URL url = getClass().getResource("/winner-sound.wav");
            if (url != null) {
                AudioInputStream audioIn = AudioSystem.getAudioInputStream(url);
                Clip clip = AudioSystem.getClip(); clip.open(audioIn); clip.start();
            }
        } catch (Exception e) { /* Silent */ }
    }

    // --- LOGIKA HEXAGONAL ---
    private List<Point> getHexNeighbors(int r, int c) {
        List<Point> neighbors = new ArrayList<>();
        int[][] dirs;
        if (r % 2 == 0) dirs = new int[][]{{-1, -1}, {-1, 0}, {0, -1}, {0, 1}, {1, -1}, {1, 0}};
        else dirs = new int[][]{{-1, 0}, {-1, 1}, {0, -1}, {0, 1}, {1, 0}, {1, 1}};
        for (int[] d : dirs) {
            int nr = r + d[0], nc = c + d[1];
            if (isValid(nr, nc)) neighbors.add(new Point(nr, nc));
        }
        return neighbors;
    }

    private boolean isValid(int r, int c) { return r >= 0 && r < rows && c >= 0 && c < cols; }

    // --- GENERATE MAZE ---
    public void generateMaze(int r, int c) {
        resetTimers(); updateStats(0, 0);
        this.rows = r; this.cols = c;
        gridType = new int[rows][cols]; parents = new Point[rows][cols];
        pawnPosition = null;
        for (int i = 0; i < rows; i++) Arrays.fill(gridType[i], WALL);

        startNode = new Point(1, 1); endNode = new Point(rows - 2, cols - 2);
        gridType[startNode.x][startNode.y] = GRASS;

        ArrayList<Point> wallList = new ArrayList<>(getHexNeighbors(startNode.x, startNode.y));
        Random rand = new Random();

        while (!wallList.isEmpty()) {
            int index = rand.nextInt(wallList.size());
            Point wall = wallList.remove(index);
            List<Point> neighbors = getHexNeighbors(wall.x, wall.y);
            int visitedCount = 0;
            for (Point n : neighbors) if (gridType[n.x][n.y] != WALL) visitedCount++;

            if (visitedCount == 1) {
                gridType[wall.x][wall.y] = generateRandomTerrain(rand);
                for (Point n : neighbors) if (gridType[n.x][n.y] == WALL && !wallList.contains(n)) wallList.add(n);
            }
        }
        gridType[startNode.x][startNode.y] = GRASS; gridType[endNode.x][endNode.y] = GRASS;
        addMultiplePaths(rand);
        pawnPosition = new Point(startNode.x, startNode.y);
        repaint();
    }

    private void addMultiplePaths(Random rand) {
        int attempts = (rows * cols) / 10;
        for (int k = 0; k < attempts; k++) {
            int r = rand.nextInt(rows - 2) + 1; int c = rand.nextInt(cols - 2) + 1;
            if (gridType[r][c] == WALL) {
                List<Point> neighbors = getHexNeighbors(r, c);
                int pathNeighbors = 0;
                for (Point n : neighbors) if (gridType[n.x][n.y] != WALL) pathNeighbors++;
                if (pathNeighbors >= 2 && rand.nextDouble() < 0.3) gridType[r][c] = generateRandomTerrain(rand);
            }
        }
    }

    private int generateRandomTerrain(Random rand) {
        double chance = rand.nextDouble();
        if (chance < 0.65) return GRASS; if (chance < 0.85) return MUD; return WATER;
    }

    // --- ALGORITMA PENCARIAN ---
    private class Node implements Comparable<Node> {
        int r, c, g, f; Node parent;
        Node(int r, int c, int g, int f, Node parent) { this.r = r; this.c = c; this.g = g; this.f = f; this.parent = parent; }
        @Override public int compareTo(Node other) { return Integer.compare(this.f, other.f); }
    }

    public void solve(String algorithm) {
        if (isSolving) return;
        resetVisuals(); updateStats(0, 0); isSolving = true;
        parents = new Point[rows][cols];

        final Queue<Node> queue; final Stack<Node> stack;
        final boolean[][] visited = new boolean[rows][cols];
        final int[][] dist = new int[rows][cols];
        for (int[] row : dist) Arrays.fill(row, Integer.MAX_VALUE);

        if (algorithm.equals("DFS")) { stack = new Stack<>(); queue = null; stack.push(new Node(startNode.x, startNode.y, 0, 0, null)); }
        else if (algorithm.equals("BFS")) { queue = new LinkedList<>(); stack = null; queue.add(new Node(startNode.x, startNode.y, 0, 0, null)); }
        else { queue = new PriorityQueue<>(); stack = null; queue.add(new Node(startNode.x, startNode.y, 0, 0, null)); dist[startNode.x][startNode.y] = 0; }

        searchTimer = new Timer(delayMs, e -> {
            boolean isEmpty = (stack != null) ? stack.isEmpty() : queue.isEmpty();
            if (isEmpty) { searchTimer.stop(); isSolving = false; return; }
            Node current = (stack != null) ? stack.pop() : queue.poll();

            if (current.r == endNode.x && current.c == endNode.y) { searchTimer.stop(); reconstructPathAnimated(current); return; }
            if ((algorithm.equals("Dijkstra") || algorithm.equals("A*")) && current.g > dist[current.r][current.c]) return;
            visited[current.r][current.c] = true;

            List<Point> neighbors = getHexNeighbors(current.r, current.c);
            if (algorithm.equals("DFS")) Collections.shuffle(neighbors);

            for (Point n : neighbors) {
                if (gridType[n.x][n.y] != WALL && !visited[n.x][n.y]) {
                    int moveCost = getCost(n.x, n.y); int newG = current.g + moveCost;
                    if (algorithm.equals("BFS") || algorithm.equals("DFS")) {
                        if (parents[n.x][n.y] == null) {
                            parents[n.x][n.y] = new Point(current.r, current.c);
                            Node next = new Node(n.x, n.y, 0, 0, current);
                            if (stack != null) stack.push(next); else queue.add(next);
                            if (algorithm.equals("BFS")) visited[n.x][n.y] = true;
                        }
                    } else {
                        if (newG < dist[n.x][n.y]) {
                            dist[n.x][n.y] = newG;
                            int h = (algorithm.equals("A*")) ? (int)(Math.sqrt(Math.pow(n.x-endNode.x,2)+Math.pow(n.y-endNode.y,2))*COST_GRASS) : 0;
                            parents[n.x][n.y] = new Point(current.r, current.c);
                            queue.add(new Node(n.x, n.y, newG, newG + h, current));
                        }
                    }
                }
            }
        });
        searchTimer.start();
    }

    private int getCost(int r, int c) {
        int t = gridType[r][c]; if (t == GRASS) return COST_GRASS; if (t == MUD) return COST_MUD; if (t == WATER) return COST_WATER; return 1;
    }

    private void reconstructPathAnimated(Node endNode) {
        ArrayList<Node> finalPath = new ArrayList<>();
        Node curr = endNode; int totalPathCost = 0;
        while (curr != null) {
            finalPath.add(curr); totalPathCost += getCost(curr.r, curr.c);
            if(curr.parent == null && !(curr.r == startNode.x && curr.c == startNode.y)) {
                Point p = parents[curr.r][curr.c]; curr = (p != null) ? new Node(p.x, p.y, 0, 0, null) : null;
            } else curr = curr.parent;
        }
        Collections.reverse(finalPath); updateStats(finalPath.size(), totalPathCost);
        final int[] step = {0};
        pathTimer = new Timer(70, e -> {
            if (step[0] < finalPath.size()) {
                Node n = finalPath.get(step[0]); pawnPosition = new Point(n.r, n.c);
                if (n.r != this.endNode.x || n.c != this.endNode.y) gridType[n.r][n.c] = SOLUTION;
                if (n.r == this.endNode.x && n.c == this.endNode.y) playWinSound();
                step[0]++;
            } else { ((Timer) e.getSource()).stop(); isSolving = false; }
        });
        pathTimer.start();
    }

    private void updateStats(int steps, int cost) {
        if (lblStatSteps != null && lblStatCost != null) { lblStatSteps.setText(String.valueOf(steps)); lblStatCost.setText(String.valueOf(cost)); }
    }
    private void resetTimers() { if (searchTimer != null && searchTimer.isRunning()) searchTimer.stop(); if (pathTimer != null && pathTimer.isRunning()) pathTimer.stop(); isSolving = false; }
    private void resetVisuals() { resetTimers(); for(int i=0;i<rows;i++) for(int j=0;j<cols;j++) if(gridType[i][j]==SOLUTION) gridType[i][j]=GRASS; pawnPosition=new Point(startNode.x, startNode.y); repaint(); }

    // --- RENDERING ---
    private Point.Double getHexCenter(int r, int c) {
        double x = offsetX + (c * hexWidth) + ((r % 2 != 0) ? hexWidth / 2.0 : 0);
        double y = offsetY + (r * (hexHeight * 0.75));
        return new Point.Double(x, y);
    }

    private Polygon getHexPolygon(double cx, double cy, double radius) {
        Polygon p = new Polygon();
        for (int i = 0; i < 6; i++) {
            double angle_rad = Math.toRadians(30 + (60 * i));
            p.addPoint((int) (cx + radius * Math.cos(angle_rad)), (int) (cy + radius * Math.sin(angle_rad)));
        }
        return p;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g); Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        int panelW = getWidth(); int panelH = getHeight();
        double maxH = (double) panelH / (rows * 0.75 + 0.25); double maxW = (double) panelW / (cols + 0.5);
        hexWidth = Math.min(maxW, maxH); hexRadius = hexWidth / Math.sqrt(3); hexHeight = 2 * hexRadius;
        offsetX = (panelW - ((cols * hexWidth) + (hexWidth/2))) / 2 + (hexWidth/2);
        offsetY = (panelH - ((rows * (hexHeight * 0.75)) + (hexHeight * 0.25))) / 2 + (hexRadius);

        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                Point.Double center = getHexCenter(r, c); Polygon poly = getHexPolygon(center.x, center.y, hexRadius);
                int type = gridType[r][c];
                if (type == WALL) { g2.setColor(COL_WALL); g2.fillPolygon(poly); g2.setColor(COL_WALL_BORDER); g2.setStroke(new BasicStroke(1f)); g2.drawPolygon(poly); }
                else {
                    if (type == WATER) g2.setColor(COL_WATER); else if (type == MUD) g2.setColor(COL_MUD); else g2.setColor(COL_GRASS);
                    g2.fillPolygon(poly);
                    if (isSolving && parents != null && parents[r][c] != null && type != SOLUTION) {
                        g2.setColor(COL_VISITED); g2.fillPolygon(poly); g2.setColor(COL_VISITED_BORDER); g2.setStroke(new BasicStroke(1.5f)); g2.drawPolygon(poly);
                    }
                    if (type == SOLUTION) {
                        float pulse = (float) (Math.sin(animTime) * 0.5 + 0.5); g2.setColor(new Color(255, 0, 255, (int)(150 + (pulse * 105))));
                        g2.fillPolygon(poly); g2.setColor(new Color(255,255,255,100)); g2.fillPolygon(getHexPolygon(center.x, center.y, hexRadius * 0.5));
                    }
                    g2.setColor(new Color(0,0,0,50)); g2.setStroke(new BasicStroke(1f)); g2.drawPolygon(poly);
                }
            }
        }
        drawSpecialHex(g2, startNode, COL_START, "S"); drawSpecialHex(g2, endNode, COL_FINISH_RED, "E");
        if (pawnPosition != null) {
            Point.Double p = getHexCenter(pawnPosition.x, pawnPosition.y); int bounce = (int)(Math.sin(animTime * 2) * 5); int pawnSize = (int)(hexRadius * 1.2);
            g2.setColor(Color.BLACK); g2.fillOval((int)p.x - pawnSize/2 + 2, (int)p.y - pawnSize/2 + 2 - bounce, pawnSize, pawnSize);
            g2.setColor(COL_PAWN); g2.fillOval((int)p.x - pawnSize/2, (int)p.y - pawnSize/2 - bounce, pawnSize, pawnSize);
            g2.setColor(COL_PAWN_BORDER); g2.setStroke(new BasicStroke(2)); g2.drawOval((int)p.x - pawnSize/2, (int)p.y - pawnSize/2 - bounce, pawnSize, pawnSize);
        }
    }

    private void drawSpecialHex(Graphics2D g2, Point pt, Color c, String text) {
        Point.Double center = getHexCenter(pt.x, pt.y); Polygon poly = getHexPolygon(center.x, center.y, hexRadius * 0.8);
        g2.setColor(c); g2.fillPolygon(poly); g2.setColor(Color.WHITE); g2.setStroke(new BasicStroke(2)); g2.drawPolygon(poly);
        g2.setColor(Color.BLACK); g2.setFont(new Font("Segoe UI", Font.BOLD, (int)(hexRadius)));
        FontMetrics fm = g2.getFontMetrics(); g2.drawString(text, (int)center.x - fm.stringWidth(text)/2, (int)center.y + fm.getAscent()/3);
    }

    // ================= MAIN METHOD & UI REWORKED =================
    public static void main(String[] args) {
        try { UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()); } catch (Exception ignored) {}

        JFrame frame = new JFrame("Hexagonal Maze - Advanced Pathfinder");
        HexMaze game = new HexMaze();

        // --- SIDEBAR SETUP (Dikembalikan ke gaya original dengan penambahan header) ---
        JPanel sidebar = new JPanel();
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        sidebar.setBackground(new Color(20, 20, 30));
        sidebar.setBorder(new EmptyBorder(20, 20, 20, 20));
        sidebar.setPreferredSize(new Dimension(260, 0)); // Sedikit lebih lebar

        // 1. Title
        JLabel titleLbl = new JLabel("GAME SETTINGS");
        titleLbl.setForeground(Color.WHITE);
        titleLbl.setFont(new Font("Segoe UI", Font.BOLD, 18));
        titleLbl.setAlignmentX(Component.CENTER_ALIGNMENT);

        // 2. Level Selector (Combobox style lama)
        // Ukuran disesuaikan untuk Hexagon tapi teks mirip original
        String[] levels = {"Easy (15x15)", "Medium (25x25)", "Hard (35x35)"};
        ModernComboBox comboLevel = new ModernComboBox(levels);
        comboLevel.setMaximumSize(new Dimension(220, 35));
        comboLevel.setSelectedIndex(1); // Default Medium
        comboLevel.addActionListener(e -> {
            int idx = comboLevel.getSelectedIndex();
            if (idx == 0) game.generateMaze(15, 15);
            else if (idx == 1) game.generateMaze(25, 25);
            else game.generateMaze(35, 35);
        });

        // 3. Legend Panel (Style lama dengan border)
        JPanel legendPanel = new JPanel(new GridLayout(3, 1));
        legendPanel.setBackground(new Color(20, 20, 30));
        legendPanel.setBorder(new TitledBorder(new LineBorder(Color.GRAY), "Terrain Cost", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, null, Color.WHITE));
        legendPanel.setMaximumSize(new Dimension(220, 100));
        legendPanel.add(createLegend("Grass (Cost 1)", game.COL_GRASS));
        legendPanel.add(createLegend("Mud (Cost 5)", game.COL_MUD));
        legendPanel.add(createLegend("Water (Cost 10)", game.COL_WATER));

        // 4. Statistics Panel (Style lama dengan border)
        JPanel statsPanel = new JPanel(new GridLayout(2, 2));
        statsPanel.setBackground(new Color(20, 20, 30));
        statsPanel.setBorder(new TitledBorder(new LineBorder(Color.CYAN), "Statistics", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, null, Color.CYAN));
        statsPanel.setMaximumSize(new Dimension(220, 80));
        JLabel lblStepsVal = new JLabel("0"); lblStepsVal.setForeground(Color.WHITE);
        JLabel lblCostVal = new JLabel("0"); lblCostVal.setForeground(Color.GREEN);
        JLabel t1 = new JLabel("Steps:"); t1.setForeground(Color.LIGHT_GRAY);
        JLabel t2 = new JLabel("Cost:"); t2.setForeground(Color.LIGHT_GRAY);
        statsPanel.add(t1); statsPanel.add(lblStepsVal);
        statsPanel.add(t2); statsPanel.add(lblCostVal);
        game.setStatLabels(lblStepsVal, lblCostVal);

        // 5. Buttons & Headers (Bagian baru yang diminta)
        JButton btnGen = new NeonButton("GENERATE MAZE", new Color(100, 100, 100));
        JButton btnBFS = new NeonButton("Solve BFS", new Color(0, 150, 200));
        JButton btnDFS = new NeonButton("Solve DFS", new Color(200, 100, 0));
        JButton btnDijkstra = new NeonButton("Solve Dijkstra", new Color(150, 0, 150));
        JButton btnAStar = new NeonButton("Solve A*", new Color(0, 200, 100));

        btnGen.addActionListener(e -> game.generateMaze(game.rows, game.cols));
        btnBFS.addActionListener(e -> game.solve("BFS"));
        btnDFS.addActionListener(e -> game.solve("DFS"));
        btnDijkstra.addActionListener(e -> game.solve("Dijkstra"));
        btnAStar.addActionListener(e -> game.solve("A*"));

        // --- MENYUSUN SIDEBAR ---
        sidebar.add(titleLbl); sidebar.add(Box.createVerticalStrut(20));
        sidebar.add(comboLevel); sidebar.add(Box.createVerticalStrut(20));
        sidebar.add(legendPanel); sidebar.add(Box.createVerticalStrut(10));
        sidebar.add(statsPanel); sidebar.add(Box.createVerticalStrut(20));
        sidebar.add(btnGen);
        sidebar.add(Box.createVerticalStrut(20));

        // Header Unweighted
        JLabel lblUn = new JLabel("Unweighted Algos:");
        lblUn.setForeground(Color.GRAY); lblUn.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblUn.setAlignmentX(Component.CENTER_ALIGNMENT);
        sidebar.add(lblUn);
        sidebar.add(Box.createVerticalStrut(5));
        sidebar.add(btnBFS); sidebar.add(Box.createVerticalStrut(5));
        sidebar.add(btnDFS);

        // Header Weighted
        sidebar.add(Box.createVerticalStrut(15));
        JLabel lblWei = new JLabel("Weighted Algos:");
        lblWei.setForeground(Color.GRAY); lblWei.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblWei.setAlignmentX(Component.CENTER_ALIGNMENT);
        sidebar.add(lblWei);
        sidebar.add(Box.createVerticalStrut(5));
        sidebar.add(btnDijkstra); sidebar.add(Box.createVerticalStrut(5));
        sidebar.add(btnAStar);

        frame.setLayout(new BorderLayout());
        frame.add(game, BorderLayout.CENTER);
        frame.add(sidebar, BorderLayout.EAST);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(1200, 800);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

    // --- UI HELPERS (Tetap sama) ---
    private static JPanel createLegend(String text, Color col) {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT)); p.setOpaque(false);
        JPanel box = new JPanel(); box.setPreferredSize(new Dimension(15, 15)); box.setBackground(col);
        JLabel lbl = new JLabel(text); lbl.setForeground(Color.LIGHT_GRAY);
        p.add(box); p.add(lbl); return p;
    }

    private static class ModernComboBox extends JComboBox<String> {
        public ModernComboBox(String[] items) {
            super(items); setOpaque(false); setFont(new Font("Segoe UI", Font.BOLD, 13));
            setForeground(Color.WHITE); setBackground(new Color(40, 40, 50));
            setUI(new BasicComboBoxUI() {
                protected JButton createArrowButton() {
                    JButton b = new JButton(); b.setContentAreaFilled(false); b.setBorder(null);
                    b.setIcon(new Icon() {
                        public void paintIcon(Component c, Graphics g, int x, int y) {
                            g.setColor(Color.WHITE); g.fillPolygon(new int[]{x,x+10,x+5}, new int[]{y+4,y+4,y+10}, 3);
                        }
                        public int getIconWidth(){return 10;} public int getIconHeight(){return 10;}
                    });
                    return b;
                }
            });
        }
    }

    private static class NeonButton extends JButton {
        private Color baseColor;
        public NeonButton(String text, Color color) {
            super(text); this.baseColor = color;
            setContentAreaFilled(false); setFocusPainted(false); setBorderPainted(false);
            setForeground(Color.WHITE); setFont(new Font("Segoe UI", Font.BOLD, 12));
            setCursor(new Cursor(Cursor.HAND_CURSOR)); setAlignmentX(Component.CENTER_ALIGNMENT);
            setMaximumSize(new Dimension(220, 35));
            addMouseListener(new MouseAdapter() {
                public void mouseEntered(MouseEvent e) { setForeground(color.brighter()); repaint(); }
                public void mouseExited(MouseEvent e) { setForeground(Color.WHITE); repaint(); }
            });
        }
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(baseColor.darker()); g2.fillRoundRect(0,0,getWidth(),getHeight(),10,10);
            g2.setColor(baseColor); g2.setStroke(new BasicStroke(2f));
            g2.drawRoundRect(1,1,getWidth()-2,getHeight()-2,10,10);
            super.paintComponent(g);
        }
    }
}