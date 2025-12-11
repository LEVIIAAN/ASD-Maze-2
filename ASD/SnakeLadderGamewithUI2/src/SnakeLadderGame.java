import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.sound.sampled.*;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.util.*;
import java.util.List;

// ============================================================================
// 1. KELAS SOUND MANAGER
// ============================================================================
class SoundManager {
    private Clip bgmClip;

    public void playSfx(String filename) {
        new Thread(() -> {
            try {
                File soundFile = new File(filename);
                if (soundFile.exists()) {
                    AudioInputStream audioIn = AudioSystem.getAudioInputStream(soundFile);
                    Clip clip = AudioSystem.getClip();
                    clip.open(audioIn);
                    clip.start();
                }
            } catch (Exception e) { /* Silent Fail */ }
        }).start();
    }

    public void playBgm(String filename) {
        new Thread(() -> {
            try {
                File soundFile = new File(filename);
                if (soundFile.exists()) {
                    AudioInputStream audioIn = AudioSystem.getAudioInputStream(soundFile);
                    bgmClip = AudioSystem.getClip();
                    bgmClip.open(audioIn);
                    bgmClip.loop(Clip.LOOP_CONTINUOUSLY);
                    bgmClip.start();
                }
            } catch (Exception e) { /* Silent Fail */ }
        }).start();
    }

    public void stopBgm() {
        if (bgmClip != null && bgmClip.isRunning()) bgmClip.stop();
    }
}

// ============================================================================
// 2. KELAS PLAYER
// ============================================================================
class Player {
    private String name;
    private int position;
    private Color color;
    private int score;

    public Player(String name, Color color) {
        this.name = name;
        this.position = 1;
        this.color = color;
        this.score = 0;
    }

    public String getName() { return name; }
    public int getPosition() { return position; }
    public Color getColor() { return color; }
    public int getScore() { return score; }

    public void setPosition(int position) { this.position = Math.max(1, position); }
    public void addScore(int points) { this.score += points; }

    public boolean hasWon(int targetPosition) { return position >= targetPosition; }
}

// ============================================================================
// 3. KELAS LINK
// ============================================================================
class Link {
    private int from;
    private int to;

    public Link(int from, int to) {
        this.from = from;
        this.to = to;
    }
    public int getFrom() { return from; }
    public int getTo() { return to; }
}

// ============================================================================
// 4. KELAS GAMEBOARD
// ============================================================================
class GameBoard {
    private int totalSquares;
    private Map<Integer, List<Integer>> adjacencyList;
    private Map<Integer, Link> links;
    private Set<Integer> starPositions;
    private Set<Integer> primePositions;
    private Map<Integer, Integer> pointNodes;

    public GameBoard(int rows, int cols) {
        this.totalSquares = rows * cols;
        this.adjacencyList = new HashMap<>();
        this.links = new HashMap<>();
        this.starPositions = new HashSet<>();
        this.primePositions = new HashSet<>();
        this.pointNodes = new HashMap<>();

        buildGraph();
        generateRandomLinks();
        generateStarPositions();
        generatePrimePositions();
        generateRandomPoints();
    }

    private void buildGraph() {
        for (int i = 1; i <= totalSquares; i++) {
            adjacencyList.put(i, new ArrayList<>());
            if (i < totalSquares) adjacencyList.get(i).add(i + 1);
        }
    }

    private void generateRandomLinks() {
        Random rand = new Random();
        int linksCreated = 0;
        while (linksCreated < 5) {
            int from = rand.nextInt(totalSquares - 20) + 2;
            int jump = rand.nextInt(11) + 5;
            int to = from + jump;
            if (to < totalSquares && !links.containsKey(from) && !links.containsKey(to)) {
                links.put(from, new Link(from, to));
                linksCreated++;
            }
        }
    }

    private void generateStarPositions() {
        for (int i = 5; i < totalSquares; i += 5) starPositions.add(i);
    }

    private void generatePrimePositions() {
        for (int i = 2; i <= totalSquares; i++) if (isPrime(i)) primePositions.add(i);
    }

