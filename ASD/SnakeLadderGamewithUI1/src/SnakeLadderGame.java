import javax.swing.*;
import javax.swing.Timer;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.util.List;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Random;

// Kelas untuk merepresentasikan Pemain
class Player {
    private String name;
    private int position;
    private Color color;

    public Player(String name, Color color) {
        this.name = name;
        this.position = 1;
        this.color = color;
    }

    public String getName() { return name; }
    public int getPosition() { return position; }
    public Color getColor() { return color; }

    public void setPosition(int position) {
        this.position = Math.max(1, position);
    }

    public boolean hasWon(int targetPosition) {
        return position >= targetPosition;
    }
}

// Kelas untuk merepresentasikan Graph Papan Permainan
class GameBoard {
    private int size;
    private int totalSquares;
    private Map<Integer, List<Integer>> adjacencyList;

    public GameBoard(int rows, int cols) {
        this.size = rows;
        this.totalSquares = rows * cols;
        this.adjacencyList = new HashMap<>();
        buildGraph();
    }

    private void buildGraph() {
        for (int i = 1; i <= totalSquares; i++) {
            adjacencyList.put(i, new ArrayList<>());
            if (i < totalSquares) {
                adjacencyList.get(i).add(i + 1);
            }
        }
    }

    public int getTotalSquares() { return totalSquares; }
    public boolean isValidPosition(int position) {
        return position >= 1 && position <= totalSquares;
    }
}

// Kelas untuk Dadu
class Dice {
    private Random random;

    public Dice() {
        this.random = new Random();
    }

    public int rollMainDice() {
        return random.nextInt(6) + 1;
    }

    public int rollModifierDice() {
        double probability = Math.random();
        return (probability < 0.8) ? 1 : -1;
    }

    public String getModifierColor(int modifier) {
        return (modifier == 1) ? "HIJAU (Maju)" : "MERAH (Mundur)";
    }
}

// Panel untuk papan permainan
class BoardPanel extends JPanel {
    private GameBoard board;
    private Queue<Player> players;
    private static final int CELL_SIZE = 70;
    private static final int BOARD_SIZE = 8;

    public BoardPanel(GameBoard board, Queue<Player> players) {
        this.board = board;
        this.players = players;
        setPreferredSize(new Dimension(BOARD_SIZE * CELL_SIZE + 40, BOARD_SIZE * CELL_SIZE + 40));
        setBackground(new Color(240, 240, 255));
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int offsetX = 20;
        int offsetY = 20;

        // Gambar papan dengan pola zig-zag
        for (int row = 0; row < BOARD_SIZE; row++) {
            for (int col = 0; col < BOARD_SIZE; col++) {
                int cellNumber;

                // Pola zig-zag seperti papan ular tangga asli
                if (row % 2 == 0) {
                    cellNumber = (BOARD_SIZE - row) * BOARD_SIZE - col;
                } else {
                    cellNumber = (BOARD_SIZE - row - 1) * BOARD_SIZE + col + 1;
                }

                int x = offsetX + col * CELL_SIZE;
                int y = offsetY + row * CELL_SIZE;

                // Warna sel bergantian
                if ((row + col) % 2 == 0) {
                    g2d.setColor(new Color(255, 255, 255));
                } else {
                    g2d.setColor(new Color(230, 240, 255));
                }

                // Kotak terakhir (finish) berwarna spesial
                if (cellNumber == 64) {
                    g2d.setColor(new Color(255, 215, 0));
                }

                g2d.fillRoundRect(x, y, CELL_SIZE, CELL_SIZE, 15, 15);

                // Border sel
                g2d.setColor(new Color(150, 150, 200));
                g2d.setStroke(new BasicStroke(2));
                g2d.drawRoundRect(x, y, CELL_SIZE, CELL_SIZE, 15, 15);

                // Nomor sel
                g2d.setColor(new Color(100, 100, 100));
                g2d.setFont(new Font("Arial", Font.BOLD, 16));
                String numStr = String.valueOf(cellNumber);
                FontMetrics fm = g2d.getFontMetrics();
                int numX = x + (CELL_SIZE - fm.stringWidth(numStr)) / 2;
                int numY = y + 20;
                g2d.drawString(numStr, numX, numY);

                // Gambar pemain di posisi mereka
                drawPlayersAtPosition(g2d, cellNumber, x, y);
            }
        }
    }

