import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import javax.imageio.ImageIO;

public class CowboyTheme {

    // --- PALET WARNA COWBOY ---
    public static final Color BG_SAND     = new Color(210, 180, 140); // Warna Pasir/Kertas Tua (Cadangan)
    public static final Color WOOD_DARK   = new Color(80, 50, 20);   // Kayu Gelap (Border/Header)
    public static final Color WOOD_MEDIUM = new Color(120, 80, 40);  // Kayu Sedang (Panel Kanan)
    public static final Color WOOD_LIGHT  = new Color(160, 110, 60);  // Kayu Terang (Tombol/Highlight)
    public static final Color GOLD_NUGGET = new Color(218, 165, 32);  // Emas (Aksen/Judul)
    public static final Color TEXT_CREAM  = new Color(245, 235, 220); // Teks Krem (Agar terbaca di latar gelap)
    public static final Color RED_BANDANA = new Color(178, 34, 34);   // Merah (Ular/Bahaya)
    public static final Color BLUE_DENIM  = new Color(25, 25, 112);   // Biru Jeans (Tangga/Aman)
    public static final Color CACTUS_GREEN= new Color(34, 139, 34);   // Hijau Kaktus (Start/Aman)

    // Warna Pemain (Earthy Tones - 10 Pemain) - TETAP SAMA
    public static final Color[] PLAYER_COLORS = {
            new Color(139, 69, 19),   // Saddle Brown
            new Color(205, 92, 92),   // Indian Red
            new Color(85, 107, 47),   // Olive Drab
            new Color(218, 165, 32),  // Goldenrod
            new Color(70, 130, 180),  // Steel Blue
            new Color(128, 0, 0),     // Maroon
            new Color(47, 79, 79),    // Dark Slate Gray
            new Color(244, 164, 96),  // Sandy Brown
            new Color(112, 128, 144), // Slate Gray
            new Color(188, 143, 143)  // Rosy Brown
    };

    // Font: Gaya Western / Wanted Poster - TETAP SAMA
    public static final Font FONT_TITLE = new Font("Rockwell", Font.BOLD, 36);
    public static final Font FONT_TEXT  = new Font("Courier New", Font.BOLD, 14);
    public static final Font FONT_NUM   = new Font("Georgia", Font.BOLD, 14);

    // --- GAMBAR BACKGROUND (PETA) - TETAP SAMA ---
    public static BufferedImage bgImage;
    static {
        try {
            bgImage = ImageIO.read(new File("cowboy_map.jpg")); // Pastikan nama file benar
        } catch (Exception e) {
            System.out.println("Gagal memuat gambar background peta. Menggunakan warna solid.");
        }
    }

    // --- HELPER METHODS: VISUAL COWBOY ---