    private void generateRandomPoints() {
        Random rand = new Random();
        int[] possiblePoints = {10, 20, 50, 100};
        int pointsCreated = 0;

        while (pointsCreated < 10) {
            int pos = rand.nextInt(totalSquares - 2) + 2;
            if (!links.containsKey(pos) && !starPositions.contains(pos) && !pointNodes.containsKey(pos)) {
                int val = possiblePoints[rand.nextInt(possiblePoints.length)];
                pointNodes.put(pos, val);
                pointsCreated++;
            }
        }
    }

    public int collectPoint(int pos) {
        if (pointNodes.containsKey(pos)) {
            return pointNodes.remove(pos);
        }
        return 0;
    }

    private boolean isPrime(int n) {
        if (n < 2) return false;
        if (n == 2) return true;
        if (n % 2 == 0) return false;
        for (int i = 3; i * i <= n; i += 2) if (n % i == 0) return false;
        return true;
    }

    public List<Integer> findShortestPathDijkstra(int start, int end) {
        PriorityQueue<int[]> pq = new PriorityQueue<>(Comparator.comparingInt(a -> a[1]));
        Map<Integer, Integer> dist = new HashMap<>();
        Map<Integer, Integer> parent = new HashMap<>();
        for (int i = 1; i <= totalSquares; i++) dist.put(i, Integer.MAX_VALUE);
        dist.put(start, 0);
        pq.offer(new int[]{start, 0});
        parent.put(start, null);

        while (!pq.isEmpty()) {
            int[] current = pq.poll();
            int u = current[0];
            int d = current[1];
            if (d > dist.get(u)) continue;
            if (u == end) return reconstructPath(parent, start, end);

            for (int step = 1; step <= 6; step++) {
                int v = u + step;
                if (v > totalSquares) continue;
                if (links.containsKey(v)) v = links.get(v).getTo();
                if (dist.get(u) + 1 < dist.get(v)) {
                    dist.put(v, dist.get(u) + 1);
                    parent.put(v, u);
                    pq.offer(new int[]{v, dist.get(v)});
                }
            }
        }
        return new ArrayList<>();
    }

    private List<Integer> reconstructPath(Map<Integer, Integer> parent, int start, int end) {
        List<Integer> path = new ArrayList<>();
        Integer current = end;
        while (current != null) {
            path.add(0, current);
            current = parent.get(current);
        }
        return path;
    }

    public int getTotalSquares() { return totalSquares; }
    public boolean isValidPosition(int position) { return position >= 1 && position <= totalSquares; }
    public Map<Integer, Link> getLinks() { return links; }
    public boolean hasStar(int position) { return starPositions.contains(position); }
    public boolean isPrimePosition(int position) { return primePositions.contains(position); }
    public Map<Integer, Integer> getPointNodes() { return pointNodes; }
}

// ============================================================================
// 5. KELAS DICE
// ============================================================================
class Dice {
    private Random random = new Random();
    public int rollMainDice() { return random.nextInt(6) + 1; }
    public int rollModifierDice() { return (Math.random() < 0.80) ? 1 : -1; }
}

// ============================================================================
// 6. UI COMPONENTS
// ============================================================================
class DiceFacePanel extends JPanel {
    private int value = 1;
    public void setValue(int value) { this.value = value; repaint(); }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int size = Math.min(getWidth(), getHeight()) - 20;
        int x = (getWidth() - size) / 2;
        int y = (getHeight() - size) / 2;

        g2.setColor(Color.WHITE);
        g2.fillRoundRect(x, y, size, size, 25, 25);
        g2.setColor(new Color(65, 105, 225));
        g2.setStroke(new BasicStroke(4));
        g2.drawRoundRect(x, y, size, size, 25, 25);
        drawPips(g2, x, y, size);
    }

    private void drawPips(Graphics2D g2, int x, int y, int size) {
        int r = size / 6;
        int cx = x + size/2, cy = y + size/2;
        int l = x + size/4, rX = x + 3*size/4;
        int t = y + size/4, b = y + 3*size/4;
        g2.setColor(new Color(25, 25, 112));
        if(value%2!=0) fillPip(g2, cx, cy, r);
        if(value>1) { fillPip(g2, l, t, r); fillPip(g2, rX, b, r); }
        if(value>3) { fillPip(g2, l, b, r); fillPip(g2, rX, t, r); }
        if(value==6) { fillPip(g2, l, cy, r); fillPip(g2, rX, cy, r); }
    }
    private void fillPip(Graphics2D g, int x, int y, int r) { g.fillOval(x-r/2, y-r/2, r, r); }
}