    private void drawPlayersAtPosition(Graphics2D g2d, int cellNumber, int cellX, int cellY) {
        List<Player> playersAtPosition = new ArrayList<>();
        for (Player p : players) {
            if (p.getPosition() == cellNumber) {
                playersAtPosition.add(p);
            }
        }

        if (!playersAtPosition.isEmpty()) {
            int markerSize = 30;
            int spacing = 5;
            int startX = cellX + (CELL_SIZE - (playersAtPosition.size() * markerSize + (playersAtPosition.size() - 1) * spacing)) / 2;
            int startY = cellY + CELL_SIZE - markerSize - 10;

            for (int i = 0; i < playersAtPosition.size(); i++) {
                Player player = playersAtPosition.get(i);
                int markerX = startX + i * (markerSize + spacing);

                // Shadow
                g2d.setColor(new Color(0, 0, 0, 50));
                g2d.fillOval(markerX + 2, startY + 2, markerSize, markerSize);

                // Marker pemain
                g2d.setColor(player.getColor());
                g2d.fillOval(markerX, startY, markerSize, markerSize);

                // Border putih
                g2d.setColor(Color.WHITE);
                g2d.setStroke(new BasicStroke(3));
                g2d.drawOval(markerX, startY, markerSize, markerSize);

                // Inisial pemain
                g2d.setColor(Color.WHITE);
                g2d.setFont(new Font("Arial", Font.BOLD, 14));
                String initial = String.valueOf(player.getName().charAt(player.getName().length() - 1));
                FontMetrics fm = g2d.getFontMetrics();
                int textX = markerX + (markerSize - fm.stringWidth(initial)) / 2;
                int textY = startY + (markerSize + fm.getAscent()) / 2 - 2;
                g2d.drawString(initial, textX, textY);
            }
        }
    }
}

// Panel informasi pemain
class PlayerInfoPanel extends JPanel {
    private Queue<Player> players;
    private Player currentPlayer;

    public PlayerInfoPanel(Queue<Player> players) {
        this.players = players;
        setPreferredSize(new Dimension(300, 0));
        setBackground(new Color(250, 250, 255));
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
    }

    public void setCurrentPlayer(Player player) {
        this.currentPlayer = player;
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int y = 10;

        // Judul
        g2d.setColor(new Color(102, 126, 234));
        g2d.setFont(new Font("Arial", Font.BOLD, 20));
        g2d.drawString("📊 STATUS PEMAIN", 10, y);
        y += 40;

        // Info setiap pemain
        for (Player player : players) {
            boolean isActive = (player == currentPlayer);

            // Background untuk pemain aktif
            if (isActive) {
                g2d.setColor(new Color(102, 126, 234, 30));
                g2d.fillRoundRect(5, y - 35, 270, 70, 15, 15);

                g2d.setColor(new Color(102, 126, 234));
                g2d.setStroke(new BasicStroke(3));
                g2d.drawRoundRect(5, y - 35, 270, 70, 15, 15);
            } else {
                g2d.setColor(new Color(200, 200, 200, 50));
                g2d.fillRoundRect(5, y - 35, 270, 70, 15, 15);
            }

            // Marker warna pemain
            g2d.setColor(player.getColor());
            g2d.fillOval(15, y - 25, 40, 40);
            g2d.setColor(Color.WHITE);
            g2d.setStroke(new BasicStroke(3));
            g2d.drawOval(15, y - 25, 40, 40);

            // Nama pemain
            g2d.setColor(Color.BLACK);
            g2d.setFont(new Font("Arial", Font.BOLD, 16));
            g2d.drawString(player.getName(), 65, y - 5);

            // Posisi pemain
            g2d.setFont(new Font("Arial", Font.PLAIN, 14));
            g2d.drawString("Kotak: " + player.getPosition(), 65, y + 15);

            // Indikator giliran
            if (isActive) {
                g2d.setColor(new Color(102, 126, 234));
                g2d.setFont(new Font("Arial", Font.BOLD, 12));
                g2d.drawString("◄ GILIRAN", 200, y + 5);
            }

            y += 85;
        }
    }
}

// Panel kontrol game
class ControlPanel extends JPanel {
    private JButton rollButton;
    private JLabel diceLabel;
    private JLabel modifierLabel;
    private JLabel messageLabel;

