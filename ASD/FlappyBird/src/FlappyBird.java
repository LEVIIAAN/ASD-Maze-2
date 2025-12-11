import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.Random;
import javax.swing.*;

public class FlappyBird extends JPanel implements ActionListener, KeyListener {

    // --- KONFIGURASI GAME ---
    int boardWidth = 360;
    int boardHeight = 640;

    // Aset Gambar (Dibuat prosedural/coding agar tidak perlu file eksternal)
    // Kita menggunakan warna dan bentuk untuk merepresentasikan burung dan pipa

    // Logika Burung
    int birdX = boardWidth / 8;
    int birdY = boardHeight / 2;
    int birdWidth = 34;
    int birdHeight = 24;

    class Bird {
        int x = birdX;
        int y = birdY;
        int width = birdWidth;
        int height = birdHeight;
        Image img;

        Bird(Image img) {
            this.img = img;
        }
    }

    // Logika Pipa
    class Pipe {
        int x = boardWidth;
        int y = 0;
        int width = 64;
        int height = 512;
        Image img;
        boolean passed = false; // Cek apakah burung sudah melewati pipa ini

        Pipe(Image img) {
            this.img = img;
        }
    }

    // Variabel Game
    Bird bird;
    int velocityX = -4; // Kecepatan pipa bergerak ke kiri
    int velocityY = 0;  // Kecepatan burung (naik/turun)
    int gravity = 1;

    ArrayList<Pipe> pipes;
    Random random = new Random();

    Timer gameLoop;
    Timer placePipesTimer;
    boolean gameOver = false;
    double score = 0;

