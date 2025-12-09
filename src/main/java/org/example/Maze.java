package org.example;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.border.TitledBorder;
import javax.swing.plaf.basic.BasicComboBoxUI;
import javax.swing.plaf.basic.BasicComboPopup;
import javax.swing.plaf.basic.ComboPopup;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import javax.swing.Timer;
import java.util.List;
import java.net.URL;
import javax.sound.sampled.*;

public class Maze extends JPanel {

    // --- KONFIGURASI BOBOT (WEIGHTS) ---
    private final int COST_GRASS = 1;
    private final int COST_MUD = 5;
    private final int COST_WATER = 10;

    // --- TIPE SEL ---
    private final int WALL = 0;
    private final int GRASS = 1;
    private final int MUD = 2;
    private final int WATER = 3;

    // Status Visualisasi
    private final int SOLUTION = 99;

    // --- VARIABEL GLOBAL ---
    private int rows = 21;
    private int cols = 31;
    // [REVISI] Kecepatan diperlambat sedikit agar animasi scan lebih terlihat
    private int delayMs = 25;

    // Warna Neon & Terrain
    private final Color COL_BG = new Color(15, 15, 20);
    private final Color COL_WALL = new Color(40, 40, 55);
    private final Color COL_WALL_SHADOW = new Color(0, 0, 0, 100);

    private final Color COL_GRASS = new Color(34, 139, 34);
    private final Color COL_MUD = new Color(139, 69, 19);
    private final Color COL_WATER = new Color(25, 25, 112);

    // WARNA ANIMASI
    // [REVISI PENTING] Mengubah warna scan menjadi Cyan Terang dengan Opacity tinggi agar kontras
    private final Color COL_VISITED = new Color(0, 240, 255, 180); // Electric Cyan, agak transparan
    // Warna border tipis untuk sel yang sedang discan agar lebih tajam
    private final Color COL_VISITED_BORDER = new Color(150, 255, 255);

    private final Color COL_START = new Color(0, 255, 0);
    private final Color COL_FINISH_RED = new Color(255, 50, 50);

    // Warna Pion
    private final Color COL_PAWN = new Color(255, 215, 0); // Emas
    private final Color COL_PAWN_BORDER = new Color(50, 50, 0);

    // Variabel Animasi Global
    private float animTime = 0f;
    private Timer renderTimer;

    // Struktur Data Grid
    private int[][] gridType;
    private Point[][] parents;
    private Point startNode;
    private Point endNode;

    // Variabel Pion
    private Point pawnPosition = null;

    // UI Statistik
    private JLabel lblStatSteps;
    private JLabel lblStatCost;

    // Rendering Dinamis
    private int cellSize;
    private int offsetX;
    private int offsetY;

    private Timer searchTimer;
    private Timer pathTimer;
    private boolean isSolving = false;

    public Maze() {
        setBackground(COL_BG);
        generateMaze(rows, cols);

        // Timer visual effects (60 FPS roughly)
        renderTimer = new Timer(16, e -> {
            animTime += 0.1f;
            if (animTime > 100) animTime = 0;
            repaint();
        });
        renderTimer.start();
    }

    public void setStatLabels(JLabel steps, JLabel cost) {
        this.lblStatSteps = steps;
        this.lblStatCost = cost;
    }

