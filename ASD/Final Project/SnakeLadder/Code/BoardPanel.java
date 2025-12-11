import javax.swing.*;
import java.awt.*;
import java.awt.geom.Path2D;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;
import java.util.*;
import java.util.List;

public class BoardPanel extends JPanel {
    private final GameBoard board;
    private final Stack<Player> players;
    private List<Integer> shortestPath = new ArrayList<>();

    // Posisi Pixel Aktual
    private final Point[] nodePositions;
    private static final int TOTAL_NODES = 64;

    private BufferedImage mapImage;

    // --- KOORDINAT MANUAL 64 TITIK (PERSENTASE 0.0 - 1.0) ---
    // Dikalibrasi khusus untuk 'cowboy_map.jpg' (Start Kanan Bawah -> Finish Kiri Atas)
    private final Point2D.Double[] manualNodes = {
            new Point2D.Double(0.860, 0.900), // Node 1 (Start)
            new Point2D.Double(0.937, 0.936), // Node 2
            new Point2D.Double(0.898, 0.950), // Node 3
            new Point2D.Double(0.761, 0.945), // Node 4
            new Point2D.Double(0.626, 0.941), // Node 5
            new Point2D.Double(0.510, 0.939), // Node 6
            new Point2D.Double(0.412, 0.940), // Node 7
            new Point2D.Double(0.329, 0.939), // Node 8
            new Point2D.Double(0.238, 0.916), // Node 9
            new Point2D.Double(0.139, 0.868), // Node 10
            new Point2D.Double(0.113, 0.812), // Node 11
            new Point2D.Double(0.222, 0.774), // Node 12
            new Point2D.Double(0.325, 0.755), // Node 13
            new Point2D.Double(0.417, 0.754), // Node 14
            new Point2D.Double(0.509, 0.758), // Node 15
            new Point2D.Double(0.604, 0.763), // Node 16
            new Point2D.Double(0.701, 0.770), // Node 17
            new Point2D.Double(0.798, 0.756), // Node 18
            new Point2D.Double(0.890, 0.700), // Node 19
            new Point2D.Double(0.878, 0.645), // Node 20
            new Point2D.Double(0.761, 0.625), // Node 21
            new Point2D.Double(0.661, 0.623), // Node 22
            new Point2D.Double(0.571, 0.633), // Node 23
            new Point2D.Double(0.478, 0.642), // Node 24
            new Point2D.Double(0.382, 0.649), // Node 25
            new Point2D.Double(0.284, 0.651), // Node 26
            new Point2D.Double(0.189, 0.623), // Node 27
            new Point2D.Double(0.099, 0.559), // Node 28
            new Point2D.Double(0.139, 0.512), // Node 29
            new Point2D.Double(0.254, 0.492), // Node 30
            new Point2D.Double(0.354, 0.483), // Node 31
            new Point2D.Double(0.444, 0.484), // Node 32
            new Point2D.Double(0.536, 0.489), // Node 33
            new Point2D.Double(0.633, 0.497), // Node 34
            new Point2D.Double(0.733, 0.509), // Node 35
            new Point2D.Double(0.829, 0.491), // Node 36
            new Point2D.Double(0.914, 0.428), // Node 37
            new Point2D.Double(0.895, 0.381), // Node 38
            new Point2D.Double(0.772, 0.380), // Node 39
            new Point2D.Double(0.668, 0.378), // Node 40
            new Point2D.Double(0.579, 0.378), // Node 41
            new Point2D.Double(0.488, 0.381), // Node 42
            new Point2D.Double(0.394, 0.386), // Node 43
            new Point2D.Double(0.297, 0.393), // Node 44
            new Point2D.Double(0.200, 0.374), // Node 45
            new Point2D.Double(0.102, 0.318), // Node 46
            new Point2D.Double(0.108, 0.271), // Node 47
            new Point2D.Double(0.229, 0.257), // Node 48
            new Point2D.Double(0.333, 0.248), // Node 49
            new Point2D.Double(0.422, 0.245), // Node 50
            new Point2D.Double(0.513, 0.247), // Node 51
            new Point2D.Double(0.612, 0.256), // Node 52
            new Point2D.Double(0.718, 0.270), // Node 53
            new Point2D.Double(0.817, 0.265), // Node 54
            new Point2D.Double(0.892, 0.205), // Node 55
            new Point2D.Double(0.884, 0.135), // Node 56
            new Point2D.Double(0.770, 0.116), // Node 57
            new Point2D.Double(0.668, 0.109), // Node 58
            new Point2D.Double(0.577, 0.111), // Node 59
            new Point2D.Double(0.485, 0.110), // Node 60
            new Point2D.Double(0.391, 0.106), // Node 61
            new Point2D.Double(0.296, 0.100), // Node 62
            new Point2D.Double(0.199, 0.091), // Node 63
            new Point2D.Double(0.100, 0.080), // Node 64 (Finish)
    };

