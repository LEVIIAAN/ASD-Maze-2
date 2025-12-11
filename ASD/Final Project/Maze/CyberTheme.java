import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class CyberTheme {

    // ==================================================================================
    // TEMA: SYNTHWAVE VIOLET (BEST CHOICE)
    // ==================================================================================

    // Background utama: Ungu gelap pekat (bukan hitam mati)
    public static final Color BG_DARK = new Color(25, 10, 40);

    // Panel transparan: Ungu sedikit lebih terang agar kontras dengan background
    public static final Color BG_PANEL = new Color(45, 20, 70, 200);

    // Aksen utama untuk Hover/Highlight: Magenta Pink
    public static final Color ACCENT_MAIN = new Color(255, 0, 200);

    // ==================================================================================
    // WARNA NEON & FONT
    // ==================================================================================

    public static final Color NEON_CYAN   = new Color(0, 255, 255);
    public static final Color NEON_PINK   = new Color(255, 50, 150);
    public static final Color NEON_YELLOW = new Color(255, 220, 50);
    public static final Color NEON_PURPLE = new Color(180, 50, 255);
    public static final Color NEON_GREEN  = new Color(50, 255, 100);

    // Font Configuration
    public static final Font FONT_TITLE = new Font("Impact", Font.ITALIC, 32);
    public static final Font FONT_TEXT  = new Font("Verdana", Font.BOLD, 12);
    public static final Font FONT_NUM   = new Font("Consolas", Font.BOLD, 14);

    // ==================================================================================
    // HELPER METHODS: EFEK GLOW (CAHAYA)
    // ==================================================================================

    // 1. Menggambar Garis Neon (untuk Grid & Link)
    public static void drawGlowingLine(Graphics2D g2, int x1, int y1, int x2, int y2, Color c, int thickness) {
        // Layer 1: Glow Luar (Lebar, Sangat Transparan)
        g2.setColor(new Color(c.getRed(), c.getGreen(), c.getBlue(), 30));
        g2.setStroke(new BasicStroke(thickness + 8, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g2.drawLine(x1, y1, x2, y2);

        // Layer 2: Glow Tengah (Agak Transparan)
        g2.setColor(new Color(c.getRed(), c.getGreen(), c.getBlue(), 80));
        g2.setStroke(new BasicStroke(thickness + 4, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g2.drawLine(x1, y1, x2, y2);

        // Layer 3: Inti Garis (Solid, Terang)
        g2.setColor(c);
        g2.setStroke(new BasicStroke(thickness, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g2.drawLine(x1, y1, x2, y2);
    }

    // 2. Menggambar Oval/Lingkaran Neon (untuk Player & Koin)
    public static void drawGlowingOval(Graphics2D g2, int x, int y, int w, int h, Color c) {
        // Aura Glow di sekeliling
        g2.setColor(new Color(c.getRed(), c.getGreen(), c.getBlue(), 50));
        g2.fillOval(x - 6, y - 6, w + 12, h + 12);

        // Lingkaran Utama
        g2.setColor(c);
        g2.fillOval(x, y, w, h);

        // Highlight Kilap Putih (Efek Kaca/3D)
        g2.setColor(new Color(255, 255, 255, 180));
        g2.fillOval(x + w/4, y + h/5, w/2, h/3);
    }

    // 3. Menggambar Polygon Neon (untuk Bintang)
    public static void drawGlowingPolygon(Graphics2D g2, Polygon p, Color c) {
        // Glow Luar (Stroke Tebal)
        g2.setColor(new Color(c.getRed(), c.getGreen(), c.getBlue(), 60));
        g2.setStroke(new BasicStroke(5));
        g2.drawPolygon(p);

        // Isi Dalam (Transparan)
        g2.setColor(new Color(c.getRed(), c.getGreen(), c.getBlue(), 150));
        g2.fillPolygon(p);

        // Garis Tepi Tajam (Putih/Terang)
        g2.setColor(Color.WHITE);
        g2.setStroke(new BasicStroke(2));
        g2.drawPolygon(p);
    }

    // ==================================================================================
    // CUSTOM BUTTON CLASS (TOMBOL FUTURISTIK)
    // ==================================================================================
    public static class Button extends JButton {
        private boolean isHovered = false;
        private final Color baseColor;

        public Button(String text, Color color) {
            super(text);
            this.baseColor = color;

            // Hilangkan style bawaan Java Swing
            setContentAreaFilled(false);
            setFocusPainted(false);
            setBorderPainted(false);

            setFont(FONT_TEXT);
            setForeground(color);
            setCursor(new Cursor(Cursor.HAND_CURSOR));

            // Mouse Listener untuk efek Hover
            addMouseListener(new MouseAdapter() {
                @Override
                public void mouseEntered(MouseEvent e) { isHovered = true; repaint(); }
                @Override
                public void mouseExited(MouseEvent e) { isHovered = false; repaint(); }
            });
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            if (isHovered) {
                // Saat Hover: Gradient Background dari Warna Base ke Aksen Pink
                GradientPaint gp = new GradientPaint(0, 0, baseColor, getWidth(), 0, ACCENT_MAIN);
                g2.setPaint(gp);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 15, 15);

                // Teks jadi putih & tebal agar terbaca jelas
                setForeground(Color.WHITE);
                setFont(FONT_TEXT.deriveFont(Font.BOLD));

                // Efek "Glitch" / Garis Tech hitam di tengah
                g2.setColor(new Color(0,0,0,50));
                g2.fillRect(5, getHeight()/2, getWidth()-10, 2);

            } else {
                // Saat Normal: Hanya Border Neon
                g2.setColor(baseColor);
                g2.setStroke(new BasicStroke(2));
                g2.drawRoundRect(1, 1, getWidth()-3, getHeight()-3, 15, 15);

                // Reset warna teks ke warna neon asli
                setForeground(baseColor);
                setFont(FONT_TEXT);

                // Hiasan Tech di bawah tombol
                g2.fillRect(15, getHeight()-5, getWidth()-30, 2);
            }
            super.paintComponent(g);
        }
    }
}