    // --- AUDIO PLAYER ---
    private void playWinSound() {
        try {
            URL url = getClass().getResource("/winner-sound.wav");
            if (url != null) {
                AudioInputStream audioIn = AudioSystem.getAudioInputStream(url);
                Clip clip = AudioSystem.getClip();
                clip.open(audioIn);
                clip.start();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // --- 1. GENERATE MAZE ---
    public void generateMaze(int r, int c) {
        resetTimers();
        updateStats(0, 0);
        this.rows = r;
        this.cols = c;
        gridType = new int[rows][cols];
        parents = new Point[rows][cols];
        pawnPosition = null;

        startNode = new Point(1, 1);
        endNode = new Point(rows - 2, cols - 2);

        for (int i = 0; i < rows; i++) Arrays.fill(gridType[i], WALL);

        int sr = startNode.x, sc = startNode.y;
        gridType[sr][sc] = GRASS;

        ArrayList<Point> walls = new ArrayList<>();
        addWalls(sr, sc, walls);

        Random rand = new Random();
        while (!walls.isEmpty()) {
            int index = rand.nextInt(walls.size());
            Point wall = walls.remove(index);

            List<Point> neighbors = getNeighbors(wall.x, wall.y, 2);
            List<Point> visitedNeighbors = new ArrayList<>();
            for (Point n : neighbors) {
                if (gridType[n.x][n.y] != WALL) visitedNeighbors.add(n);
            }

            if (visitedNeighbors.size() == 1) {
                int terrain = generateRandomTerrain(rand);
                gridType[wall.x][wall.y] = terrain;
                Point neighbor = visitedNeighbors.get(0);
                gridType[(wall.x + neighbor.x) / 2][(wall.y + neighbor.y) / 2] = terrain;
                addWalls(wall.x, wall.y, walls);
            }
        }
        addMultiplePaths(rand);
        forceConnectivity(startNode);
        forceConnectivity(endNode);

        pawnPosition = new Point(startNode.x, startNode.y);
        repaint();
    }

    private void forceConnectivity(Point p) {
        gridType[p.x][p.y] = GRASS;
        int openCount = 0;
        int[][] dirs = {{-1,0}, {1,0}, {0,-1}, {0,1}};
        for(int[] d : dirs) {
            int nr = p.x + d[0], nc = p.y + d[1];
            if(isValid(nr, nc) && gridType[nr][nc] != WALL) openCount++;
        }
        if(openCount == 0) {
            for(int[] d : dirs) {
                int nr = p.x + d[0], nc = p.y + d[1];
                if(isValid(nr, nc)) {
                    gridType[nr][nc] = GRASS;
                    break;
                }
            }
        }
    }

    private void addMultiplePaths(Random rand) {
        double loopChance = 0.07;
        for (int i = 1; i < rows - 1; i++) {
            for (int j = 1; j < cols - 1; j++) {
                if (gridType[i][j] == WALL) {
                    boolean v = isValid(i - 1, j) && isValid(i + 1, j) && gridType[i - 1][j] != WALL && gridType[i + 1][j] != WALL;
                    boolean h = isValid(i, j - 1) && isValid(i, j + 1) && gridType[i][j - 1] != WALL && gridType[i][j + 1] != WALL;
                    if ((v || h) && rand.nextDouble() < loopChance) gridType[i][j] = generateRandomTerrain(rand);
                }
            }
        }
    }

    private int generateRandomTerrain(Random rand) {
        double chance = rand.nextDouble();
        if (chance < 0.60) return GRASS;
        if (chance < 0.85) return MUD;
        return WATER;
    }

    private void addWalls(int r, int c, ArrayList<Point> walls) {
        int[][] dirs = {{-2, 0}, {2, 0}, {0, -2}, {0, 2}};
        for (int[] d : dirs) {
            int nr = r + d[0], nc = c + d[1];
            if (isValid(nr, nc) && gridType[nr][nc] == WALL) walls.add(new Point(nr, nc));
        }
    }

    // --- 2. ALGORITMA PENCARIAN ---
    private class Node implements Comparable<Node> {
        int r, c, g, f;
        Node parent;
        Node(int r, int c, int g, int f, Node parent) {
            this.r = r; this.c = c; this.g = g; this.f = f; this.parent = parent;
        }
        @Override public int compareTo(Node other) { return Integer.compare(this.f, other.f); }
    }

    public void solve(String algorithm) {
        if (isSolving) return;
        resetVisuals();
        updateStats(0, 0);
        isSolving = true;
        parents = new Point[rows][cols];

        final Queue<Node> queue;
        final Stack<Node> stack;
        final boolean[][] visited = new boolean[rows][cols];
        final int[][] dist = new int[rows][cols];
        for (int[] row : dist) Arrays.fill(row, Integer.MAX_VALUE);

        if (algorithm.equals("DFS")) {
            stack = new Stack<>(); queue = null;
            stack.push(new Node(startNode.x, startNode.y, 0, 0, null));
        } else if (algorithm.equals("BFS")) {
            queue = new LinkedList<>(); stack = null;
            queue.add(new Node(startNode.x, startNode.y, 0, 0, null));
        } else {
            queue = new PriorityQueue<>(); stack = null;
            queue.add(new Node(startNode.x, startNode.y, 0, 0, null));
            dist[startNode.x][startNode.y] = 0;
        }

        // Timer menggunakan delayMs yang sudah diperlambat
        searchTimer = new Timer(delayMs, e -> {
            boolean isEmpty = (stack != null) ? stack.isEmpty() : queue.isEmpty();
            if (isEmpty) { searchTimer.stop(); isSolving = false; return; }

            Node current = (stack != null) ? stack.pop() : queue.poll();

            if (current.r == endNode.x && current.c == endNode.y) {
                searchTimer.stop();
                reconstructPathAnimated(current);
                return;
            }

            if ((algorithm.equals("Dijkstra") || algorithm.equals("A*")) && current.g > dist[current.r][current.c]) return;

            visited[current.r][current.c] = true;
            int[][] dirs = {{-1, 0}, {1, 0}, {0, -1}, {0, 1}};
            if (algorithm.equals("DFS")) Collections.shuffle(Arrays.asList(dirs));

            for (int[] d : dirs) {
                int nr = current.r + d[0], nc = current.c + d[1];
                if (isValid(nr, nc) && gridType[nr][nc] != WALL && !visited[nr][nc]) {
                    int moveCost = getCost(nr, nc);
                    int newG = current.g + moveCost;

                    if (algorithm.equals("BFS") || algorithm.equals("DFS")) {
                        if (parents[nr][nc] == null) {
                            parents[nr][nc] = new Point(current.r, current.c);
                            Node next = new Node(nr, nc, 0, 0, current);
                            if (stack != null) stack.push(next); else queue.add(next);
                            if (algorithm.equals("BFS")) visited[nr][nc] = true;
                        }
                    } else {
                        if (newG < dist[nr][nc]) {
                            dist[nr][nc] = newG;
                            int h = (algorithm.equals("A*")) ? (Math.abs(nr - endNode.x) + Math.abs(nc - endNode.y)) : 0;
                            parents[nr][nc] = new Point(current.r, current.c);
                            queue.add(new Node(nr, nc, newG, newG + h, current));
                        }
                    }
                }
            }
            // Repaint dihandle oleh renderTimer global
        });
        searchTimer.start();
    }

    private int getCost(int r, int c) {
        int t = gridType[r][c];
        if (t == GRASS) return COST_GRASS; if (t == MUD) return COST_MUD; if (t == WATER) return COST_WATER;
        return 1;
    }

    // --- 3. ANIMASI JALUR AKHIR & PION ---
    private void reconstructPathAnimated(Node endNode) {
        ArrayList<Node> finalPath = new ArrayList<>();
        Node curr = endNode;
        int totalPathCost = 0;

        while (curr != null) {
            finalPath.add(curr);
            totalPathCost += getCost(curr.r, curr.c);
            if(curr.parent == null && !(curr.r == startNode.x && curr.c == startNode.y)) {
                Point p = parents[curr.r][curr.c];
                curr = (p != null) ? new Node(p.x, p.y, 0, 0, null) : null;
            } else {
                curr = curr.parent;
            }
        }
        Collections.reverse(finalPath);
        updateStats(finalPath.size(), totalPathCost);

        final int[] step = {0};
        // [REVISI] Kecepatan pion juga disesuaikan agar selaras (70ms)
        pathTimer = new Timer(70, e -> {
            if (step[0] < finalPath.size()) {
                Node n = finalPath.get(step[0]);
                pawnPosition = new Point(n.r, n.c);
                if (n.r != this.endNode.x || n.c != this.endNode.y) {
                    gridType[n.r][n.c] = SOLUTION;
                }
                if (n.r == this.endNode.x && n.c == this.endNode.y) {
                    playWinSound();
                }
                step[0]++;
            } else {
                ((Timer) e.getSource()).stop();
                isSolving = false;
            }
        });
        pathTimer.start();
    }

    private void updateStats(int steps, int cost) {
        if (lblStatSteps != null && lblStatCost != null) {
            lblStatSteps.setText(String.valueOf(steps));
            lblStatCost.setText(String.valueOf(cost));
        }
    }

    private void resetTimers() {
        if (searchTimer != null && searchTimer.isRunning()) searchTimer.stop();
        if (pathTimer != null && pathTimer.isRunning()) pathTimer.stop();
        isSolving = false;
    }

    private void resetVisuals() {
        resetTimers();
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                if (gridType[i][j] == SOLUTION) gridType[i][j] = GRASS;
            }
        }
        pawnPosition = new Point(startNode.x, startNode.y);
        repaint();
    }

    private boolean isValid(int r, int c) { return r >= 0 && r < rows && c >= 0 && c < cols; }

    private List<Point> getNeighbors(int r, int c, int step) {
        List<Point> list = new ArrayList<>();
        int[][] dirs = {{-step, 0}, {step, 0}, {0, -step}, {0, step}};
        for (int[] d : dirs) {
            int nr = r + d[0], nc = c + d[1];
            if (isValid(nr, nc)) list.add(new Point(nr, nc));
        }
        return list;
    }

    // --- 4. RENDERING VISUAL (KONTRASTING TINGGI) ---
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int panelW = getWidth(); int panelH = getHeight();
        int cellW = panelW / cols; int cellH = panelH / rows;
        cellSize = Math.max(2, Math.min(cellW, cellH));
        offsetX = (panelW - (cellSize * cols)) / 2;
        offsetY = (panelH - (cellSize * rows)) / 2;

        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                int x = offsetX + (j * cellSize); int y = offsetY + (i * cellSize);
                int type = gridType[i][j];

                if (type == WALL) {
                    // Shadow
                    g2.setColor(COL_WALL_SHADOW);
                    g2.fillRect(x + 3, y + 3, cellSize, cellSize);
                    // Wall
                    g2.setColor(COL_WALL);
                    g2.fillRect(x, y, cellSize, cellSize);
                    // Highlight border
                    g2.setColor(new Color(60, 60, 80));
                    g2.drawRect(x, y, cellSize-1, cellSize-1);
                } else {
                    // Base Terrain
                    if (type == WATER) g2.setColor(COL_WATER);
                    else if (type == MUD) g2.setColor(COL_MUD);
                    else g2.setColor(COL_GRASS);
                    g2.fillRect(x, y, cellSize, cellSize);

                    // [REVISI VISUAL SCANNING]
                    // Jika sedang solving dan node ini sudah dikunjungi (ada di parents)
                    if (isSolving && parents != null && parents[i][j] != null && type != SOLUTION) {
                        // 1. Fill dengan warna Cyan terang yang lebih solid
                        g2.setColor(COL_VISITED);
                        g2.fillRect(x, y, cellSize, cellSize);

                        // 2. Tambahkan border tipis terang agar setiap sel terlihat tajam
                        g2.setColor(COL_VISITED_BORDER);
                        g2.setStroke(new BasicStroke(1f));
                        g2.drawRect(x, y, cellSize - 1, cellSize - 1);
                    }

                    // Glowing Path Effect
                    if (type == SOLUTION) {
                        float pulse = (float) (Math.sin(animTime) * 0.5 + 0.5);
                        int alpha = (int)(150 + (pulse * 105));
                        g2.setColor(new Color(255, 0, 255, alpha));
                        g2.fillRect(x, y, cellSize, cellSize);
                        g2.setColor(Color.WHITE);
                        g2.fillRect(x + cellSize/3, y + cellSize/3, cellSize/3, cellSize/3);
                    }
                }
            }
        }

        drawStartNode(g2, startNode.x, startNode.y);
        drawFinishNode(g2, endNode.x, endNode.y);

        if (pawnPosition != null) {
            drawChessPawn(g2, pawnPosition.x, pawnPosition.y);
        }
    }

    private void drawChessPawn(Graphics2D g2, int r, int c) {
        int x = offsetX + (c * cellSize);
        int y = offsetY + (r * cellSize);

        // Animasi Bobbing (Naik Turun)
        int bounce = (int)(Math.sin(animTime * 2) * 3);

        int padding = cellSize / 5;
        int pw = cellSize - (padding * 2);
        int ph = cellSize - (padding * 2);
        int px = x + padding;
        int py = y + padding - bounce;

        // Bayangan Pion
        g2.setColor(new Color(0,0,0,100));
        g2.fillOval(px + 2, y + cellSize - padding - 5, pw, 5);

        // Badan Pion Emas
        g2.setColor(COL_PAWN);
        int headSize = (int)(pw * 0.6);
        g2.fillOval(px + (pw - headSize)/2, py, headSize, headSize);

        Polygon body = new Polygon();
        body.addPoint(px + pw/2, py + headSize/2);
        body.addPoint(px + pw, py + ph);
        body.addPoint(px, py + ph);
        g2.fillPolygon(body);

        // Border Pion
        g2.setColor(COL_PAWN_BORDER);
        g2.setStroke(new BasicStroke(2));
        g2.drawOval(px + (pw - headSize)/2, py, headSize, headSize);
        g2.drawPolygon(body);

        // Kilau
        g2.setColor(Color.WHITE);
        g2.fillOval(px + (pw - headSize)/2 + 3, py + 3, headSize/4, headSize/4);
    }

    private void drawStartNode(Graphics2D g2, int r, int c) {
        int x = offsetX + (c * cellSize); int y = offsetY + (r * cellSize);
        g2.setColor(COL_START);
        int pulse = (int)(Math.sin(animTime * 2) * 5);
        g2.fillRoundRect(x + 2 - pulse/2, y + 2 - pulse/2, cellSize - 4 + pulse, cellSize - 4 + pulse, 8, 8);
    }

    private void drawFinishNode(Graphics2D g2, int r, int c) {
        int x = offsetX + (c * cellSize); int y = offsetY + (r * cellSize);
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
        int gridSize = 3;
        int miniCellSize = Math.max(1, cellSize / gridSize);
        for (int i = 0; i < gridSize; i++) {
            for (int j = 0; j < gridSize; j++) {
                if ((i + j) % 2 == 0) g2.setColor(COL_FINISH_RED); else g2.setColor(Color.WHITE);
                g2.fillRect(x + (j * miniCellSize), y + (i * miniCellSize), miniCellSize, miniCellSize);
            }
        }
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setColor(COL_FINISH_RED); g2.setStroke(new BasicStroke(2f));
        g2.drawRect(x, y, cellSize, cellSize);
    }

    // -----------------------------------------------------------
    // MAIN METHOD & UI HELPERS
    // -----------------------------------------------------------

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {}

        JFrame frame = new JFrame("Advanced Maze - High Contrast Scan");
        Maze game = new Maze();

        // --- SIDEBAR SETUP ---
        JPanel sidebar = new JPanel();
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        sidebar.setBackground(new Color(20, 20, 30));
        sidebar.setBorder(new EmptyBorder(20, 20, 20, 20));
        sidebar.setPreferredSize(new Dimension(260, 0));

        // Title
        JLabel titleLbl = new JLabel("GAME SETTINGS");
        titleLbl.setForeground(Color.WHITE);
        titleLbl.setFont(new Font("Segoe UI", Font.BOLD, 18));
        titleLbl.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Level Selector
        String[] levels = {"Easy (21x31)", "Medium (41x51)", "Hard (61x81)"};
        ModernComboBox comboLevel = new ModernComboBox(levels);
        comboLevel.setMaximumSize(new Dimension(220, 35));
        comboLevel.addActionListener(e -> {
            int idx = comboLevel.getSelectedIndex();
            if (idx == 0) game.generateMaze(21, 31);
            else if (idx == 1) game.generateMaze(41, 51);
            else game.generateMaze(61, 81);
        });

        // Legend
        JPanel legendPanel = new JPanel(new GridLayout(3, 1));
        legendPanel.setBackground(new Color(20, 20, 30));
        legendPanel.setBorder(new TitledBorder(new LineBorder(Color.GRAY), "Terrain Cost", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, null, Color.WHITE));
        legendPanel.setMaximumSize(new Dimension(220, 100));
        legendPanel.add(createLegend("Grass (Cost 1)", game.COL_GRASS));
        legendPanel.add(createLegend("Mud (Cost 5)", game.COL_MUD));
        legendPanel.add(createLegend("Water (Cost 10)", game.COL_WATER));

        // Statistics
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

        // Buttons
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

        // Add to Sidebar
        sidebar.add(titleLbl); sidebar.add(Box.createVerticalStrut(20));
        sidebar.add(comboLevel); sidebar.add(Box.createVerticalStrut(20));
        sidebar.add(legendPanel); sidebar.add(Box.createVerticalStrut(10));
        sidebar.add(statsPanel); sidebar.add(Box.createVerticalStrut(20));
        sidebar.add(btnGen);

        sidebar.add(Box.createVerticalStrut(20));
        JLabel lblUn = new JLabel("Unweighted Algos:");
        lblUn.setForeground(Color.GRAY); lblUn.setAlignmentX(Component.CENTER_ALIGNMENT);
        sidebar.add(lblUn);
        sidebar.add(Box.createVerticalStrut(5));
        sidebar.add(btnBFS); sidebar.add(Box.createVerticalStrut(5));
        sidebar.add(btnDFS);

        sidebar.add(Box.createVerticalStrut(15));
        JLabel lblWei = new JLabel("Weighted Algos:");
        lblWei.setForeground(Color.GRAY); lblWei.setAlignmentX(Component.CENTER_ALIGNMENT);
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
        frame.setExtendedState(JFrame.MAXIMIZED_BOTH);
        frame.setVisible(true);
    }

    // --- HELPER UI CLASSES ---
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