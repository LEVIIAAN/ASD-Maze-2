import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.*;
import java.util.List;

public class PlayerPanel extends JPanel {
    private final Stack<Player> players;
    private Player current;

    public PlayerPanel(Stack<Player> players) {
        this.players = players;

        // Agar background transparan (mengikuti warna pasir di main frame)
        setOpaque(false);

        // Warna background panel (opsional, tapi kita set transparan di atas)
        setBackground(CowboyTheme.BG_SAND);
        setBorder(new EmptyBorder(20, 20, 20, 20));
    }

    public void setCurrent(Player p) {
        this.current = p;
        repaint();
    }

    // Menghitung tinggi panel agar ScrollPane berfungsi
    @Override
    public Dimension getPreferredSize() {
        int cardHeight = 90;
        int headerHeight = 50;
        int totalHeight = headerHeight + (players.size() * cardHeight) + 30;
        return new Dimension(380, Math.max(250, totalHeight));
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // 1. GAMBAR BACKGROUND KERTAS TUA / PAPAN
        g2.setColor(new Color(255, 248, 220, 150)); // Cornsilk transparan
        g2.fillRoundRect(0, 0, getWidth(), getHeight(), 25, 25);

        // Border Panel
        g2.setColor(CowboyTheme.WOOD_DARK);
        g2.setStroke(new BasicStroke(2));
        g2.drawRoundRect(0, 0, getWidth()-1, getHeight()-1, 25, 25);

        super.paintComponent(g);

        // 2. HEADER "WANTED LIST"
        int y = 40;
        g2.setColor(CowboyTheme.RED_BANDANA);
        g2.fillRect(15, 10, 6, 25); // Strip merah di kiri

        g2.setColor(CowboyTheme.WOOD_DARK);
        g2.setFont(CowboyTheme.FONT_TITLE.deriveFont(22f));
        g2.drawString("GUNSLINGERS", 30, 30);

        // 3. DAFTAR PEMAIN
        List<Player> list = new ArrayList<>(players);
        Collections.reverse(list);

        int count = 0;
        for (Player p : list) {
            boolean active = (p == current);
            int cardH = 75;

            // --- Background Kartu Pemain ---
            if (active) {
                // Jika Giliran Aktif: Warna Emas/Kayu Terang
                g2.setColor(new Color(222, 184, 135)); // Burlywood
                g2.fillRoundRect(10, y - 5, 340, cardH + 10, 15, 15);

                // Border Tebal
                g2.setColor(CowboyTheme.GOLD_NUGGET);
                g2.setStroke(new BasicStroke(3));
                g2.drawRoundRect(10, y - 5, 340, cardH + 10, 15, 15);
            } else {
                // Pasif: Transparan / Pudar
                g2.setColor(new Color(139, 69, 19, 30)); // Coklat transparan
                g2.fillRoundRect(15, y, 330, cardH, 15, 15);
            }

            // --- Avatar (Topi/Warna) ---
            // Gambar lingkaran warna pemain
            g2.setColor(p.getColor());
            g2.fillOval(30, y + 12, 50, 50);

            // Outline Avatar
            g2.setColor(CowboyTheme.WOOD_DARK);
            g2.setStroke(new BasicStroke(2));
            g2.drawOval(30, y + 12, 50, 50);

            // --- Nama Pemain ---
            g2.setFont(CowboyTheme.FONT_TEXT.deriveFont(Font.BOLD, 16f));
            g2.setColor(CowboyTheme.WOOD_DARK);
            g2.drawString(p.getName(), 95, y + 30);

            // --- Status (Posisi & Koin) ---
            g2.setFont(CowboyTheme.FONT_TEXT.deriveFont(12f));
            g2.setColor(new Color(101, 67, 33)); // Coklat Tua
            g2.drawString("POS: " + p.getPosition() + "  |  GOLD: $" + p.getScore(), 95, y + 55);

            // --- Indikator Aktif ---
            if (active) {
                g2.setColor(CowboyTheme.RED_BANDANA);
                g2.setFont(new Font("Arial", Font.BOLD, 10));
                g2.drawString("★ SHOOTING NOW", 230, y + 25);
            }

            y += 90;
            count++;
        }
    }
}