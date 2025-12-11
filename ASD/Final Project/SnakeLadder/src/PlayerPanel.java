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

        // Agar background transparan dan tidak glitch
        setOpaque(false);

        // Hapus setPreferredSize statis, kita pakai getPreferredSize dinamis di bawah
        setBackground(CyberTheme.BG_PANEL);
        setBorder(new EmptyBorder(20, 20, 20, 20));
    }

    public void setCurrent(Player p) {
        this.current = p;
        repaint();
    }

    // [PENTING] Menghitung tinggi panel berdasarkan jumlah pemain
    // Agar ScrollPane tahu seberapa panjang area yang bisa discroll
    @Override
    public Dimension getPreferredSize() {
        int cardHeight = 90; // Tinggi per kartu + gap
        int headerHeight = 50;
        int totalHeight = headerHeight + (players.size() * cardHeight) + 30; // +Padding bawah
        return new Dimension(380, Math.max(250, totalHeight));
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // 1. GAMBAR BACKGROUND PANEL
        g2.setColor(getBackground());
        g2.fillRoundRect(0, 0, getWidth(), getHeight(), 25, 25);

        super.paintComponent(g);

        // 2. HEADER
        int y = 40;
        g2.setColor(CyberTheme.NEON_CYAN);
        g2.fillRect(15, 10, 4, 25);

        g2.setColor(Color.WHITE);
        g2.setFont(CyberTheme.FONT_TITLE.deriveFont(22f));
        g2.drawString("PLAYER STACK", 28, 30);

        // 3. DAFTAR PEMAIN
        List<Player> list = new ArrayList<>(players);
        Collections.reverse(list);

        int count = 0;
        for (Player p : list) {
            // [MODIFIKASI] HAPUS LIMITER (break) AGAR SEMUA PEMAIN DIGAMBAR
            // if (count >= 3) break; <--- Baris ini dihapus

            boolean active = (p == current);
            int cardH = 75;

            // Background Kartu
            if (active) {
                g2.setColor(new Color(CyberTheme.NEON_CYAN.getRed(), CyberTheme.NEON_CYAN.getGreen(), CyberTheme.NEON_CYAN.getBlue(), 40));
                g2.fillRoundRect(10, y - 5, 340, cardH + 10, 15, 15);
                g2.setColor(CyberTheme.NEON_CYAN);
                g2.setStroke(new BasicStroke(1));
                g2.drawRoundRect(10, y - 5, 340, cardH + 10, 15, 15);
            } else {
                g2.setColor(new Color(255, 255, 255, 15));
                g2.fillRoundRect(15, y, 330, cardH, 15, 15);
            }

            // Avatar & Teks
            CyberTheme.drawGlowingOval(g2, 30, y + 12, 50, 50, p.getColor());

            g2.setFont(CyberTheme.FONT_TEXT.deriveFont(Font.BOLD, 16f));
            g2.setColor(active ? CyberTheme.NEON_CYAN : Color.LIGHT_GRAY);
            g2.drawString(p.getName(), 95, y + 30);

            g2.setFont(CyberTheme.FONT_TEXT.deriveFont(11f));
            g2.setColor(Color.WHITE);
            g2.drawString("POS: " + p.getPosition() + "  |  COINS: " + p.getScore(), 95, y + 55);

            if (active) {
                g2.setColor(CyberTheme.NEON_GREEN);
                g2.setFont(new Font("Arial", Font.BOLD, 10));
                g2.drawString("• ACTIVE UPLINK", 230, y + 25);
            }

            y += 90;
            count++;
        }
    }
}