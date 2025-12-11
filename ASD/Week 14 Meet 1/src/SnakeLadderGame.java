import java.util.*;

// Kelas untuk merepresentasikan Pemain
class Player {
    private String name;
    private int position;

    public Player(String name) {
        this.name = name;
        this.position = 1; // Mulai dari kotak 1
    }

    public String getName() {
        return name;
    }

    public int getPosition() {
        return position;
    }

    public void setPosition(int position) {
        // Pastikan posisi tidak kurang dari 1
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
        // Membangun graf sederhana: setiap kotak terhubung ke kotak berikutnya
        for (int i = 1; i <= totalSquares; i++) {
            adjacencyList.put(i, new ArrayList<>());
            if (i < totalSquares) {
                adjacencyList.get(i).add(i + 1);
            }
        }
    }

    public int getTotalSquares() {
        return totalSquares;
    }

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

    // Dadu Utama: menghasilkan nilai 1-6
    public int rollMainDice() {
        return random.nextInt(6) + 1;
    }

    // Dadu Pengubah: 80% positif (maju), 20% negatif (mundur)
    public int rollModifierDice() {
        double probability = Math.random();
        return (probability < 0.80) ? 1 : -1; // 1 untuk maju, -1 untuk mundur
    }

    public String getModifierColor(int modifier) {
        return (modifier == 1) ? "HIJAU (Maju)" : "MERAH (Mundur)";
    }
}

// Kelas utama Game
public class SnakeLadderGame {
    private GameBoard board;
    private Queue<Player> playerQueue;
    private Dice dice;
    private int targetPosition;

    // ANSI Color Codes untuk output konsol
    private static final String GREEN = "\u001B[32m";
    private static final String RED = "\u001B[31m";
    private static final String YELLOW = "\u001B[33m";
    private static final String BLUE = "\u001B[34m";
    private static final String RESET = "\u001B[0m";
    private static final String BOLD = "\u001B[1m";

    public SnakeLadderGame(int numPlayers) {
        this.board = new GameBoard(8, 8);
        this.targetPosition = board.getTotalSquares();
        this.dice = new Dice();
        this.playerQueue = new LinkedList<>();

        // Inisialisasi pemain
        for (int i = 1; i <= numPlayers; i++) {
            playerQueue.offer(new Player("Pemain " + i));
        }
    }

    public void displayWelcome() {
        System.out.println(BOLD + BLUE + "╔════════════════════════════════════════╗");
        System.out.println("║   🎲 PERMAINAN ULAR TANGGA 8x8 🐍    ║");
        System.out.println("╚════════════════════════════════════════╝" + RESET);
        System.out.println("Target: Mencapai kotak ke-" + targetPosition);
        System.out.println("Jumlah Pemain: " + playerQueue.size());
        System.out.println("════════════════════════════════════════\n");
    }

    public void displayBoard(Player currentPlayer) {
        System.out.println(YELLOW + "┌─────── Status Papan ───────┐" + RESET);
        for (Player p : playerQueue) {
            String marker = (p == currentPlayer) ? " ◄──" : "";
            System.out.println("  " + p.getName() + ": Kotak " + p.getPosition() + marker);
        }
        System.out.println(YELLOW + "└────────────────────────────┘" + RESET);
        System.out.println();
    }

    public void play() {
        displayWelcome();
        int turnNumber = 0;
        Player winner = null;

        while (winner == null) {
            turnNumber++;
            System.out.println(BOLD + "═══ GILIRAN #" + turnNumber + " ═══" + RESET);

            // Ambil pemain dari antrian
            Player currentPlayer = playerQueue.poll();
            displayBoard(currentPlayer);

            System.out.println(BLUE + "→ " + currentPlayer.getName() + " melempar dadu..." + RESET);

            // Lempar dadu
            int mainDiceValue = dice.rollMainDice();
            int modifierDiceValue = dice.rollModifierDice();
            String modifierColor = dice.getModifierColor(modifierDiceValue);

            // Tampilkan hasil dadu
            System.out.println("  🎲 Dadu Utama: " + mainDiceValue);
            String colorCode = (modifierDiceValue == 1) ? GREEN : RED;
            System.out.println("  🎲 Dadu Pengubah: " + colorCode + modifierColor + RESET);

            // Hitung langkah
            int steps = mainDiceValue * modifierDiceValue;
            int oldPosition = currentPlayer.getPosition();
            int newPosition = oldPosition + steps;

            // Batasi posisi minimum ke 1
            if (newPosition < 1) {
                newPosition = 1;
            }

            currentPlayer.setPosition(newPosition);

            // Tampilkan pergerakan
            String movement = (steps > 0) ? "maju" : "mundur";
            System.out.println("  📍 Pergerakan: " + movement + " " + Math.abs(steps) + " langkah");
            System.out.println("  📊 Posisi: " + oldPosition + " → " + BOLD + currentPlayer.getPosition() + RESET);

            // Cek pemenang
            if (currentPlayer.hasWon(targetPosition)) {
                winner = currentPlayer;
            } else {
                // Kembalikan pemain ke antrian
                playerQueue.offer(currentPlayer);
            }

            System.out.println();

            // Jeda untuk keterbacaan (opsional, bisa dikomentari)
            try {
                Thread.sleep(800);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }

        // Tampilkan pemenang
        displayWinner(winner, turnNumber);
    }

    private void displayWinner(Player winner, int totalTurns) {
        System.out.println(BOLD + GREEN + "╔════════════════════════════════════════╗");
        System.out.println("║          🏆 PERMAINAN SELESAI 🏆      ║");
        System.out.println("╚════════════════════════════════════════╝" + RESET);
        System.out.println(BOLD + GREEN + "🎉 PEMENANG: " + winner.getName() + " 🎉" + RESET);
        System.out.println("Posisi Akhir: Kotak " + winner.getPosition());
        System.out.println("Total Giliran: " + totalTurns);
        System.out.println("\n" + YELLOW + "Terima kasih telah bermain!" + RESET);
    }

    // Main method untuk menjalankan game
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        System.out.print("Masukkan jumlah pemain (2-4): ");
        int numPlayers = scanner.nextInt();

        // Validasi jumlah pemain
        if (numPlayers < 2 || numPlayers > 4) {
            System.out.println("Jumlah pemain harus antara 2 dan 4!");
            scanner.close();
            return;
        }

        SnakeLadderGame game = new SnakeLadderGame(numPlayers);
        game.play();

        scanner.close();
    }
}