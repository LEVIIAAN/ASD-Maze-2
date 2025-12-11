import java.awt.*;

public class Cell implements Comparable<Cell> {
    public int row, col;
    public int size;

    // Properti
    public int cost = 1; // 1:Grass, 5:Mud, 10:Water

    // Variabel Algoritma
    public int gCost = Integer.MAX_VALUE;
    public int hCost = 0;
    public int fCost = Integer.MAX_VALUE;

    // Status
    public boolean visited = false;
    public boolean searched = false;
    public boolean isPath = false;
    public boolean isStart = false;
    public boolean isEnd = false;
    public boolean isHead = false;

    // Dinding & Visual
    public boolean[] walls = {true, true, true, true};
    public Cell parent;
    public Color searchColor = null;

    public Cell(int row, int col, int size) {
        this.row = row;
        this.col = col;
        this.size = size;
    }

    @Override
    public int compareTo(Cell other) {
        return Integer.compare(this.fCost, other.fCost);
    }

    public void draw(Graphics g) {
        int x = col * size;
        int y = row * size;

        Graphics2D g2 = (Graphics2D) g;
        // Haluskan gambar lingkaran/garis
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // --- LAYER 1: TERRAIN (DIGAMBAR MANUAL AGAR TIDAK KOTAK-KOTAK) ---
        if (cost == 5) { // MUD (LUMPUR)
            g.setColor(new Color(160, 82, 45)); // Coklat Background
            g.fillRect(x, y, size, size);

            g.setColor(new Color(80, 40, 10)); // Coklat Tua (Titik)
            g.fillOval(x + 4, y + 4, 4, 4);
            g.fillOval(x + size - 8, y + 6, 3, 3);
            g.fillOval(x + 6, y + size - 8, 3, 3);

        } else if (cost == 10) { // WATER (AIR)
            g.setColor(new Color(135, 206, 235)); // Biru Langit
            g.fillRect(x, y, size, size);

            g.setColor(new Color(0, 80, 200)); // Biru Laut (Gelombang)
            g2.setFont(new Font("SansSerif", Font.BOLD, 16));
            g.drawString("~", x + (size/2) - 5, y + (size/2) + 5);

        } else { // GRASS (RUMPUT)
            g.setColor(new Color(240, 255, 240)); // Hijau Muda Bersih
            g.fillRect(x, y, size, size);
        }

        // --- LAYER 2: MASKING (HITAM JIKA BELUM JADI MAZE) ---
        if (!visited) {
            g.setColor(new Color(30, 30, 30));
            g.fillRect(x, y, size, size);
        }

        // --- LAYER 3: STATUS & ALGORITMA ---
        if (isHead) {
            g.setColor(Color.ORANGE);
            g.fillRect(x, y, size, size);
            g.setColor(Color.BLACK);
            g.drawRect(x+4, y+4, size-8, size-8);
        } else if (isStart) {
            g.setColor(Color.GREEN);
            g.fillRect(x, y, size, size);
            g.setColor(Color.BLACK);
            g2.setFont(new Font("Arial", Font.BOLD, 12));
            g.drawString("S", x + 8, y + size - 6);
        } else if (isEnd) {
            g.setColor(Color.RED);
            g.fillRect(x, y, size, size);
            g.setColor(Color.WHITE);
            g2.setFont(new Font("Arial", Font.BOLD, 12));
            g.drawString("E", x + 8, y + size - 6);
        } else if (isPath) {
            g.setColor(new Color(255, 215, 0)); // Emas
            g.fillRect(x, y, size, size);
            g.setColor(new Color(255, 140, 0)); // Garis Tepi Emas Tua
            g2.setStroke(new BasicStroke(2));
            g2.drawRect(x+5, y+5, size-10, size-10);
            g2.setStroke(new BasicStroke(1));
        } else if (searched) {
            Color base = (searchColor != null) ? searchColor : Color.CYAN;
            g.setColor(new Color(base.getRed(), base.getGreen(), base.getBlue(), 150));
            g.fillRect(x, y, size, size);
        }

        // --- LAYER 4: DINDING TEBAL ---
        if (visited) {
            g.setColor(Color.BLACK);
            g2.setStroke(new BasicStroke(3)); // Tebal 3px

            if (walls[0]) g2.drawLine(x, y, x + size, y);
            if (walls[1]) g2.drawLine(x + size, y, x + size, y + size);
            if (walls[2]) g2.drawLine(x + size, y + size, x, y + size);
            if (walls[3]) g2.drawLine(x, y + size, x, y);

            g2.setStroke(new BasicStroke(1)); // Reset tebal garis
        }
    }

    public void resetForSolver() {
        searched = false; isPath = false; parent = null; isHead = false;
        searchColor = null; gCost = Integer.MAX_VALUE; fCost = Integer.MAX_VALUE; hCost = 0;
    }
}