class BoardPanel extends JPanel {
    private GameBoard board;
    private Stack<Player> players;
    private List<Integer> shortestPath;
    private static final int CELL_SIZE = 75;
    private static final int BOARD_SIZE = 8;

    public BoardPanel(GameBoard board, Stack<Player> players) {
        this.board = board;
        this.players = players;
        this.shortestPath = new ArrayList<>();
        setPreferredSize(new Dimension(BOARD_SIZE * CELL_SIZE + 40, BOARD_SIZE * CELL_SIZE + 40));
        setBackground(new Color(230, 230, 250));
    }

    public void setShortestPath(List<Integer> path) { this.shortestPath = path; repaint(); }
    public void clearShortestPath() { this.shortestPath.clear(); repaint(); }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int offsetX = 20, offsetY = 20;

        for (int row = 0; row < BOARD_SIZE; row++) {
            for (int col = 0; col < BOARD_SIZE; col++) {
                int cellNumber = getCellNumber(row, col);
                int x = offsetX + col * CELL_SIZE;
                int y = offsetY + row * CELL_SIZE;

                Color cellColor = ((row+col)%2 == 0) ? Color.WHITE : new Color(240, 248, 255);
                if (cellNumber == 64) cellColor = new Color(255, 215, 0);
                g2d.setColor(cellColor);
                g2d.fillRoundRect(x, y, CELL_SIZE, CELL_SIZE, 15, 15);
                g2d.setColor(new Color(176, 196, 222));
                g2d.setStroke(new BasicStroke(2));
                g2d.drawRoundRect(x, y, CELL_SIZE, CELL_SIZE, 15, 15);

                if (shortestPath.contains(cellNumber)) {
                    g2d.setColor(new Color(255, 165, 0, 100));
                    g2d.fillRoundRect(x + 5, y + 5, CELL_SIZE - 10, CELL_SIZE - 10, 10, 10);
                    g2d.setColor(new Color(255, 69, 0));
                    g2d.setStroke(new BasicStroke(3));
                    g2d.drawRoundRect(x + 5, y + 5, CELL_SIZE - 10, CELL_SIZE - 10, 10, 10);
                }

                drawCellNumber(g2d, cellNumber, x, y);

                if (board.hasStar(cellNumber)) drawStar(g2d, x, y);
                if (board.isPrimePosition(cellNumber)) drawPrimeIndicator(g2d, x, y);

                if (board.getPointNodes().containsKey(cellNumber)) {
                    drawPoint(g2d, x, y, board.getPointNodes().get(cellNumber));
                }

                drawPlayersAtPosition(g2d, cellNumber, x, y);
            }
        }
        drawLinks(g2d, offsetX, offsetY);
    }

    private int getCellNumber(int row, int col) {
        if (row % 2 == 0) return (BOARD_SIZE - row) * BOARD_SIZE - col;
        else return (BOARD_SIZE - row - 1) * BOARD_SIZE + col + 1;
    }

    // FIXED: Convert int to String
    private void drawCellNumber(Graphics2D g2d, int cellNumber, int x, int y) {
        g2d.setColor(new Color(100, 100, 100));
        g2d.setFont(new Font("Arial", Font.BOLD, 14));
        g2d.drawString(String.valueOf(cellNumber), x + 8, y + 20);
    }

    private void drawStar(Graphics2D g2d, int x, int y) {
        g2d.setColor(new Color(255, 215, 0));
        g2d.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 20));
        g2d.drawString("⭐", x + 5, y + 45);
    }

    private void drawPrimeIndicator(Graphics2D g2d, int x, int y) {
        g2d.setColor(new Color(65, 105, 225));
        g2d.fillOval(x + 5, y + CELL_SIZE - 22, 16, 16);
        g2d.setColor(Color.WHITE);
        g2d.setFont(new Font("Arial", Font.BOLD, 10));
        g2d.drawString("P", x + 9, y + CELL_SIZE - 10);
    }

    // FIXED: Convert int to String
    private void drawPoint(Graphics2D g2d, int x, int y, int value) {
        g2d.setColor(new Color(218, 165, 32));
        g2d.fillOval(x + CELL_SIZE - 35, y + 5, 30, 30);
        g2d.setColor(new Color(255, 215, 0));
        g2d.fillOval(x + CELL_SIZE - 33, y + 7, 26, 26);

        g2d.setColor(Color.BLACK);
        g2d.setFont(new Font("Arial", Font.BOLD, 10));
        String valStr = String.valueOf(value);
        int strW = g2d.getFontMetrics().stringWidth(valStr);
        g2d.drawString(valStr, x + CELL_SIZE - 20 - (strW/2), y + 24);
    }

    private void drawPlayersAtPosition(Graphics2D g2d, int cellNumber, int cellX, int cellY) {
        List<Player> here = new ArrayList<>();
        for (Player p : players) if (p.getPosition() == cellNumber) here.add(p);

        if (!here.isEmpty()) {
            int size = 28, sp = 4;
            int startX = cellX + (CELL_SIZE - (here.size() * size + (here.size()-1)*sp)) / 2;
            int startY = cellY + CELL_SIZE - size - 8;

            for (int i = 0; i < here.size(); i++) {
                Player p = here.get(i);
                int mx = startX + i * (size + sp);
                g2d.setColor(new Color(0,0,0,60));
                g2d.fillOval(mx+2, startY+2, size, size);
                g2d.setColor(p.getColor());
                g2d.fillOval(mx, startY, size, size);
                g2d.setColor(Color.WHITE);
                g2d.setStroke(new BasicStroke(2));
                g2d.drawOval(mx, startY, size, size);
                g2d.setFont(new Font("Arial", Font.BOLD, 12));
                String init = String.valueOf(p.getName().charAt(p.getName().length()-1));
                int textW = g2d.getFontMetrics().stringWidth(init);
                g2d.drawString(init, mx + (size - textW)/2, startY + (size + 10)/2);
            }
        }
    }

    private void drawLinks(Graphics2D g2d, int ox, int oy) {
        for (Link link : board.getLinks().values()) {
            Point p1 = getCellCenter(link.getFrom(), ox, oy);
            Point p2 = getCellCenter(link.getTo(), ox, oy);
            g2d.setColor(new Color(34, 139, 34, 200));
            g2d.setStroke(new BasicStroke(5, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g2d.drawLine(p1.x, p1.y, p2.x, p2.y);
            g2d.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 20));
            g2d.drawString("🪜", (p1.x+p2.x)/2, (p1.y+p2.y)/2);
        }
    }

    private Point getCellCenter(int cellNumber, int ox, int oy) {
        for (int r = 0; r < BOARD_SIZE; r++) {
            for (int c = 0; c < BOARD_SIZE; c++) {
                if (getCellNumber(r, c) == cellNumber) return new Point(ox + c * CELL_SIZE + CELL_SIZE/2, oy + r * CELL_SIZE + CELL_SIZE/2);
            }
        }
        return new Point(ox, oy);
    }
}