    // 1. Menggambar Tali Tambang (Untuk Tangga/Link Aman) - TETAP SAMA
    public static void drawRope(Graphics2D g2, int x1, int y1, int x2, int y2) {
        // ... (Kode sama seperti sebelumnya) ...
        g2.setColor(new Color(210, 180, 140));
        g2.setStroke(new BasicStroke(6, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g2.drawLine(x1, y1, x2, y2);
        g2.setColor(WOOD_DARK);
        g2.setStroke(new BasicStroke(2, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 0, new float[]{5, 5}, 0));
        g2.drawLine(x1, y1, x2, y2);
    }

    // 2. Menggambar Ular Berbisa (Garis Meliuk untuk Link Bahaya) - TETAP SAMA
    public static void drawSnakeLine(Graphics2D g2, int x1, int y1, int x2, int y2) {
        // ... (Kode sama seperti sebelumnya) ...
        g2.setColor(new Color(0,0,0,50));
        g2.setStroke(new BasicStroke(8, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g2.drawLine(x1+2, y1+2, x2+2, y2+2);
        g2.setColor(RED_BANDANA);
        g2.setStroke(new BasicStroke(6, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g2.drawLine(x1, y1, x2, y2);
        g2.setColor(Color.BLACK);
        g2.setStroke(new BasicStroke(1, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 0, new float[]{3, 6}, 0));
        g2.drawLine(x1, y1, x2, y2);
    }

    // 3. Menggambar Lencana Sheriff (Bintang) - TETAP SAMA
    public static void drawSheriffStar(Graphics2D g2, int cx, int cy, int size) {
        // ... (Kode sama seperti sebelumnya) ...
        int[] xp = {cx, cx+4, cx+15, cx+6, cx+10, cx, cx-10, cx-6, cx-15, cx-4};
        int[] yp = {cy-15, cy-5, cy-5, cy+5, cy+15, cy+8, cy+15, cy+5, cy-5, cy-5};
        double scale = size / 15.0;
        for(int i=0; i<xp.length; i++) xp[i] = cx + (int)((xp[i]-cx)*scale);
        for(int i=0; i<yp.length; i++) yp[i] = cy + (int)((yp[i]-cy)*scale);
        g2.setColor(GOLD_NUGGET);
        g2.fillPolygon(xp, yp, 10);
        g2.setColor(WOOD_DARK);
        g2.setStroke(new BasicStroke(2));
        g2.drawPolygon(xp, yp, 10);
        for(int i=0; i<10; i+=2) g2.fillOval(xp[i]-2, yp[i]-2, 4, 4);
    }

    // --- BARU: Helper untuk Menggambar Latar Belakang Kayu ---
    public static void drawWoodBackground(Graphics2D g2, int x, int y, int w, int h, Color baseColor) {
        g2.setColor(baseColor);
        g2.fillRect(x, y, w, h);

        // Efek Serat Kayu Sederhana
        g2.setColor(new Color(0, 0, 0, 30));
        for (int i = y; i < y + h; i += 10) {
            int startX = x + (int)(Math.random() * 20);
            int endX = x + w - (int)(Math.random() * 20);
            int flicker = (int)(Math.random() * 5);
            g2.drawLine(startX, i + flicker, endX, i + flicker);
        }
    }

    // --- BARU: Helper untuk Menggambar Bingkai Kayu ---
    public static void drawWoodBorder(Graphics2D g2, int x, int y, int w, int h) {
        int borderThickness = 8;
        g2.setColor(WOOD_DARK);
        g2.fillRect(x, y, w, borderThickness); // Atas
        g2.fillRect(x, y + h - borderThickness, w, borderThickness); // Bawah
        g2.fillRect(x, y, borderThickness, h); // Kiri
        g2.fillRect(x + w - borderThickness, y, borderThickness, h); // Kanan

        // Aksen Paku di Sudut
        g2.setColor(Color.DARK_GRAY);
        int nailSize = 6;
        g2.fillOval(x + 2, y + 2, nailSize, nailSize);
        g2.fillOval(x + w - borderThickness + 2, y + 2, nailSize, nailSize);
        g2.fillOval(x + 2, y + h - borderThickness + 2, nailSize, nailSize);
        g2.fillOval(x + w - borderThickness + 2, y + h - borderThickness + 2, nailSize, nailSize);
    }

    // --- TOMBOL KAYU (Diperbarui Warna Teks) ---
    public static class Button extends JButton {
        private boolean isHovered = false;

        public Button(String text) {
            super(text);
            setContentAreaFilled(false);
            setFocusPainted(false);
            setBorderPainted(false);
            setFont(FONT_TEXT.deriveFont(Font.BOLD, 16f));
            setForeground(TEXT_CREAM); // Ganti warna teks jadi krem
            setCursor(new Cursor(Cursor.HAND_CURSOR));

            addMouseListener(new MouseAdapter() {
                @Override public void mouseEntered(MouseEvent e) { isHovered = true; repaint(); }
                @Override public void mouseExited(MouseEvent e) { isHovered = false; repaint(); }
            });
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            Color c = isHovered ? WOOD_LIGHT.brighter() : WOOD_LIGHT;

            // Gambar Papan Kayu
            g2.setColor(WOOD_DARK); // Border
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), 15, 15);
            g2.setColor(c); // Isi Kayu
            g2.fillRoundRect(3, 3, getWidth()-6, getHeight()-6, 15, 15);

            // Efek Serat Kayu Sederhana
            g2.setColor(new Color(0,0,0,40));
            g2.drawLine(10, 10, getWidth()-10, 10);
            g2.drawLine(10, getHeight()-10, getWidth()-10, getHeight()-10);

            // Paku di sudut
            g2.setColor(Color.DARK_GRAY);
            int nailSize = 6;
            g2.fillOval(5, 5, nailSize, nailSize);
            g2.fillOval(getWidth()-5-nailSize, 5, nailSize, nailSize);
            g2.fillOval(5, getHeight()-5-nailSize, nailSize, nailSize);
            g2.fillOval(getWidth()-5-nailSize, getHeight()-5-nailSize, nailSize, nailSize);

            super.paintComponent(g);
        }
    }
}