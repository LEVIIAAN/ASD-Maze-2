import javax.swing.*;
import java.awt.*;

public class DiceFacePanel extends JPanel {
    private int value = 1;

    public DiceFacePanel() {
        // PENTING: Buat panel transparan
        setOpaque(false);
        setBackground(new Color(0,0,0,0));
    }

    public void setValue(int v) { this.value = v; repaint(); }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int size = Math.min(getWidth(), getHeight()) - 10;
        int x = (getWidth() - size) / 2;
        int y = (getHeight() - size) / 2;

        // 1. Ganti Kotak Putih dengan Kotak Transparan Ber-Border Neon
        g2.setColor(CyberTheme.BG_PANEL); // Isi agak gelap transparan
        g2.fillRoundRect(x, y, size, size, 25, 25);

        // Border Neon Cyan
        g2.setColor(new Color(CyberTheme.NEON_CYAN.getRed(), CyberTheme.NEON_CYAN.getGreen(), CyberTheme.NEON_CYAN.getBlue(), 100));
        g2.setStroke(new BasicStroke(6));
        g2.drawRoundRect(x, y, size, size, 25, 25);
        g2.setColor(CyberTheme.NEON_CYAN);
        g2.setStroke(new BasicStroke(3));
        g2.drawRoundRect(x, y, size, size, 25, 25);

        // 2. Gambar Titik Dadu (Pips) dengan warna Neon
        drawPips(g2, x, y, size);
    }

    private void drawPips(Graphics2D g2, int x, int y, int size) {
        int r = size / 7; // Titik sedikit lebih kecil
        int cx = x + size/2, cy = y + size/2;
        int l = x + size/4, rX = x + 3*size/4;
        int t = y + size/4, b = y + 3*size/4;

        // Warna Titik Dadu jadi Putih/Kuning terang
        g2.setColor(Color.WHITE);
        if(value%2!=0) fillGlowPip(g2, cx-r/2, cy-r/2, r);
        if(value>1) { fillGlowPip(g2, l-r/2, t-r/2, r); fillGlowPip(g2, rX-r/2, b-r/2, r); }
        if(value>3) { fillGlowPip(g2, l-r/2, b-r/2, r); fillGlowPip(g2, rX-r/2, t-r/2, r); }
        if(value==6) { fillGlowPip(g2, l-r/2, cy-r/2, r); fillGlowPip(g2, rX-r/2, cy-r/2, r); }
    }

    // Helper kecil untuk titik dadu bercahaya
    private void fillGlowPip(Graphics2D g2, int px, int py, int pr) {
        g2.setColor(new Color(255,255,255,100)); g2.fillOval(px-2, py-2, pr+4, pr+4);
        g2.setColor(Color.WHITE); g2.fillOval(px, py, pr, pr);
    }
}