import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.util.*;
import java.util.List;

public class SnakeLadderGame extends JFrame {
    private final GameBoard board;
    private final Stack<Player> playerStack;
    private final Dice dice;
    private final SoundManager sound;
    private final List<String> originalNames;

    private BoardPanel boardPanel;
    private ControlPanel controlPanel;
    private PlayerPanel playerPanel;
    private Player currentPlayer;

    public SnakeLadderGame(List<String> names) {
        this.originalNames = names;
        this.board = new GameBoard(8, 8);
        this.dice = new Dice();
        this.sound = new SoundManager();
        this.playerStack = new Stack<>();

        // [MODIFIKASI OOP] Encapsulation: Definisi palet warna diperluas untuk 10 pemain
        // Kita menggunakan array objek Color untuk menyimpan state visual
        Color[] pal = {
                CyberTheme.NEON_CYAN,           // P1
                CyberTheme.NEON_PINK,           // P2
                CyberTheme.NEON_GREEN,          // P3
                CyberTheme.NEON_PURPLE,         // P4
                new Color(255, 165, 0),         // P5 (Orange Neon)
                new Color(255, 50, 50),         // P6 (Red Neon)
                new Color(50, 100, 255),        // P7 (Royal Blue)
                new Color(255, 255, 255),       // P8 (White Bright)
                new Color(0, 255, 127),         // P9 (Spring Green)
                new Color(255, 20, 147)         // P10 (Deep Pink)
        };

        // Loop inisialisasi pemain
        for (int i = names.size()-1; i >= 0; i--) {
            String pName = names.get(i);
            // Menggunakan modulus (%) memastikan tidak error meski warna habis (defensive programming)
            Player p = new Player(pName, pal[i % pal.length]);

            int savedScore = LeaderboardManager.getScore(pName);
            p.setScore(savedScore);

            playerStack.push(p);
            LeaderboardManager.addScore(pName, 0);
        }

        initUI();
        sound.playBgm("bgm.wav");
    }

    private void initUI() {
        // Setup Frame
        setTitle("Cyberpunk: NetRunner Race");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout());
        getContentPane().setBackground(CyberTheme.BG_DARK);

        // --- HEADER ---
        JPanel header = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                String text = "NEON ASCENSION";
                g2.setFont(CyberTheme.FONT_TITLE);
                FontMetrics fm = g2.getFontMetrics();
                int textW = fm.stringWidth(text);
                int textH = fm.getAscent();
                int centerX = getWidth() / 2;
                int centerY = getHeight() / 2 + textH / 4;

                g2.setColor(CyberTheme.NEON_CYAN);
                g2.drawString(text, centerX - textW / 2, centerY);