// ============================================================================
// 7. PANEL INFO PEMAIN (Updated with Score)
// ============================================================================
class PlayerInfoPanel extends JPanel {
    private Stack<Player> players;
    private Player currentPlayer;

    public PlayerInfoPanel(Stack<Player> players) {
        this.players = players;
        setPreferredSize(new Dimension(300, 0));
        setBackground(new Color(65, 105, 225));
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
    }

    public void setCurrentPlayer(Player p) { this.currentPlayer = p; repaint(); }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int y = 10;
        g2.setColor(Color.WHITE);
        g2.setFont(new Font("Arial", Font.BOLD, 22));
        g2.drawString("🏆 PLAYER STACK", 10, y);
        y += 40;

        List<Player> list = new ArrayList<>(players);
        Collections.reverse(list);

        for (Player p : list) {
            boolean active = (p == currentPlayer);
            g2.setColor(active ? new Color(255, 255, 255) : new Color(100, 149, 237));
            g2.fillRoundRect(5, y-35, 270, 70, 20, 20);

            g2.setColor(p.getColor());
            g2.fillOval(20, y-20, 40, 40);

            g2.setColor(active ? new Color(25, 25, 112) : Color.WHITE);
            g2.setFont(new Font("Arial", Font.BOLD, 18));
            g2.drawString(p.getName(), 75, y+5);

            // FIXED: Convert int score to String using implicit concatenation
            g2.setFont(new Font("Arial", Font.PLAIN, 14));
            g2.drawString("Pos: " + p.getPosition() + " | Score: " + p.getScore(), 75, y+25);

            if(active) {
                g2.setColor(new Color(50, 205, 50));
                g2.setFont(new Font("Arial", Font.BOLD, 12));
                g2.drawString("ACTIVE TURN", 180, y+5);
            }
            y += 85;
        }
    }
}

