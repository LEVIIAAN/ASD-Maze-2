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

        // [UBAH] Gunakan Palet Warna Cowboy
        Color[] pal = CowboyTheme.PLAYER_COLORS;

        for (int i = names.size()-1; i >= 0; i--) {
            String pName = names.get(i);
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
        setTitle("Wild West Quest");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        // Background utama tetap pasir untuk menyatukan tepi-tepi yang mungkin kosong
        getContentPane().setBackground(CowboyTheme.BG_SAND);

        // --- HEADER ---
        // (Kode Header TETAP SAMA seperti sebelumnya, tidak perlu diubah)
        JPanel header = new JPanel() {
            // ... isi paintComponent sama ...
        };
        header.setPreferredSize(new Dimension(800, 90));
        header.setBackground(CowboyTheme.BG_SAND);
        header.setBorder(new EmptyBorder(10, 0, 10, 0));
        add(header, BorderLayout.NORTH);

        // --- BOARD PANEL ---
        boardPanel = new BoardPanel(board, playerStack);

        // Wrapper untuk Peta agar punya bingkai sendiri
        JPanel boardWrapper = new JPanel(new BorderLayout());
        boardWrapper.setBackground(CowboyTheme.WOOD_DARK); // Bingkai gelap di sekitar peta
        boardWrapper.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(60, 40, 20), 8), // Bingkai Luar Tebal
                BorderFactory.createEmptyBorder(5, 5, 5, 5) // Padding
        ));
        boardWrapper.add(boardPanel, BorderLayout.CENTER);
        add(boardWrapper, BorderLayout.CENTER);

        // --- RIGHT PANEL (PANEL KANAN BARU) ---
        JPanel rightPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                // Gambar tekstur kayu vertikal sebagai background panel kanan
                CowboyTheme.drawWoodBackground(g2, 0, 0, getWidth(), getHeight(), new Color(139, 90, 43)); // Kayu Medium

                // Garis pemisah vertikal di kiri
                g2.setColor(new Color(60, 30, 10));
                g2.fillRect(0, 0, 10, getHeight());

                // Efek paku di sepanjang garis pemisah
                g2.setColor(Color.DARK_GRAY);
                for(int i=20; i<getHeight(); i+=60) g2.fillOval(2, i, 6, 6);
            }
        };
        rightPanel.setLayout(new BoxLayout(rightPanel, BoxLayout.Y_AXIS));
        rightPanel.setBorder(new EmptyBorder(20, 25, 20, 25)); // Padding isi

        // 1. Player Panel
        playerPanel = new PlayerPanel(playerStack);
        JScrollPane scrollWrapper = new JScrollPane(playerPanel);
        scrollWrapper.setPreferredSize(new Dimension(380, 280)); // Tinggi dikurangi sedikit
        scrollWrapper.setOpaque(false);
        scrollWrapper.getViewport().setOpaque(false);
        scrollWrapper.setBorder(null);
        scrollWrapper.getVerticalScrollBar().setUnitIncrement(16);

        rightPanel.add(scrollWrapper);
        rightPanel.add(Box.createVerticalStrut(30)); // Jarak antar panel

        // 2. Control Panel
        controlPanel = new ControlPanel(e -> playTurn());
        // Control panel tidak perlu background khusus lagi karena rightPanel sudah kayu
        rightPanel.add(controlPanel);

        add(rightPanel, BorderLayout.EAST);

        pack();
        setLocationRelativeTo(null);
        refreshUI();
    }




    private void refreshUI() {
        currentPlayer = playerStack.peek();
        playerPanel.setCurrent(currentPlayer);
        // [UBAH] Warna status jadi Kayu Gelap
        controlPanel.setStatus("TURN: " + currentPlayer.getName().toUpperCase(), CowboyTheme.WOOD_DARK);
        controlPanel.toggleBtn(true);
        controlPanel.setPath("");
        boardPanel.clearPath();
    }

    private void playTurn() {
        controlPanel.toggleBtn(false);
        controlPanel.setStatus("ROLLING...", Color.GRAY);
        // [CATATAN] Pastikan file roll.wav ada di folder luar proyek
        sound.playSfx("roll.wav");

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
            // [UBAH] Warna status Emas
            controlPanel.setStatus("FOUND GOLD: " + pts + "!", CowboyTheme.GOLD_NUGGET);
        }

        if(board.getLinks().containsKey(pos)) {
            int next = board.getLinks().get(pos).getTo();
            boolean isLadder = next > pos;
            String msg = isLadder ? "CLIMBING UP!" : "SNAKE BITE!";
            // [UBAH] Warna status Biru/Merah tema Cowboy
            controlPanel.setStatus(msg, isLadder ? CowboyTheme.BLUE_DENIM : CowboyTheme.RED_BANDANA);

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
            controlPanel.setStatus("SHERIFF BADGE: BONUS TURN!", CowboyTheme.GOLD_NUGGET);
            javax.swing.Timer t = new javax.swing.Timer(1500, e -> {
                refreshUI();
                ((javax.swing.Timer)e.getSource()).stop();
            });
            t.start();
            return;
        }

        if(board.isPrime(pos)) {
            controlPanel.setStatus("SECRET SHORTCUT FOUND!", CowboyTheme.BLUE_DENIM);
            List<Integer> path = board.findShortestPath(pos, board.getTotalSquares());
            boardPanel.updatePath(path);
            controlPanel.setPath("SHORTCUT:\n" + path.toString());
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
        JDialog d = new JDialog(this, "Yeehaw! Winner!", true);
        d.setSize(500, 600);
        d.setLocationRelativeTo(this);
        d.setUndecorated(true);

        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setBackground(CowboyTheme.BG_SAND);
        p.setBorder(new LineBorder(CowboyTheme.WOOD_DARK, 5));

        JLabel lbl = new JLabel("🏆 TOP GUNSLINGERS 🏆");
        lbl.setFont(CowboyTheme.FONT_TITLE);
        lbl.setForeground(CowboyTheme.WOOD_DARK);
        lbl.setAlignmentX(CENTER_ALIGNMENT);
        p.add(Box.createVerticalStrut(20));
        p.add(lbl);
        p.add(Box.createVerticalStrut(30));

        List<Map.Entry<String, Integer>> top3 = LeaderboardManager.getTop3Scores();

        int rank = 1;
        for (Map.Entry<String, Integer> entry : top3) {
            JPanel row = new JPanel(new FlowLayout(FlowLayout.LEFT));
            row.setOpaque(false);
            row.setMaximumSize(new Dimension(400, 50));

            JLabel text = new JLabel(rank + ". " + entry.getKey() + " - $" + entry.getValue());
            text.setFont(CowboyTheme.FONT_TEXT.deriveFont(20f));
            text.setForeground(CowboyTheme.WOOD_DARK);

            row.add(text);
            p.add(row);
            rank++;
        }

        p.add(Box.createVerticalStrut(40));

        CowboyTheme.Button btnReplay = new CowboyTheme.Button("PLAY AGAIN");
        btnReplay.setAlignmentX(CENTER_ALIGNMENT);
        btnReplay.addActionListener(e -> {
            d.dispose();
            dispose();
            new SnakeLadderGame(originalNames).setVisible(true);
        });

        CowboyTheme.Button btnMenu = new CowboyTheme.Button("MAIN MENU");
        btnMenu.setAlignmentX(CENTER_ALIGNMENT);
        btnMenu.addActionListener(e -> {
            d.dispose();
            dispose();
            showMainMenu();
        });

        p.add(btnReplay);
        p.add(Box.createVerticalStrut(10));
        p.add(btnMenu);

        d.add(p);
        d.setVisible(true);
    }

    // --- MAIN MENU & ENTRY POINT ---
    public static void main(String[] args) {
        System.setProperty("awt.useSystemAAFontSettings", "on");
        SwingUtilities.invokeLater(SnakeLadderGame::showMainMenu);
    }

    private static void showMainMenu() {
        JFrame frame = new JFrame("Wild West Quest");
        frame.setSize(500, 480);
        frame.setLocationRelativeTo(null);
        frame.setDefaultCloseOperation(EXIT_ON_CLOSE);

        JPanel mainPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                g2.setColor(CowboyTheme.BG_SAND);
                g2.fillRect(0, 0, getWidth(), getHeight());

                // Efek Kertas Tua (Bintik)
                g2.setColor(new Color(0,0,0,20));
                for(int i=0; i<300; i++) {
                    int x = (int)(Math.random()*getWidth());
                    int y = (int)(Math.random()*getHeight());
                    g2.fillOval(x, y, 2, 2);
                }
            }
        };
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setBorder(new EmptyBorder(40, 40, 40, 40));

        JLabel title = new JLabel("WILD WEST QUEST");
        title.setFont(CowboyTheme.FONT_TITLE);
        title.setForeground(CowboyTheme.WOOD_DARK);
        title.setAlignmentX(CENTER_ALIGNMENT);

        JLabel subtitle = new JLabel("/// WANTED: BRAVE SOULS ///");
        subtitle.setFont(CowboyTheme.FONT_TEXT);
        subtitle.setForeground(CowboyTheme.RED_BANDANA);
        subtitle.setAlignmentX(CENTER_ALIGNMENT);

        // Selector Pemain
        JPanel selector = new JPanel(new FlowLayout());
        selector.setOpaque(false);
        JLabel numLabel = new JLabel("2");
        numLabel.setFont(new Font("Rockwell", Font.BOLD, 50));
        numLabel.setForeground(CowboyTheme.WOOD_DARK);

        CowboyTheme.Button btnMin = new CowboyTheme.Button("-");
        btnMin.setPreferredSize(new Dimension(50, 50));
        btnMin.addActionListener(e -> {
            int n = Integer.parseInt(numLabel.getText());
            if(n>2) numLabel.setText(""+(n-1));
        });

        CowboyTheme.Button btnPlus = new CowboyTheme.Button("+");
        btnPlus.setPreferredSize(new Dimension(50, 50));
        btnPlus.addActionListener(e -> {
            int n = Integer.parseInt(numLabel.getText());
            if(n<10) numLabel.setText(""+(n+1));
        });

        selector.add(btnMin);
        selector.add(numLabel);
        selector.add(btnPlus);

        CowboyTheme.Button btnStart = new CowboyTheme.Button("START ADVENTURE");
        btnStart.setAlignmentX(CENTER_ALIGNMENT);
        btnStart.setMaximumSize(new Dimension(250, 60));
        btnStart.addActionListener(e -> {
            frame.dispose();
            showNameInput(Integer.parseInt(numLabel.getText()));
        });

        mainPanel.add(title);
        mainPanel.add(Box.createVerticalStrut(10));
        mainPanel.add(subtitle);
        mainPanel.add(Box.createVerticalStrut(40));
        mainPanel.add(selector);
        mainPanel.add(Box.createVerticalStrut(40));
        mainPanel.add(btnStart);

        frame.add(mainPanel);
        frame.setVisible(true);
    }

    private static void showNameInput(int n) {
        JDialog d = new JDialog((Frame)null, "Who are you, stranger?", true);
        int height = Math.min(600, 200 + (n * 70));
        d.setSize(450, height);
        d.setLocationRelativeTo(null);

        JPanel p = new JPanel();
        p.setLayout(new BorderLayout()); // Layout Utama Border
        p.setBackground(CowboyTheme.BG_SAND);
        p.setBorder(new EmptyBorder(20, 20, 20, 20));

        // Judul
        JLabel title = new JLabel("ENTER NAMES", SwingConstants.CENTER);
        title.setFont(CowboyTheme.FONT_TITLE.deriveFont(24f));
        title.setForeground(CowboyTheme.WOOD_DARK);
        p.add(title, BorderLayout.NORTH);

        // Scrollable Input
        JPanel inputs = new JPanel();
        inputs.setLayout(new BoxLayout(inputs, BoxLayout.Y_AXIS));
        inputs.setBackground(CowboyTheme.BG_SAND);

        List<JTextField> tfs = new ArrayList<>();
        for(int i=0; i<n; i++) {
            JPanel row = new JPanel(new BorderLayout(10,0));
            row.setOpaque(false);
            row.setMaximumSize(new Dimension(400, 40));

            JLabel num = new JLabel((i+1)+".");
            num.setFont(CowboyTheme.FONT_NUM);

            JTextField tf = new JTextField("Cowboy " + (i+1));
            tf.setBackground(new Color(245, 230, 210));
            tf.setBorder(BorderFactory.createLineBorder(CowboyTheme.WOOD_DARK));
            tf.setFont(CowboyTheme.FONT_TEXT);

            tfs.add(tf);
            row.add(num, BorderLayout.WEST);
            row.add(tf, BorderLayout.CENTER);

            inputs.add(row);
            inputs.add(Box.createVerticalStrut(15));
        }

        JScrollPane scroll = new JScrollPane(inputs);
        scroll.setBorder(null);
        scroll.getViewport().setBackground(CowboyTheme.BG_SAND);
        p.add(scroll, BorderLayout.CENTER);

        // Tombol Start
        JPanel btnPanel = new JPanel();
        btnPanel.setOpaque(false);
        CowboyTheme.Button btn = new CowboyTheme.Button("RIDE ON!");
        btn.setPreferredSize(new Dimension(200, 50));
        btn.addActionListener(e -> {
            List<String> names = new ArrayList<>();
            for(JTextField tf : tfs) names.add(tf.getText());
            d.dispose();
            new SnakeLadderGame(names).setVisible(true);
        });
        btnPanel.add(btn);
        p.add(btnPanel, BorderLayout.SOUTH);

        d.add(p);
        d.setVisible(true);
    }
}