                int iconGap = 40;
                drawLightning(g2, centerX - textW / 2 - iconGap, centerY - 15, true);
                drawLightning(g2, centerX + textW / 2 + iconGap, centerY - 15, false);
            }
        };
        header.setPreferredSize(new Dimension(800, 80));
        header.setBackground(CyberTheme.BG_DARK);
        header.setBorder(new EmptyBorder(10, 0, 10, 0));
        add(header, BorderLayout.NORTH);

        // --- BOARD PANEL ---
        boardPanel = new BoardPanel(board, playerStack);
        add(boardPanel, BorderLayout.CENTER);

        // --- RIGHT PANEL ---
        JPanel rightPanel = new JPanel();
        rightPanel.setLayout(new BoxLayout(rightPanel, BoxLayout.Y_AXIS));
        rightPanel.setBackground(CyberTheme.BG_DARK);
        rightPanel.setBorder(new EmptyBorder(20, 30, 20, 30));

        // [MODIFIKASI] PLAYER PANEL DENGAN SCROLL
        playerPanel = new PlayerPanel(playerStack);

        // Bungkus PlayerPanel dengan JScrollPane
        JScrollPane scrollWrapper = new JScrollPane(playerPanel);
        scrollWrapper.setPreferredSize(new Dimension(400, 300)); // Ukuran fix area scroll
        scrollWrapper.setOpaque(false); // Transparan
        scrollWrapper.getViewport().setOpaque(false); // Viewport transparan
        scrollWrapper.setBorder(null); // Hilangkan border default

        // Custom ScrollBar UI (Agar scrollbar terlihat menyatu dengan tema)
        scrollWrapper.getVerticalScrollBar().setUI(new javax.swing.plaf.basic.BasicScrollBarUI() {
            @Override
            protected void configureScrollBarColors() {
                this.thumbColor = CyberTheme.NEON_CYAN; // Warna Scroll
                this.trackColor = new Color(20, 20, 30); // Warna Track
            }
            @Override
            protected JButton createDecreaseButton(int orientation) { return createZeroButton(); }
            @Override
            protected JButton createIncreaseButton(int orientation) { return createZeroButton(); }
            private JButton createZeroButton() {
                JButton jbutton = new JButton();
                jbutton.setPreferredSize(new Dimension(0, 0)); // Hilangkan tombol panah atas/bawah
                return jbutton;
            }
        });

        rightPanel.add(scrollWrapper);
        rightPanel.add(Box.createVerticalStrut(20));

        // Control Panel
        controlPanel = new ControlPanel(e -> playTurn());
        controlPanel.setBackground(CyberTheme.BG_PANEL);
        rightPanel.add(controlPanel);

        add(rightPanel, BorderLayout.EAST);

        pack();
        setLocationRelativeTo(null);
        refreshUI();
    }

    // Helper untuk menggambar Petir Neon (SUDAH DIPERBAIKI: boolean)
    private void drawLightning(Graphics2D g2, int x, int y, boolean alignRight) {
        int[] xPoints = {0, 15, 8, 20, 5, 12, 0};
        int[] yPoints = {0, 0, 15, 15, 35, 20, 20};

        if (alignRight) x -= 20;
        for(int i=0; i<xPoints.length; i++) {
            xPoints[i] += x;
            yPoints[i] += y;
        }

        Polygon p = new Polygon(xPoints, yPoints, xPoints.length);
        CyberTheme.drawGlowingPolygon(g2, p, CyberTheme.NEON_YELLOW);
    }

    private void refreshUI() {
        currentPlayer = playerStack.peek();
        playerPanel.setCurrent(currentPlayer);
        controlPanel.setStatus("TURN: " + currentPlayer.getName().toUpperCase(), CyberTheme.NEON_CYAN);
        controlPanel.toggleBtn(true);
        controlPanel.setPath("");
        boardPanel.clearPath();
    }

    private void playTurn() {
        controlPanel.toggleBtn(false);
        controlPanel.setStatus("INITIALIZING ROLL...", Color.GRAY);
        sound.playSfx("C:\\Users\\Farhan Fitran\\Documents\\Coding Projects\\ASD\\Final Project SnakeLadder\\sound\\roll.wav");

        final int[] count = {0};
        javax.swing.Timer t = new javax.swing.Timer(80, e -> {
            if (count[0]++ < 12) {
                // Animation frame
            } else {
                ((javax.swing.Timer)e.getSource()).stop();
                finalizeTurn();
            }
        });
        t.start();
    }

    private void finalizeTurn() {
        int d1 = dice.rollMain();
        int d2 = dice.rollModifier();
        controlPanel.updateDice(d1, d2);

        if(d2 == -1) sound.playSfx("backward.wav");

        int steps = d1 * d2;
        int currentPos = currentPlayer.getPosition();
        int target = Math.max(1, Math.min(board.getTotalSquares(), currentPos + steps));

        animateMove(target, () -> checkEvents(target));
    }

    private void animateMove(int targetPos, Runnable onComplete) {
        int startPos = currentPlayer.getPosition();
        if (startPos == targetPos) {
            onComplete.run();
            return;
        }

        int direction = Integer.compare(targetPos, startPos);
        javax.swing.Timer stepTimer = new javax.swing.Timer(300, null);
        stepTimer.addActionListener(e -> {
            int current = currentPlayer.getPosition();
            if (current == targetPos) {
                stepTimer.stop();
                onComplete.run();
                return;
            }
            currentPlayer.setPosition(current + direction);
            boardPanel.repaint();
        });
        stepTimer.setInitialDelay(0);
        stepTimer.start();
    }

    private void checkEvents(int pos) {
        int pts = board.collectPoint(pos);
        if(pts > 0) {
            currentPlayer.addScore(pts);
            LeaderboardManager.addScore(currentPlayer.getName(), pts);
            sound.playSfx("coin.wav");
            controlPanel.setStatus("DATA ACQUIRED: " + pts + " UNITS!", CyberTheme.NEON_YELLOW);
        }

        if(board.getLinks().containsKey(pos)) {
            int next = board.getLinks().get(pos).getTo();
            javax.swing.Timer t = new javax.swing.Timer(800, ev -> {
                ((javax.swing.Timer)ev.getSource()).stop();
                animateMove(next, () -> checkSpecial(next));
            });
            t.start();
        } else {
            checkSpecial(pos);
        }
    }

    private void checkSpecial(int pos) {
        if(board.hasStar(pos)) {
            controlPanel.setStatus("⭐ SYSTEM OVERRIDE: BONUS TURN!", CyberTheme.NEON_PURPLE);
            javax.swing.Timer t = new javax.swing.Timer(1500, e -> {
                refreshUI();
                ((javax.swing.Timer)e.getSource()).stop();
            });
            t.start();
            return;
        }

        if(board.isPrime(pos)) {
            controlPanel.setStatus("🔢 OPTIMAL PATH CALCULATED!", CyberTheme.NEON_CYAN);
            List<Integer> path = board.findShortestPath(pos, board.getTotalSquares());
            boardPanel.updatePath(path);
            controlPanel.setPath("SHORTEST PATH:\n" + path.toString());
        }

        if(currentPlayer.hasWon(board.getTotalSquares())) {
            sound.playSfx("win.wav");
            LeaderboardManager.addWin(currentPlayer.getName());
            showLeaderboard();
            return;
        }

        javax.swing.Timer t = new javax.swing.Timer(1500, e -> {
            playerStack.pop();
            playerStack.add(0, currentPlayer);
            refreshUI();
            ((javax.swing.Timer)e.getSource()).stop();
        });
        t.start();
    }

    private void showLeaderboard() {
        JDialog d = new JDialog(this, "Mission Complete", true);
        d.setSize(500, 600);
        d.setLocationRelativeTo(this);
        d.setUndecorated(true);

        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setBackground(CyberTheme.BG_DARK);
        p.setBorder(new LineBorder(CyberTheme.NEON_CYAN, 3));

        JLabel lbl = new JLabel("🏆 TOP AGENTS 🏆");
        lbl.setFont(CyberTheme.FONT_TITLE);
        lbl.setForeground(CyberTheme.NEON_YELLOW);
        lbl.setAlignmentX(CENTER_ALIGNMENT);
        p.add(Box.createVerticalStrut(20));
        p.add(lbl);
        p.add(Box.createVerticalStrut(30));

        List<Map.Entry<String, Integer>> top3 = LeaderboardManager.getTop3Scores();
        Map<String, Integer> wins = LeaderboardManager.getWins();

        int rank = 1;
        for (Map.Entry<String, Integer> entry : top3) {
            String name = entry.getKey();
            int score = entry.getValue();
            int winCount = wins.getOrDefault(name, 0);

            JPanel rowPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
            rowPanel.setOpaque(false);
            rowPanel.setAlignmentX(CENTER_ALIGNMENT);
            rowPanel.setBorder(new LineBorder(rank == 1 ? CyberTheme.NEON_YELLOW : (rank == 2 ? Color.LIGHT_GRAY : new Color(205, 127, 50)), 2, true));
            rowPanel.setPreferredSize(new Dimension(400, 60));

            JLabel medal = new JLabel(rank == 1 ? "🥇 " : (rank == 2 ? "🥈 " : "🥉 "));
            medal.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 30));

            JLabel text = new JLabel(String.format("RANK %d: %s | 💰 %d | 🏆 %d WINS", rank, name.toUpperCase(), score, winCount));
            text.setFont(CyberTheme.FONT_TEXT);
            text.setForeground(Color.WHITE);

            rowPanel.add(medal);
            rowPanel.add(text);
            p.add(rowPanel);
            p.add(Box.createVerticalStrut(15));
            rank++;
        }

        if (top3.isEmpty()) {
            JLabel empty = new JLabel("NO DATA AVAILABLE.");
            empty.setFont(CyberTheme.FONT_TEXT);
            empty.setForeground(Color.GRAY);
            empty.setAlignmentX(CENTER_ALIGNMENT);
            p.add(empty);
        }

        p.add(Box.createVerticalStrut(40));

        JPanel btnPanel = new JPanel(new GridLayout(1, 2, 20, 0));
        btnPanel.setOpaque(false);
        btnPanel.setMaximumSize(new Dimension(400, 50));

        CyberTheme.Button btnReplay = new CyberTheme.Button("REPLAY MISSION", CyberTheme.NEON_CYAN);
        btnReplay.addActionListener(e -> {
            d.dispose();
            dispose();
            new SnakeLadderGame(originalNames).setVisible(true);
        });

        CyberTheme.Button btnMenu = new CyberTheme.Button("MAIN MENU", CyberTheme.NEON_PINK);
        btnMenu.addActionListener(e -> {
            d.dispose();
            dispose();
            showMainMenu();
        });

        btnPanel.add(btnReplay);
        btnPanel.add(btnMenu);
        p.add(btnPanel);
        p.add(Box.createVerticalStrut(20));

        d.add(p);
        d.setVisible(true);
    }

    // ==================================================================================
    // STATIC ENTRY POINT & MAIN MENU (HOMEPAGE)
    // ==================================================================================

    public static void main(String[] args) {
        System.setProperty("awt.useSystemAAFontSettings", "on");
        System.setProperty("swing.aatext", "true");
        SwingUtilities.invokeLater(SnakeLadderGame::showMainMenu);
    }

    private static void showMainMenu() {
        JFrame frame = new JFrame("Cyber Grid: System Initialization");
        frame.setSize(500, 480); // Ukuran sedikit dipertinggi agar muat
        frame.setLocationRelativeTo(null);
        frame.setDefaultCloseOperation(EXIT_ON_CLOSE);

        // --- 1. SETUP BACKGROUND PANEL (GRID EFFECT) ---
        JPanel mainPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                // Isi Background Gelap
                g2.setColor(CyberTheme.BG_DARK);
                g2.fillRect(0, 0, getWidth(), getHeight());

                // Gambar Garis Grid Transparan
                g2.setColor(new Color(0, 255, 255, 10)); // Cyan sangat transparan
                for(int i=0; i<getWidth(); i+=40) g2.drawLine(i, 0, i, getHeight());
                for(int i=0; i<getHeight(); i+=40) g2.drawLine(0, i, getWidth(), i);
            }
        };
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setBorder(new EmptyBorder(40, 40, 40, 40));

        // --- 2. HEADER & LOGO ---
        JLabel title = new JLabel("NEON ASCENSION");
        title.setFont(CyberTheme.FONT_TITLE.deriveFont(36f));
        title.setForeground(CyberTheme.NEON_CYAN);
        title.setAlignmentX(CENTER_ALIGNMENT);

        JLabel subtitle = new JLabel("/// PROTOCOL INITIATED ///");
        subtitle.setFont(CyberTheme.FONT_TEXT);
        subtitle.setForeground(CyberTheme.NEON_PINK);
        subtitle.setAlignmentX(CENTER_ALIGNMENT);

        // --- 3. PLAYER SELECTOR (TOMBOL +/-) ---
        JPanel selectorPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 0));
        selectorPanel.setOpaque(false);
        selectorPanel.setMaximumSize(new Dimension(400, 100));

        // Label Angka Besar
        JLabel numLabel = new JLabel("2");
        numLabel.setFont(new Font("Impact", Font.PLAIN, 60));
        numLabel.setForeground(Color.WHITE);

        // Tombol Kurang [-]
        CyberTheme.Button btnMinus = new CyberTheme.Button("-", CyberTheme.NEON_PINK);
        btnMinus.setPreferredSize(new Dimension(50, 50));
        btnMinus.setFont(new Font("Arial", Font.BOLD, 24));
        btnMinus.addActionListener(e -> {
            int n = Integer.parseInt(numLabel.getText());
            if (n > 2) numLabel.setText(String.valueOf(n - 1)); // Minimal 2 Pemain
        });

        // Tombol Tambah [+]
        CyberTheme.Button btnPlus = new CyberTheme.Button("+", CyberTheme.NEON_GREEN);
        btnPlus.setPreferredSize(new Dimension(50, 50));
        btnPlus.setFont(new Font("Arial", Font.BOLD, 24));
        btnPlus.addActionListener(e -> {
            int n = Integer.parseInt(numLabel.getText());
            if (n < 10) numLabel.setText(String.valueOf(n + 1)); // Maksimal 10 Pemain
        });

        selectorPanel.add(btnMinus);
        selectorPanel.add(numLabel);
        selectorPanel.add(btnPlus);

        // Label Info
        JLabel lblInfo = new JLabel("SELECT NUMBER OF AGENTS (2-10)");
        lblInfo.setFont(CyberTheme.FONT_TEXT);
        lblInfo.setForeground(Color.GRAY);
        lblInfo.setAlignmentX(CENTER_ALIGNMENT);

        // --- 4. TOMBOL START ---
        CyberTheme.Button btnStart = new CyberTheme.Button("INITIALIZE MISSION", CyberTheme.NEON_CYAN);
        btnStart.setAlignmentX(CENTER_ALIGNMENT);
        btnStart.setMaximumSize(new Dimension(250, 60));
        btnStart.addActionListener(e -> {
            int n = Integer.parseInt(numLabel.getText());
            frame.dispose();
            showNameInput(n);
        });

        // --- 5. MENYUSUN KOMPONEN ---
        mainPanel.add(title);
        mainPanel.add(Box.createVerticalStrut(5));
        mainPanel.add(subtitle);
        mainPanel.add(Box.createVerticalStrut(50)); // Jarak ke selector
        mainPanel.add(lblInfo);
        mainPanel.add(Box.createVerticalStrut(10));
        mainPanel.add(selectorPanel);
        mainPanel.add(Box.createVerticalStrut(50)); // Jarak ke tombol start
        mainPanel.add(btnStart);

        frame.add(mainPanel);
        frame.setVisible(true);
    }

    private static void showNameInput(int n) {
        JDialog d = new JDialog((Frame)null, "Identity Verification", true);
        // Ukuran dialog dibatasi max 600px agar tidak terlalu panjang
        int height = Math.min(600, 200 + (n * 70));
        d.setSize(500, height);
        d.setLocationRelativeTo(null);

        // Panel Utama (Container)
        JPanel mainContainer = new JPanel();
        mainContainer.setLayout(new BorderLayout());
        mainContainer.setBackground(CyberTheme.BG_DARK);
        mainContainer.setBorder(new EmptyBorder(20, 20, 20, 20));

        // Judul di Atas
        JLabel lbl = new JLabel("ENTER AGENT ALIASES", SwingConstants.CENTER);
        lbl.setFont(CyberTheme.FONT_TITLE.deriveFont(24f));
        lbl.setForeground(CyberTheme.NEON_YELLOW);
        lbl.setBorder(new EmptyBorder(0, 0, 20, 0));
        mainContainer.add(lbl, BorderLayout.NORTH);

        // [MODIFIKASI UI] Panel Input di dalam ScrollPane
        // Panel internal untuk menampung field input
        JPanel inputPanel = new JPanel();
        inputPanel.setLayout(new BoxLayout(inputPanel, BoxLayout.Y_AXIS));
        inputPanel.setBackground(CyberTheme.BG_DARK);

        List<JTextField> tfs = new ArrayList<>();
        // Palet warna teks untuk input (sesuaikan dengan constructor)
        Color[] textColors = {
                CyberTheme.NEON_CYAN, CyberTheme.NEON_PINK, CyberTheme.NEON_GREEN, CyberTheme.NEON_PURPLE,
                new Color(255, 165, 0), new Color(255, 50, 50), new Color(50, 100, 255),
                Color.WHITE, new Color(0, 255, 127), new Color(255, 20, 147)
        };

        for(int i=0; i<n; i++) {
            JPanel row = new JPanel(new BorderLayout(15, 0));
            row.setOpaque(false);
            row.setMaximumSize(new Dimension(400, 45)); // Tinggi per baris
            row.setAlignmentX(CENTER_ALIGNMENT);

            JLabel num = new JLabel((i+1) < 10 ? "0" + (i+1) : String.valueOf(i+1));
            num.setFont(CyberTheme.FONT_NUM);
            num.setForeground(Color.GRAY);

            JTextField tf = new JTextField("Agent " + (i+1));
            tf.setBackground(new Color(30, 30, 45));
            // Gunakan modulus agar aman jika n > jumlah warna
            tf.setForeground(textColors[i % textColors.length]);
            tf.setCaretColor(Color.WHITE);
            tf.setFont(CyberTheme.FONT_TEXT);
            tf.setBorder(BorderFactory.createCompoundBorder(
                    new LineBorder(textColors[i % textColors.length], 1),
                    new EmptyBorder(5, 10, 5, 10)
            ));

            tfs.add(tf);
            row.add(num, BorderLayout.WEST);
            row.add(tf, BorderLayout.CENTER);

            inputPanel.add(row);
            inputPanel.add(Box.createVerticalStrut(15)); // Jarak antar input
        }

        // Membungkus inputPanel dengan JScrollPane
        JScrollPane scrollPane = new JScrollPane(inputPanel);
        scrollPane.getViewport().setBackground(CyberTheme.BG_DARK);
        scrollPane.setBorder(null);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16); // Scroll lebih smooth
        mainContainer.add(scrollPane, BorderLayout.CENTER);

        // Tombol Start di Bawah
        CyberTheme.Button btn = new CyberTheme.Button("UPLOAD DATA & START", CyberTheme.NEON_CYAN);
        btn.setPreferredSize(new Dimension(200, 50));
        btn.addActionListener(e -> {
            List<String> names = new ArrayList<>();
            for(JTextField tf : tfs) names.add(tf.getText());
            d.dispose();
            // Start game
            new SnakeLadderGame(names).setVisible(true);
        });

        // Panel tombol agar ada padding
        JPanel btnPanel = new JPanel();
        btnPanel.setOpaque(false);
        btnPanel.setBorder(new EmptyBorder(20, 0, 0, 0));
        btnPanel.add(btn);
        mainContainer.add(btnPanel, BorderLayout.SOUTH);

        d.add(mainContainer);
        d.setVisible(true);
    }
}