// ============================================================================
// 8. PANEL KONTROL GAME
// ============================================================================
class ControlPanel extends JPanel {
    private JButton rollButton;
    private JLabel dice2Label, messageLabel;
    private JTextArea pathInfoArea;
    private DiceFacePanel mainDiceFace;

    public ControlPanel(ActionListener rollAction) {
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setBackground(new Color(240, 248, 255));
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JLabel diceTitle = new JLabel("DICE CONTROL");
        diceTitle.setFont(new Font("Arial", Font.BOLD, 20));
        diceTitle.setForeground(new Color(65, 105, 225));
        diceTitle.setAlignmentX(CENTER_ALIGNMENT);
        add(diceTitle);
        add(Box.createRigidArea(new Dimension(0, 15)));

        JPanel diceDisplay = new JPanel(new GridLayout(1, 2, 20, 0));
        diceDisplay.setOpaque(false);
        diceDisplay.setMaximumSize(new Dimension(280, 140));

        JPanel p1 = new JPanel(new BorderLayout());
        p1.setBackground(Color.WHITE);
        p1.setBorder(new EmptyBorder(10,10,10,10));
        mainDiceFace = new DiceFacePanel();
        mainDiceFace.setPreferredSize(new Dimension(100, 100));
        p1.add(mainDiceFace, BorderLayout.CENTER);
        diceDisplay.add(p1);

        JPanel p2 = new JPanel(new BorderLayout());
        p2.setBackground(Color.WHITE);
        p2.setBorder(new EmptyBorder(10,10,10,10));
        dice2Label = new JLabel("?", SwingConstants.CENTER);
        dice2Label.setFont(new Font("Arial", Font.BOLD, 36));
        dice2Label.setForeground(Color.GRAY);
        p2.add(dice2Label, BorderLayout.CENTER);
        diceDisplay.add(p2);

        add(diceDisplay);
        add(Box.createRigidArea(new Dimension(0, 25)));

        rollButton = new JButton("ROLL DICE");
        rollButton.setFont(new Font("Arial", Font.BOLD, 18));
        rollButton.setBackground(new Color(65, 105, 225));
        rollButton.setForeground(Color.WHITE);
        rollButton.setFocusPainted(false);
        rollButton.setAlignmentX(CENTER_ALIGNMENT);
        rollButton.setMaximumSize(new Dimension(280, 60));
        rollButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        rollButton.addActionListener(rollAction);
        add(rollButton);
        add(Box.createRigidArea(new Dimension(0, 20)));

        messageLabel = new JLabel("Ready?", SwingConstants.CENTER);
        messageLabel.setFont(new Font("Arial", Font.BOLD, 14));
        messageLabel.setAlignmentX(CENTER_ALIGNMENT);
        messageLabel.setPreferredSize(new Dimension(260, 60));
        messageLabel.setMaximumSize(new Dimension(260, 60));
        add(messageLabel);

        JLabel pathTitle = new JLabel("Dijkstra Path Suggestion");
        pathTitle.setFont(new Font("Arial", Font.BOLD, 14));
        pathTitle.setForeground(new Color(65, 105, 225));
        pathTitle.setAlignmentX(CENTER_ALIGNMENT);
        add(pathTitle);

        pathInfoArea = new JTextArea(4, 20);
        pathInfoArea.setEditable(false);
        pathInfoArea.setLineWrap(true);
        pathInfoArea.setWrapStyleWord(true);
        pathInfoArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        JScrollPane sp = new JScrollPane(pathInfoArea);
        sp.setMaximumSize(new Dimension(260, 100));
        add(sp);
    }

    public void animateRolling(Runnable onComplete) {
        final int[] count = {0};
        // FIXED: Explicitly use javax.swing.Timer
        javax.swing.Timer t = new javax.swing.Timer(80, e -> {
            if(count[0]++ < 12) {
                mainDiceFace.setValue(new Random().nextInt(6)+1);
                dice2Label.setText(new Random().nextBoolean() ? "▲" : "▼");
            } else {
                ((javax.swing.Timer)e.getSource()).stop();
                onComplete.run();
            }
        });
        t.start();
    }