    public ControlPanel(ActionListener rollAction) {
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setBackground(new Color(240, 245, 255));
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // Panel dadu
        JPanel dicePanel = new JPanel();
        dicePanel.setLayout(new GridLayout(2, 2, 10, 10));
        dicePanel.setBackground(new Color(240, 245, 255));
        dicePanel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(new Color(102, 126, 234), 2),
                "🎲 Hasil Dadu",
                0, 0, new Font("Arial", Font.BOLD, 14), new Color(102, 126, 234)
        ));

        JLabel diceTitle = new JLabel("Dadu Utama:");
        diceTitle.setFont(new Font("Arial", Font.PLAIN, 14));
        dicePanel.add(diceTitle);

        diceLabel = new JLabel("-", SwingConstants.CENTER);
        diceLabel.setFont(new Font("Arial", Font.BOLD, 28));
        diceLabel.setForeground(new Color(102, 126, 234));
        dicePanel.add(diceLabel);

        JLabel modTitle = new JLabel("Pengubah:");
        modTitle.setFont(new Font("Arial", Font.PLAIN, 14));
        dicePanel.add(modTitle);

        modifierLabel = new JLabel("-", SwingConstants.CENTER);
        modifierLabel.setFont(new Font("Arial", Font.BOLD, 14));
        dicePanel.add(modifierLabel);

        add(dicePanel);
        add(Box.createRigidArea(new Dimension(0, 20)));

        // Tombol lempar dadu
        rollButton = new JButton("🎲 LEMPAR DADU");
        rollButton.setFont(new Font("Arial", Font.BOLD, 18));
        rollButton.setBackground(new Color(102, 126, 234));
        rollButton.setForeground(Color.WHITE);
        rollButton.setFocusPainted(false);
        rollButton.setBorderPainted(false);
        rollButton.setPreferredSize(new Dimension(250, 60));
        rollButton.setMaximumSize(new Dimension(250, 60));
        rollButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        rollButton.addActionListener(rollAction);
        rollButton.setCursor(new Cursor(Cursor.HAND_CURSOR));

        add(rollButton);
        add(Box.createRigidArea(new Dimension(0, 20)));

        // Label pesan
        messageLabel = new JLabel("", SwingConstants.CENTER);
        messageLabel.setFont(new Font("Arial", Font.BOLD, 14));
        messageLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        messageLabel.setPreferredSize(new Dimension(250, 80));
        messageLabel.setMaximumSize(new Dimension(250, 80));
        add(messageLabel);
    }

    public void setDiceValue(int value) {
        diceLabel.setText(String.valueOf(value));
    }

    public void setModifierValue(String text, Color color) {
        if (text.equals("MAJU")) {
            modifierLabel.setText("🟢 MAJU");
            modifierLabel.setForeground(new Color(76, 175, 80));
        } else {
            modifierLabel.setText("🔴 MUNDUR");
            modifierLabel.setForeground(new Color(244, 67, 54));
        }
    }

    public void setMessage(String message, Color color) {
        messageLabel.setText("<html><div style='text-align: center;'>" + message + "</div></html>");
        messageLabel.setForeground(color);
    }

    public void setRollButtonEnabled(boolean enabled) {
        rollButton.setEnabled(enabled);
        if (!enabled) {
            rollButton.setBackground(new Color(150, 150, 150));
        } else {
            rollButton.setBackground(new Color(102, 126, 234));
        }
    }

    public void resetDisplay() {
        diceLabel.setText("-");
        modifierLabel.setText("-");
        modifierLabel.setForeground(Color.BLACK);
        messageLabel.setText("");
    }
}

// Kelas utama Game dengan GUI
public class SnakeLadderGame extends JFrame {
    private GameBoard board;
    private Queue<Player> playerQueue;
    private Dice dice;
    private BoardPanel boardPanel;
    private PlayerInfoPanel playerInfoPanel;
    private ControlPanel controlPanel;
    private Player currentPlayer;

