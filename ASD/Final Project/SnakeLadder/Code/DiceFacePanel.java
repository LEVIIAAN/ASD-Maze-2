import javax.swing.*;
import java.awt.*;

public class DiceFacePanel extends JPanel {
    private int value = 1;

    public DiceFacePanel() {
        setOpaque(false); // Transparan agar bentuk bulat terlihat
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

        // 1. Gambar Dadu Kayu (Solid)
        g2.setColor(CowboyTheme.WOOD_LIGHT); // Warna dasar kayu
        g2.fillRoundRect(x, y, size, size, 25, 25);

        // 2. Border Kayu Gelap
        g2.setColor(CowboyTheme.WOOD_DARK);
        g2.setStroke(new BasicStroke(4));
        g2.drawRoundRect(x, y, size, size, 25, 25);

        // Efek serat kayu sedikit
        g2.setColor(new Color(0,0,0,20));
        g2.drawLine(x+10, y+20, x+size-10, y+20);
        g2.drawLine(x+10, y+size-20, x+size-10, y+size-20);

        // 3. Gambar Titik Dadu (Pips) - Warna Hitam/Gelap
        drawPips(g2, x, y, size);
    }

    private void drawPips(Graphics2D g2, int x, int y, int size) {
        int r = size / 7;
        int cx = x + size/2, cy = y + size/2;
        int l = x + size/4, rX = x + 3*size/4;
        int t = y + size/4, b = y + 3*size/4;

        g2.setColor(new Color(50, 30, 10)); // Coklat sangat tua / Hitam

        if(value%2!=0) fillPip(g2, cx-r/2, cy-r/2, r);
        if(value>1) { fillPip(g2, l-r/2, t-r/2, r); fillPip(g2, rX-r/2, b-r/2, r); }
        if(value>3) { fillPip(g2, l-r/2, b-r/2, r); fillPip(g2, rX-r/2, t-r/2, r); }
        if(value==6) { fillPip(g2, l-r/2, cy-r/2, r); fillPip(g2, rX-r/2, cy-r/2, r); }
    }

    private void fillPip(Graphics2D g2, int px, int py, int pr) {
        g2.fillOval(px, py, pr, pr);
    }
}