    public void showResult(int val, int mod) {
        mainDiceFace.setValue(val);
        dice2Label.setText(mod == 1 ? "▲" : "▼");
        dice2Label.setForeground(mod == 1 ? new Color(34, 139, 34) : new Color(220, 20, 60));
    }

    public void setMessage(String msg, Color c) { messageLabel.setText("<html><center>"+msg+"</center></html>"); messageLabel.setForeground(c); }
    public void setPathInfo(String s) { pathInfoArea.setText(s); }
    public void clearPathInfo() { pathInfoArea.setText(""); }
    public void setRollEnabled(boolean b) { rollButton.setEnabled(b); rollButton.setBackground(b ? new Color(65, 105, 225) : Color.GRAY); }
    public void reset() { mainDiceFace.setValue(1); dice2Label.setText("?"); dice2Label.setForeground(Color.GRAY); messageLabel.setText(""); }
}

// ============================================================================
// 9. CONTROLLER UTAMA
// ============================================================================
public class SnakeLadderGame extends JFrame {
    private static Map<String, Integer> globalScoreMap = new HashMap<>();
    private static Map<String, Integer> globalWinMap = new HashMap<>();

    private GameBoard board;
    private Stack<Player> playerStack;
    private Dice dice;
    private BoardPanel boardPanel;
    private ControlPanel controlPanel;
    private PlayerInfoPanel playerInfoPanel;
    private Player currentPlayer;
    private SoundManager soundManager;

    public SnakeLadderGame(List<String> names) {
        board = new GameBoard(8, 8);
        dice = new Dice();
        playerStack = new Stack<>();
        soundManager = new SoundManager();

        Color[] cols = {new Color(255, 99, 71), new Color(60, 179, 113), new Color(255, 215, 0), new Color(186, 85, 211), Color.CYAN, Color.MAGENTA};

        for(int i = names.size() - 1; i >= 0; i--) {
            String name = names.get(i);
            playerStack.push(new Player(name, cols[i % cols.length]));
            globalScoreMap.putIfAbsent(name, 0);
            globalWinMap.putIfAbsent(name, 0);
        }

        setupUI();
        soundManager.playBgm("bgm.wav");
    }

    private void setupUI() {
        setTitle("Royal Snake & Ladder (Stack & Audio Edition)");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        JPanel header = new JPanel();
        header.setBackground(new Color(65, 105, 225));
        header.setPreferredSize(new Dimension(0, 70));
        JLabel title = new JLabel("🏰 ROYAL SNAKE & LADDER 🏰");
        title.setFont(new Font("Serif", Font.BOLD, 32));
        title.setForeground(Color.WHITE);
        header.add(title);
        add(header, BorderLayout.NORTH);

        boardPanel = new BoardPanel(board, playerStack);
        add(boardPanel, BorderLayout.CENTER);

        controlPanel = new ControlPanel(e -> playTurn());
        playerInfoPanel = new PlayerInfoPanel(playerStack);

        JPanel right = new JPanel();
        right.setLayout(new BoxLayout(right, BoxLayout.Y_AXIS));
        right.setBackground(new Color(65, 105, 225));
        right.add(playerInfoPanel);
        right.add(controlPanel);
        add(right, BorderLayout.EAST);

        pack(); setLocationRelativeTo(null); nextPlayer();
    }

    private void nextPlayer() {
        currentPlayer = playerStack.peek();
        playerInfoPanel.setCurrentPlayer(currentPlayer);
        controlPanel.reset(); controlPanel.clearPathInfo(); boardPanel.clearShortestPath();
        controlPanel.setMessage("Turn: " + currentPlayer.getName(), new Color(65, 105, 225));
        controlPanel.setRollEnabled(true);
    }

    private void playTurn() {
        controlPanel.setRollEnabled(false);
        controlPanel.setMessage("Rolling...", Color.GRAY);
        soundManager.playSfx("roll.wav");
        playerStack.pop();

        controlPanel.animateRolling(() -> {
            int d1 = dice.rollMainDice(), d2 = dice.rollModifierDice();
            controlPanel.showResult(d1, d2);
            int steps = d1 * d2;
            if (steps < 0) soundManager.playSfx("backward.wav");

            int target = Math.max(1, Math.min(board.getTotalSquares(), currentPlayer.getPosition() + steps));
            animateMove(currentPlayer.getPosition(), target, steps > 0, () -> checkTileEvents(target));
        });
    }