    // Constructor
    public FlappyBird() {
        setPreferredSize(new Dimension(boardWidth, boardHeight));
        setBackground(Color.CYAN); // Warna langit dasar jika gradasi gagal
        setFocusable(true);
        addKeyListener(this);

        bird = new Bird(null); // Kita akan menggambar manual di paintComponent
        pipes = new ArrayList<Pipe>();

        // Timer untuk menempatkan pipa setiap 1.5 detik
        placePipesTimer = new Timer(1500, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                placePipes();
            }
        });
        placePipesTimer.start();

        // Game Loop (update 60 kali per detik)
        gameLoop = new Timer(1000/60, this);
        gameLoop.start();
    }

    void placePipes() {
        // Posisi Y pipa acak
        // Pipa atas (y bergeser ke atas antara -pipeHeight/4 sampai -pipeHeight/2)
        int randomPipeY = (int) (0 - 512/4 - Math.random()*(512/2));
        int openingSpace = boardHeight / 4;

        Pipe topPipe = new Pipe(null);
        topPipe.y = randomPipeY;
        pipes.add(topPipe);

        Pipe bottomPipe = new Pipe(null);
        bottomPipe.y = topPipe.y + 512 + openingSpace;
        pipes.add(bottomPipe);
    }

    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        draw(g);
    }

    public void draw(Graphics g) {
        Graphics2D g2d = (Graphics2D) g;

        // Mengaktifkan Anti-Aliasing agar grafis halus
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // 1. Background (Langit dengan Gradasi)
        GradientPaint skyGradient = new GradientPaint(0, 0, new Color(112, 197, 206), 0, boardHeight, new Color(185, 235, 240));
        g2d.setPaint(skyGradient);
        g2d.fillRect(0, 0, boardWidth, boardHeight);

        // 2. Gambar Burung (Bentuk Oval Kuning dengan Outline)
        g2d.setColor(new Color(255, 215, 0)); // Emas/Kuning
        g2d.fillOval(bird.x, bird.y, bird.width, bird.height);
        g2d.setColor(Color.BLACK); // Outline
        g2d.setStroke(new BasicStroke(2));
        g2d.drawOval(bird.x, bird.y, bird.width, bird.height);

        // Mata Burung
        g2d.setColor(Color.WHITE);
        g2d.fillOval(bird.x + 24, bird.y + 4, 8, 8);
        g2d.setColor(Color.BLACK);
        g2d.fillOval(bird.x + 28, bird.y + 6, 2, 2);

        // Sayap
        g2d.setColor(new Color(255, 255, 220));
        g2d.fillOval(bird.x + 5, bird.y + 12, 16, 8);

        // 3. Gambar Pipa
        for (int i = 0; i < pipes.size(); i++) {
            Pipe pipe = pipes.get(i);

            // Warna Pipa Hijau
            g2d.setColor(new Color(115, 191, 46));
            g2d.fillRect(pipe.x, pipe.y, pipe.width, pipe.height);

            // Detail Pipa (Outline & Cap)
            g2d.setColor(new Color(85, 144, 34)); // Hijau tua untuk border
            g2d.setStroke(new BasicStroke(3));
            g2d.drawRect(pipe.x, pipe.y, pipe.width, pipe.height);

            // Efek Highlight Pipa (biar agak 3D)
            g2d.setColor(new Color(165, 230, 90, 100));
            g2d.fillRect(pipe.x + 5, pipe.y, 10, pipe.height);
        }

        // 4. Skor
        g2d.setColor(Color.WHITE);
        g2d.setFont(new Font("Arial", Font.BOLD, 32));
        if (gameOver) {
            // Tampilan Game Over
            g2d.setColor(new Color(0, 0, 0, 150)); // Overlay gelap
            g2d.fillRect(0, 0, boardWidth, boardHeight);

            g2d.setColor(Color.WHITE);
            g2d.drawString("Game Over", 85, 300);
            g2d.drawString("Score: " + (int) score, 110, 340);

            g2d.setFont(new Font("Arial", Font.PLAIN, 16));
            g2d.drawString("Tekan SPASI untuk Restart", 85, 380);
        } else {
            // Tampilan Skor Saat Main
            g2d.drawString(String.valueOf((int) score), 10, 35);
        }
    }

    public void move() {
        // Logika Burung
        velocityY += gravity;
        bird.y += velocityY;
        bird.y = Math.max(bird.y, 0); // Tidak bisa terbang lebih tinggi dari atap

        // Logika Pipa
        for (int i = 0; i < pipes.size(); i++) {
            Pipe pipe = pipes.get(i);
            pipe.x += velocityX;

            // Cek tabrakan
            if (collision(bird, pipe)) {
                gameOver = true;
            }

            // Skor bertambah jika lewat pipa
            if (!pipe.passed && bird.x > pipe.x + pipe.width) {
                score += 0.5; // 0.5 karena ada 2 pipa (atas & bawah), total jadi 1
                pipe.passed = true;
            }
        }

        // Jatuh ke tanah
        if (bird.y > boardHeight) {
            gameOver = true;
        }
    }

    // Deteksi Tabrakan Sederhana (AABB Collision)
    boolean collision(Bird a, Pipe b) {
        return a.x < b.x + b.width &&   // Kiri a < Kanan b
                a.x + a.width > b.x &&   // Kanan a > Kiri b
                a.y < b.y + b.height &&  // Atas a < Bawah b
                a.y + a.height > b.y;    // Bawah a > Atas b
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        move();
        repaint(); // Memanggil paintComponent ulang
        if (gameOver) {
            placePipesTimer.stop();
            gameLoop.stop();
        }
    }

    @Override
    public void keyPressed(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_SPACE) {
            if (gameOver) {
                // Restart Game
                bird.y = birdY;
                velocityY = 0;
                pipes.clear();
                score = 0;
                gameOver = false;
                gameLoop.start();
                placePipesTimer.start();
            } else {
                // Lompat
                velocityY = -9;
            }
        }
    }

    @Override
    public void keyTyped(KeyEvent e) {}

    @Override
    public void keyReleased(KeyEvent e) {}

    // --- MAIN METHOD (ENTRY POINT) ---
    public static void main(String[] args) {
        JFrame frame = new JFrame("Flappy Bird Java");
        FlappyBird game = new FlappyBird();

        frame.add(game);
        frame.pack(); // Sesuaikan ukuran frame dengan panel
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLocationRelativeTo(null); // Tengah layar
        frame.setResizable(false);
        frame.setVisible(true);

        game.requestFocus(); // Agar keyboard langsung terdeteksi
    }
}