    public BoardPanel(GameBoard board, Stack<Player> players) {
        this.board = board;
        this.players = players;
        this.nodePositions = new Point[TOTAL_NODES + 1];

        setPreferredSize(new Dimension(500, 800));

        // --- LOAD GAMBAR ---
        try {
            File imgFile = new File("C:\\Users\\Farhan Fitran\\Documents\\Coding Projects\\ASD\\Final Project\\SnakeLadder\\assets\\cowboy_map.jpg");
            if (!imgFile.exists()) imgFile = new File("C:\\Users\\Farhan Fitran\\Documents\\Coding Projects\\ASD\\Final Project\\SnakeLadder\\assets\\cowboy_map.jpg");

            if (imgFile.exists()) {
                mapImage = ImageIO.read(imgFile);
            } else {
                System.err.println("GAMBAR TIDAK DITEMUKAN");
                setBackground(CowboyTheme.BG_SAND);
            }
        } catch (IOException e) {
            setBackground(CowboyTheme.BG_SAND);
        }
    }

    public void updatePath(List<Integer> path) { this.shortestPath = path; repaint(); }
    public void clearPath() { this.shortestPath.clear(); repaint(); }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // 1. GAMBAR BACKGROUND
        if (mapImage != null) {
            g2.drawImage(mapImage, 0, 0, getWidth(), getHeight(), null);
        } else {
            g2.setColor(CowboyTheme.BG_SAND);
            g2.fillRect(0, 0, getWidth(), getHeight());
        }

        // 2. SET POSISI PIXEL BERDASARKAN ARRAY MANUAL
        updatePixelPositions(getWidth(), getHeight());

        // 3. GAMBAR JALUR PUTUS-PUTUS
        drawDashedPath(g2);

        // 4. GAMBAR NODE
        for (int i = 1; i <= TOTAL_NODES; i++) {
            drawMapNode(g2, i);
        }