    private void animateMove(int s, int e, boolean fwd, Runnable done) {
        if(s==e) { done.run(); return; }

        final int[] pos = {s};
        // FIXED: Explicitly use javax.swing.Timer
        javax.swing.Timer t = new javax.swing.Timer(200, ev -> {
            if((fwd && pos[0]<e) || (!fwd && pos[0]>e)) {
                pos[0] += (fwd ? 1 : -1);
                currentPlayer.setPosition(pos[0]);
                boardPanel.repaint(); playerInfoPanel.repaint();
            } else {
                ((javax.swing.Timer)ev.getSource()).stop();
                done.run();
            }
        });
        t.start();
    }

    private void checkTileEvents(int pos) {
        int collected = board.collectPoint(pos);
        if (collected > 0) {
            currentPlayer.addScore(collected);
            globalScoreMap.put(currentPlayer.getName(), globalScoreMap.get(currentPlayer.getName()) + collected);
            controlPanel.setMessage("💰 Got " + collected + " Points!", new Color(218, 165, 32));
            soundManager.playSfx("coin.wav");
            boardPanel.repaint();
            playerInfoPanel.repaint();
        }

        if(board.getLinks().containsKey(pos)) {
            Link l = board.getLinks().get(pos);
            // FIXED: Explicitly use javax.swing.Timer
            javax.swing.Timer t = new javax.swing.Timer(1000, ev -> {
                controlPanel.setMessage("🪜 SHORTCUT!", new Color(34, 139, 34));
                animateMove(pos, l.getTo(), true, () -> checkSpecial(l.getTo()));
                ((javax.swing.Timer)ev.getSource()).stop();
            });
            t.setRepeats(false);
            t.start();
        } else {
            checkSpecial(pos);
        }
    }

    private void checkSpecial(int pos) {
        if(board.hasStar(pos)) {
            controlPanel.setMessage("⭐ BONUS! Play Again!", new Color(255, 140, 0));
            // FIXED: Explicitly use javax.swing.Timer
            javax.swing.Timer t = new javax.swing.Timer(1500, ev -> {
                playerStack.push(currentPlayer);
                nextPlayer();
                ((javax.swing.Timer)ev.getSource()).stop();
            });
            t.setRepeats(false);
            t.start();
            return;
        }

        if(board.isPrimePosition(pos)) {
            controlPanel.setMessage("🔢 DIJKSTRA PATH!", Color.BLUE);
            List<Integer> path = board.findShortestPathDijkstra(pos, board.getTotalSquares());
            boardPanel.setShortestPath(path);
            controlPanel.setPathInfo("Steps: " + (path.size()-1) + "\n" + path.toString());
        }

        if(currentPlayer.hasWon(board.getTotalSquares())) {
            soundManager.playSfx("win.wav");
            globalWinMap.put(currentPlayer.getName(), globalWinMap.get(currentPlayer.getName()) + 1);
            showLeaderboard();
            return;
        }

        // FIXED: Explicitly use javax.swing.Timer
        javax.swing.Timer t = new javax.swing.Timer(1500, ev -> {
            playerStack.add(0, currentPlayer);
            nextPlayer();
            ((javax.swing.Timer)ev.getSource()).stop();
        });
        t.setRepeats(false);
        t.start();
    }

