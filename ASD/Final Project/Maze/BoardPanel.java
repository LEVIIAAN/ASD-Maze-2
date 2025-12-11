import javax.swing.*;
import java.awt.*;
import java.awt.geom.Path2D;
import java.util.*;
import java.util.List;

public class BoardPanel extends JPanel {
    private final GameBoard board;
    private final Stack<Player> players;
    private List<Integer> shortestPath = new ArrayList<>();

    // Simpan koordinat X,Y untuk setiap kotak (1 sampai 64)
    private final Point[] nodePositions;
    private static final int TOTAL_NODES = 64;

    public BoardPanel(GameBoard board, Stack<Player> players) {
        this.board = board;
        this.players = players;
        this.nodePositions = new Point[TOTAL_NODES + 1]; // Index 1-64

        // Ukuran panel sedikit diperbesar agar jalur muat
        setPreferredSize(new Dimension(800, 700));
        setBackground(CyberTheme.BG_DARK);
    }

    public void updatePath(List<Integer> path) { this.shortestPath = path; repaint(); }
    public void clearPath() { this.shortestPath.clear(); repaint(); }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // 1. HITUNG ULANG POSISI (RESPONSIF & RAPI)
        calculateAdventurePath(getWidth(), getHeight());

        // 2. GAMBAR JALUR PUTUS-PUTUS (JEJAK)
        drawAdventureTrail(g2);

        // 3. GAMBAR NODE (TITIK-TITIK 1-64)
        for (int i = 1; i <= TOTAL_NODES; i++) {
            drawCyberNode(g2, i);
        }

        // 4. GAMBAR LINK (ULAR/TANGGA)
        drawShortcuts(g2);