    private static final Color[] PLAYER_COLORS = {
            new Color(255, 107, 107),  // Merah
            new Color(78, 205, 196),    // Cyan
            new Color(255, 217, 61),    // Kuning
            new Color(149, 225, 211),   // Mint
            new Color(255, 159, 243),   // Pink
            new Color(118, 181, 197),   // Biru Muda
            new Color(255, 138, 101),   // Orange
            new Color(162, 155, 254),   // Ungu
            new Color(108, 204, 119),   // Hijau
            new Color(255, 195, 113)    // Peach
    };

    public SnakeLadderGame(int numPlayers) {
        this.board = new GameBoard(8, 8);
        this.dice = new Dice();
        this.playerQueue = new LinkedList<>();

        // Inisialisasi pemain dengan warna yang berbeda
        for (int i = 1; i <= numPlayers; i++) {
            Color playerColor = PLAYER_COLORS[(i - 1) % PLAYER_COLORS.length];
            playerQueue.offer(new Player("Pemain " + i, playerColor));
        }

        setupUI();
    }

    private void setupUI() {
        setTitle("🎲 Permainan Ular Tangga 8x8 🐍");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout(10, 10));
        getContentPane().setBackground(new Color(240, 240, 255));

        // Header
        JPanel headerPanel = new JPanel();
        headerPanel.setBackground(new Color(102, 126, 234));
        headerPanel.setPreferredSize(new Dimension(0, 80));
        JLabel titleLabel = new JLabel("🎲 ULAR TANGGA 8×8 🐍");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 32));
        titleLabel.setForeground(Color.WHITE);
        headerPanel.add(titleLabel);
        add(headerPanel, BorderLayout.NORTH);

        // Panel tengah
        JPanel centerPanel = new JPanel(new BorderLayout(10, 10));
        centerPanel.setBackground(new Color(240, 240, 255));
        centerPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Papan permainan
        boardPanel = new BoardPanel(board, playerQueue);
        centerPanel.add(boardPanel, BorderLayout.CENTER);

        // Panel kanan (info pemain + kontrol)
        JPanel rightPanel = new JPanel();
        rightPanel.setLayout(new BoxLayout(rightPanel, BoxLayout.Y_AXIS));
        rightPanel.setBackground(new Color(240, 240, 255));

        playerInfoPanel = new PlayerInfoPanel(playerQueue);
        controlPanel = new ControlPanel(e -> performTurn());

        rightPanel.add(playerInfoPanel);
        rightPanel.add(Box.createRigidArea(new Dimension(0, 20)));
        rightPanel.add(controlPanel);

        centerPanel.add(rightPanel, BorderLayout.EAST);
        add(centerPanel, BorderLayout.CENTER);

        pack();
        setLocationRelativeTo(null);
        setResizable(false);

        // Mulai giliran pertama
        nextPlayer();
    }

    private void nextPlayer() {
        currentPlayer = playerQueue.peek();
        playerInfoPanel.setCurrentPlayer(currentPlayer);
        controlPanel.resetDisplay();
        controlPanel.setMessage("Giliran " + currentPlayer.getName() + "!", new Color(102, 126, 234));
    }

    private void performTurn() {
        controlPanel.setRollButtonEnabled(false);

        // Lempar dadu dengan animasi
        Timer animationTimer = new Timer(100, null);
        final int[] count = {0};

        animationTimer.addActionListener(e -> {
            if (count[0] < 10) {
                controlPanel.setDiceValue((int)(Math.random() * 6) + 1);
                count[0]++;
            } else {
                animationTimer.stop();
                executeTurn();
            }
        });
        animationTimer.start();
    }

    private void executeTurn() {
        int mainDice = dice.rollMainDice();
        int modifierDice = dice.rollModifierDice();

        controlPanel.setDiceValue(mainDice);

        String modifierText = (modifierDice == 1) ? "MAJU" : "MUNDUR";
        Color modifierColor = (modifierDice == 1) ? new Color(76, 175, 80) : new Color(244, 67, 54);
        controlPanel.setModifierValue(modifierText, modifierColor);

        int steps = mainDice * modifierDice;
        int oldPosition = currentPlayer.getPosition();
        int newPosition = oldPosition + steps;

        if (newPosition < 1) newPosition = 1;

        currentPlayer.setPosition(newPosition);

        String movement = (steps > 0) ? "maju" : "mundur";
        String message = String.format("%s %s %d langkah<br>%d → %d",
                currentPlayer.getName(), movement, Math.abs(steps), oldPosition, newPosition);
        controlPanel.setMessage(message, Color.BLACK);

        boardPanel.repaint();
        playerInfoPanel.repaint();

        // Cek pemenang
        if (currentPlayer.hasWon(board.getTotalSquares())) {
            Timer winTimer = new Timer(1000, e -> showWinner());
            winTimer.setRepeats(false);
            winTimer.start();
        } else {
            // Pindah ke pemain berikutnya
            Timer nextTimer = new Timer(2000, e -> {
                playerQueue.offer(playerQueue.poll());
                nextPlayer();
                controlPanel.setRollButtonEnabled(true);
            });
            nextTimer.setRepeats(false);
            nextTimer.start();
        }
    }

    private void showWinner() {
        String message = String.format(
                "🏆 SELAMAT! 🏆\n\n%s MENANG!\n\nPosisi Akhir: Kotak %d",
                currentPlayer.getName(), currentPlayer.getPosition()
        );

        JOptionPane.showMessageDialog(
                this,
                message,
                "Permainan Selesai",
                JOptionPane.INFORMATION_MESSAGE
        );

        int choice = JOptionPane.showConfirmDialog(
                this,
                "Mau main lagi?",
                "Main Lagi?",
                JOptionPane.YES_NO_OPTION
        );

        if (choice == JOptionPane.YES_OPTION) {
            dispose();
            showSetupDialog();
        } else {
            System.exit(0);
        }
    }

    private static void showSetupDialog() {
        JDialog setupDialog = new JDialog();
        setupDialog.setTitle("Setup Permainan");
        setupDialog.setModal(true);
        setupDialog.setLayout(new BorderLayout());
        setupDialog.setSize(450, 350);
        setupDialog.setLocationRelativeTo(null);

        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.setBorder(BorderFactory.createEmptyBorder(30, 30, 30, 30));
        contentPanel.setBackground(Color.WHITE);

        JLabel titleLabel = new JLabel("🎲 Ular Tangga 8×8 🐍");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 28));
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        titleLabel.setForeground(new Color(102, 126, 234));
        contentPanel.add(titleLabel);
        contentPanel.add(Box.createRigidArea(new Dimension(0, 30)));

        JLabel instructionLabel = new JLabel("Masukkan jumlah pemain (2-10):");
        instructionLabel.setFont(new Font("Arial", Font.PLAIN, 16));
        instructionLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        contentPanel.add(instructionLabel);
        contentPanel.add(Box.createRigidArea(new Dimension(0, 15)));

        // Spinner untuk jumlah pemain
        SpinnerNumberModel spinnerModel = new SpinnerNumberModel(2, 2, 10, 1);
        JSpinner playerSpinner = new JSpinner(spinnerModel);
        playerSpinner.setFont(new Font("Arial", Font.BOLD, 24));
        ((JSpinner.DefaultEditor) playerSpinner.getEditor()).getTextField().setHorizontalAlignment(JTextField.CENTER);
        playerSpinner.setMaximumSize(new Dimension(100, 50));
        playerSpinner.setAlignmentX(Component.CENTER_ALIGNMENT);
        contentPanel.add(playerSpinner);
        contentPanel.add(Box.createRigidArea(new Dimension(0, 25)));

        // Tombol mulai
        JButton startButton = new JButton("🎮 MULAI PERMAINAN");
        startButton.setFont(new Font("Arial", Font.BOLD, 16));
        startButton.setBackground(new Color(102, 126, 234));
        startButton.setForeground(Color.WHITE);
        startButton.setFocusPainted(false);
        startButton.setBorderPainted(false);
        startButton.setPreferredSize(new Dimension(250, 50));
        startButton.setMaximumSize(new Dimension(250, 50));
        startButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        startButton.setCursor(new Cursor(Cursor.HAND_CURSOR));

        startButton.addActionListener(e -> {
            int numPlayers = (Integer) playerSpinner.getValue();
            setupDialog.dispose();
            SwingUtilities.invokeLater(() -> {
                SnakeLadderGame game = new SnakeLadderGame(numPlayers);
                game.setVisible(true);
            });
        });

        contentPanel.add(startButton);

        setupDialog.add(contentPanel, BorderLayout.CENTER);
        setupDialog.setVisible(true);
    }

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }

        SwingUtilities.invokeLater(() -> showSetupDialog());
    }
}