    private void showLeaderboard() {
        JDialog d = new JDialog(this, "🏆 Global Leaderboard", true);
        d.setLayout(new BorderLayout());
        d.setSize(400, 500);
        d.setLocationRelativeTo(this);

        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setBorder(new EmptyBorder(20, 20, 20, 20));
        content.setBackground(new Color(240, 248, 255));

        JLabel title = new JLabel("🏆 HALL OF FAME");
        title.setFont(new Font("Arial", Font.BOLD, 24));
        title.setAlignmentX(CENTER_ALIGNMENT);
        content.add(title);
        content.add(Box.createVerticalStrut(20));

        List<Map.Entry<String, Integer>> list = new ArrayList<>(globalScoreMap.entrySet());
        list.sort((a, b) -> b.getValue().compareTo(a.getValue()));

        for(Map.Entry<String, Integer> entry : list) {
            String name = entry.getKey();
            int score = entry.getValue();
            int wins = globalWinMap.getOrDefault(name, 0);

            JPanel row = new JPanel(new BorderLayout());
            row.setBackground(Color.WHITE);
            row.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
            row.setMaximumSize(new Dimension(350, 50));

            JLabel nameLbl = new JLabel("  " + name);
            nameLbl.setFont(new Font("Arial", Font.BOLD, 16));

            JLabel statLbl = new JLabel("Score: " + score + " | Wins: " + wins + "  ");
            statLbl.setFont(new Font("Arial", Font.PLAIN, 14));

            row.add(nameLbl, BorderLayout.WEST);
            row.add(statLbl, BorderLayout.EAST);
            content.add(row);
            content.add(Box.createVerticalStrut(10));
        }

        JButton closeBtn = new JButton("PLAY AGAIN");
        closeBtn.setBackground(new Color(65, 105, 225));
        closeBtn.setForeground(Color.WHITE);
        closeBtn.setAlignmentX(CENTER_ALIGNMENT);
        closeBtn.addActionListener(e -> {
            d.dispose();
            this.dispose();
            showHomepage();
        });

        content.add(Box.createVerticalStrut(20));
        content.add(closeBtn);
        d.add(content);
        d.setVisible(true);
    }

    public static void showHomepage() {
        JFrame home = new JFrame("Royal Snake & Ladder - Setup");
        home.setSize(500, 400);
        home.setLocationRelativeTo(null);
        home.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(new Color(65, 105, 225));
        panel.setBorder(new EmptyBorder(40, 40, 40, 40));

        JLabel title = new JLabel("🏰 WELCOME 🏰");
        title.setFont(new Font("Serif", Font.BOLD, 32));
        title.setForeground(Color.WHITE);
        title.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel lblNum = new JLabel("Number of Players (2-4):");
        lblNum.setForeground(Color.WHITE);
        lblNum.setAlignmentX(Component.CENTER_ALIGNMENT);

        JSpinner spinPlayers = new JSpinner(new SpinnerNumberModel(2, 2, 4, 1));
        spinPlayers.setMaximumSize(new Dimension(100, 30));

        JButton btnNext = new JButton("NEXT");
        btnNext.setBackground(Color.WHITE);
        btnNext.setForeground(new Color(65, 105, 225));
        btnNext.setAlignmentX(Component.CENTER_ALIGNMENT);

        panel.add(title);
        panel.add(Box.createVerticalStrut(30));
        panel.add(lblNum);
        panel.add(Box.createVerticalStrut(10));
        panel.add(spinPlayers);
        panel.add(Box.createVerticalStrut(30));
        panel.add(btnNext);

        btnNext.addActionListener(e -> {
            int num = (int) spinPlayers.getValue();
            home.dispose();
            showNameInput(num);
        });

        home.add(panel);
        home.setVisible(true);
    }

    private static void showNameInput(int count) {
        JDialog d = new JDialog((Frame)null, "Enter Names", true);
        d.setSize(400, 400);
        d.setLocationRelativeTo(null);

        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(new EmptyBorder(20, 20, 20, 20));

        List<JTextField> fields = new ArrayList<>();
        for(int i=0; i<count; i++) {
            panel.add(new JLabel("Player " + (i+1) + " Name:"));
            JTextField tf = new JTextField("Player " + (i+1));
            fields.add(tf);
            panel.add(tf);
            panel.add(Box.createVerticalStrut(10));
        }

        JButton start = new JButton("START GAME");
        start.setAlignmentX(Component.CENTER_ALIGNMENT);
        start.setBackground(new Color(65, 105, 225));
        start.setForeground(Color.WHITE);

        start.addActionListener(e -> {
            List<String> names = new ArrayList<>();
            for(JTextField tf : fields) names.add(tf.getText());
            d.dispose();
            SwingUtilities.invokeLater(() -> new SnakeLadderGame(names).setVisible(true));
        });

        panel.add(Box.createVerticalStrut(20));
        panel.add(start);
        d.add(panel);
        d.setVisible(true);
    }

    public static void main(String[] args) {
        try { UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()); } catch (Exception e) {}
        SwingUtilities.invokeLater(SnakeLadderGame::showHomepage);
    }
}