        // 5. GAMBAR LINK & PEMAIN
        drawLinks(g2);
        drawPlayers(g2);
    }

    // --- KONVERSI PERSENTASE KE PIXEL ---
    private void updatePixelPositions(int w, int h) {
        for (int i = 0; i < TOTAL_NODES; i++) {
            Point2D.Double p = manualNodes[i];
            int x = (int) (p.x * w);
            int y = (int) (p.y * h);
            nodePositions[i + 1] = new Point(x, y);
        }
    }

    private void drawDashedPath(Graphics2D g2) {
        if (nodePositions[1] == null) return;
        Path2D path = new Path2D.Float();
        path.moveTo(nodePositions[1].x, nodePositions[1].y);

        for (int i = 2; i <= TOTAL_NODES; i++) {
            Point p = nodePositions[i];
            Point prev = nodePositions[i-1];
            // QuadCurve halus antar titik
            path.quadTo(prev.x, prev.y, p.x, p.y);
        }

        Stroke dashed = new BasicStroke(3, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 0, new float[]{8}, 0);
        g2.setColor(new Color(60, 30, 10, 80)); // Shadow
        g2.setStroke(new BasicStroke(5));
        g2.draw(path);
        g2.setColor(new Color(255, 245, 220)); // Krem Terang
        g2.setStroke(dashed);
        g2.draw(path);
    }

    private void drawMapNode(Graphics2D g2, int num) {
        Point p = nodePositions[num];
        if (p == null) return;

        int size = 28;
        int x = p.x - size/2;
        int y = p.y - size/2;

        boolean isPath = shortestPath.contains(num);
        if (isPath) {
            g2.setColor(new Color(0, 191, 255, 180));
            g2.fillOval(x-2, y-2, size+4, size+4);
        }

        // Node: Lingkaran Kayu
        g2.setColor(CowboyTheme.WOOD_DARK);
        g2.fillOval(x, y, size, size);
        g2.setColor(CowboyTheme.WOOD_LIGHT);
        g2.fillOval(x+2, y+2, size-4, size-4);

        // Angka
        g2.setFont(CowboyTheme.FONT_NUM.deriveFont(Font.BOLD, 12f));
        String s = String.valueOf(num);
        FontMetrics fm = g2.getFontMetrics();
        int textX = p.x - fm.stringWidth(s)/2;
        int textY = p.y + 5;

        g2.setColor(Color.WHITE);
        g2.drawString(s, textX, textY);

        if (board.hasStar(num)) CowboyTheme.drawSheriffStar(g2, p.x, p.y - 18, 14);
        if (board.getPointsMap().containsKey(num)) {
            g2.setColor(CowboyTheme.GOLD_NUGGET);
            g2.fillOval(p.x + 10, p.y + 5, 10, 10);
        }

        if (num == 1) drawLabel(g2, "START", p.x, p.y + 30, new Color(34, 139, 34));
        if (num == 64) drawLabel(g2, "FINISH", p.x, p.y - 20, new Color(255, 215, 0));
    }

    private void drawLabel(Graphics2D g2, String text, int x, int y, Color c) {
        g2.setFont(new Font("Rockwell", Font.BOLD, 14));
        FontMetrics fm = g2.getFontMetrics();
        int w = fm.stringWidth(text);
        g2.setColor(Color.BLACK);
        g2.drawString(text, x - w/2 + 1, y + 1);
        g2.setColor(c);
        g2.drawString(text, x - w/2, y);
    }

    private void drawLinks(Graphics2D g2) {
        for (Link l : board.getLinks().values()) {
            Point p1 = nodePositions[l.getFrom()];
            Point p2 = nodePositions[l.getTo()];
            if (p1 == null || p2 == null) continue;

            if (l.getFrom() < l.getTo()) {
                CowboyTheme.drawRope(g2, p1.x, p1.y, p2.x, p2.y);
            } else {
                CowboyTheme.drawSnakeLine(g2, p1.x, p1.y, p2.x, p2.y);
            }
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

            int offsetStep = 15;
            int startX = p.x - ((here.size() - 1) * offsetStep) / 2;

            for (int i = 0; i < here.size(); i++) {
                Player player = here.get(i);
                drawCowboyHat(g2, startX + (i * offsetStep), p.y - 20, player.getColor());
            }
        }
    }

    private void drawCowboyHat(Graphics2D g2, int x, int y, Color c) {
        g2.setColor(c.darker());
        g2.fillOval(x-12, y+5, 24, 8);
        g2.setColor(c);
        g2.fillRoundRect(x-8, y-5, 16, 12, 5, 5);
        g2.setColor(Color.BLACK);
        g2.setStroke(new BasicStroke(1));
        g2.drawOval(x-12, y+5, 24, 8);
        g2.drawRoundRect(x-8, y-5, 16, 12, 5, 5);
    }
}