        // 5. GAMBAR PEMAIN
        drawPlayers(g2);
    }

    // --- LOGIKA BARU: WAVY GRID (Agar tidak menumpuk) ---
    private void calculateAdventurePath(int w, int h) {
        int marginX = 80;
        int marginY = 80;

        // Area efektif untuk menggambar
        int usableW = w - (2 * marginX);
        int usableH = h - (2 * marginY);

        int rows = 8;
        int cols = 8;

        // Jarak antar titik
        double stepX = (double) usableW / (cols - 1);
        double stepY = (double) usableH / (rows - 1);

        for (int i = 0; i < TOTAL_NODES; i++) {
            int nodeNum = i + 1;

            // Tentukan Baris dan Kolom logika (0-7)
            int row = i / cols;
            int col = i % cols;

            // Logika Ular: Baris Genap (Kiri->Kanan), Ganjil (Kanan->Kiri)
            int actualCol = (row % 2 == 0) ? col : (cols - 1 - col);

            // Hitung Posisi Dasar (Grid)
            // Y dihitung dari bawah ke atas (row 0 di bawah)
            int baseX = marginX + (int)(actualCol * stepX);
            int baseY = (h - marginY) - (int)(row * stepY);

            // --- EFEK "ADVENTURE" (MELIUK) ---
            // Tambahkan gelombang Sinus pada sumbu Y agar tidak datar
            // Ini membuat jalur terlihat naik turun seperti bukit kecil
            int waveOffset = (int)(Math.sin(col * 0.8) * 15);

            // Tambahkan sedikit offset X agar belokan tidak terlalu tajam
            int curveOffset = (row % 2 != 0) ? 10 : -10;

            nodePositions[nodeNum] = new Point(baseX + curveOffset, baseY + waveOffset);
        }
    }

    // Menggambar Garis Putus-Putus (Dashed Line)
    private void drawAdventureTrail(Graphics2D g2) {
        if (nodePositions[1] == null) return;

        Path2D path = new Path2D.Float();
        path.moveTo(nodePositions[1].x, nodePositions[1].y);

        for (int i = 2; i <= TOTAL_NODES; i++) {
            Point p1 = nodePositions[i-1];
            Point p2 = nodePositions[i];

            // Gunakan kurva bezier agar sudut tidak patah-patah
            // Control point berada di tengah-tengah tapi agak naik/turun
            int ctrlX = (p1.x + p2.x) / 2;
            int ctrlY = (p1.y + p2.y) / 2 - 10; // Sedikit lengkung ke atas

            path.quadTo(ctrlX, ctrlY, p2.x, p2.y);
        }

        // --- STYLE GARIS PUTUS-PUTUS ---
        // Pola: 15px garis, 10px kosong
        float[] dashPattern = {15f, 10f};
        Stroke dashedStroke = new BasicStroke(3, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND, 0, dashPattern, 0);

        // 1. Gambar Glow (Bayangan Neon) - Solid tapi transparan
        g2.setColor(new Color(0, 243, 255, 30));
        g2.setStroke(new BasicStroke(15, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND)); // Tebal untuk glow
        g2.draw(path);

        // 2. Gambar Garis Inti (Putus-putus)
        g2.setColor(CyberTheme.NEON_CYAN);
        g2.setStroke(dashedStroke);
        g2.draw(path);
    }

    private void drawCyberNode(Graphics2D g2, int num) {
        Point p = nodePositions[num];
        if (p == null) return;

        int size = 28;
        int x = p.x - size/2;
        int y = p.y - size/2;

        boolean isPath = shortestPath.contains(num);

        // Start & Finish Nodes Special
        if (num == TOTAL_NODES) { // FINISH
            CyberTheme.drawGlowingOval(g2, x-5, y-5, size+10, size+10, CyberTheme.NEON_YELLOW);
            g2.setColor(Color.WHITE);
            g2.setFont(new Font("Arial", Font.BOLD, 12));
            g2.drawString("FINISH", x-10, y+45);
        } else if (num == 1) { // START
            CyberTheme.drawGlowingOval(g2, x-5, y-5, size+10, size+10, CyberTheme.NEON_GREEN);
            g2.setColor(Color.WHITE);
            g2.setFont(new Font("Arial", Font.BOLD, 12));
            g2.drawString("START", x-5, y+45);
        } else {
            // Node Biasa
            Color nodeCol = isPath ? CyberTheme.NEON_GREEN : new Color(30, 30, 50);

            // Lingkaran luar
            g2.setColor(CyberTheme.NEON_CYAN);
            g2.setStroke(new BasicStroke(1));
            g2.drawOval(x, y, size, size);

            // Isi
            g2.setColor(nodeCol);
            g2.fillOval(x, y, size, size);
        }

        // Nomor Node
        g2.setFont(CyberTheme.FONT_NUM.deriveFont(11f));
        g2.setColor(Color.WHITE);
        String s = String.valueOf(num);
        FontMetrics fm = g2.getFontMetrics();
        g2.drawString(s, p.x - fm.stringWidth(s)/2, p.y + 4);

        drawFeatures(g2, p.x, p.y, num);
    }

    private void drawFeatures(Graphics2D g2, int cx, int cy, int num) {
        // Fitur digambar sedikit di kanan atas node
        int ox = 12;
        int oy = -12;

        if (board.hasStar(num)) {
            int[] xp = {cx+ox, cx+ox+3, cx+ox+8, cx+ox+4, cx+ox+6, cx+ox, cx+ox-6, cx+ox-4, cx+ox-8, cx+ox-3};
            int[] yp = {cy+oy-8, cy+oy-3, cy+oy-3, cy+oy+2, cy+oy+7, cy+oy+4, cy+oy+7, cy+oy+2, cy+oy-3, cy+oy-3};
            CyberTheme.drawGlowingPolygon(g2, new Polygon(xp, yp, 10), CyberTheme.NEON_PURPLE);
        }

        if (board.isPrime(num)) {
            g2.setColor(CyberTheme.NEON_CYAN);
            g2.setFont(new Font("Arial", Font.BOLD, 10));
            g2.drawString("P", cx - 20, cy - 10);
            g2.drawRect(cx - 22, cy - 20, 12, 12);
        }

        if (board.getPointsMap().containsKey(num)) {
            int val = board.getPointsMap().get(num);
            g2.setColor(CyberTheme.NEON_YELLOW);
            g2.fillOval(cx + 10, cy + 5, 14, 14);
            g2.setColor(Color.BLACK);
            g2.setFont(new Font("Arial", Font.BOLD, 9));
            g2.drawString(String.valueOf(val), cx + 11, cy + 16);
        }
    }

    private void drawShortcuts(Graphics2D g2) {
        for (Link l : board.getLinks().values()) {
            Point p1 = nodePositions[l.getFrom()];
            Point p2 = nodePositions[l.getTo()];
            if (p1 == null || p2 == null) continue;

            boolean isTrap = l.getFrom() > l.getTo();
            Color linkColor = isTrap ? new Color(255, 50, 50) : CyberTheme.NEON_PINK;

            // Shortcut tetap garis solid atau titik halus berbeda
            Stroke oldStroke = g2.getStroke();
            // Garis titik-titik sangat halus untuk shortcut
            g2.setStroke(new BasicStroke(2, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 0, new float[]{3, 5}, 0));
            CyberTheme.drawGlowingLine(g2, p1.x, p1.y, p2.x, p2.y, linkColor, 2);
            g2.setStroke(oldStroke);

            // Panah Arah
            int midX = (p1.x + p2.x) / 2;
            int midY = (p1.y + p2.y) / 2;
            g2.setColor(Color.WHITE);
            g2.setFont(new Font("Arial", Font.BOLD, 14));
            g2.drawString(isTrap ? "▼" : "▲", midX, midY);
        }
    }

    private void drawPlayers(Graphics2D g2) {
        Map<Integer, List<Player>> map = new HashMap<>();
        for (Player p : players) {
            map.computeIfAbsent(p.getPosition(), k -> new ArrayList<>()).add(p);
        }

        for (Map.Entry<Integer, List<Player>> entry : map.entrySet()) {
            int pos = entry.getKey();
            List<Player> here = entry.getValue();
            Point p = nodePositions[pos];
            if (p == null) continue;

            // Susun pemain berjejer horizontal di atas node
            int offsetStep = 18;
            int startX = p.x - ((here.size() - 1) * offsetStep) / 2;

            for (int i = 0; i < here.size(); i++) {
                Player player = here.get(i);
                // Bola energi melayang
                CyberTheme.drawGlowingOval(g2, startX + (i * offsetStep) - 8, p.y - 28, 16, 16, player.getColor());
            }
        }
    }
}