import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class MazeApp {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("Weighted Maze Solver");
            frame.setLayout(new BorderLayout());

            JLabel statusLabel = new JLabel("Ready...");
            statusLabel.setFont(new Font("Consolas", Font.BOLD, 14));
            statusLabel.setForeground(Color.BLUE);
            statusLabel.setHorizontalAlignment(SwingConstants.CENTER);

            MazePanel mazePanel = new MazePanel(statusLabel);
            frame.add(mazePanel, BorderLayout.CENTER);

            JPanel sidePanel = new JPanel();
            sidePanel.setLayout(new BoxLayout(sidePanel, BoxLayout.Y_AXIS));
            sidePanel.setPreferredSize(new Dimension(220, 720));
            sidePanel.setBorder(new EmptyBorder(15, 15, 15, 15));
            sidePanel.setBackground(new Color(240, 240, 240));

            // --- HEADER ---
            JLabel titleLabel = new JLabel("Control Panel");
            titleLabel.setFont(new Font("Arial", Font.BOLD, 18));
            titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
            sidePanel.add(titleLabel);
            sidePanel.add(Box.createRigidArea(new Dimension(0, 10)));

            // --- TOMBOL RESIZE ---
            JButton btnResize = createStyledButton("⚙ Resize Maze", Color.LIGHT_GRAY);
            btnResize.addActionListener(e -> {
                JPanel p = new JPanel(new GridLayout(2, 2, 5, 5));
                JTextField rowField = new JTextField("25");
                JTextField colField = new JTextField("35");
                p.add(new JLabel("Rows (10-40):")); p.add(rowField);
                p.add(new JLabel("Cols (10-50):")); p.add(colField);

                int result = JOptionPane.showConfirmDialog(frame, p, "Settings", JOptionPane.OK_CANCEL_OPTION);
                if (result == JOptionPane.OK_OPTION) {
                    try {
                        int r = Integer.parseInt(rowField.getText());
                        int c = Integer.parseInt(colField.getText());
                        if (r < 5 || r > 50 || c < 5 || c > 60) {
                            JOptionPane.showMessageDialog(frame, "Size too big/small! Keep Rows<50, Cols<60.");
                        } else {
                            mazePanel.resizeMaze(r, c);
                            frame.pack(); frame.setLocationRelativeTo(null);
                        }
                    } catch (NumberFormatException ex) {
                        JOptionPane.showMessageDialog(frame, "Invalid Number!");
                    }
                }
            });
            sidePanel.add(btnResize);
            sidePanel.add(Box.createRigidArea(new Dimension(0, 15)));

            // --- GENERATION ---
            sidePanel.add(createLabel("Generation Mode"));
            JButton btnGenInstant = createStyledButton("New Maze (Instant)", new Color(176, 224, 230));
            btnGenInstant.addActionListener(e -> mazePanel.generateNewMaze(false));
            sidePanel.add(btnGenInstant);

            JButton btnGenAnim = createStyledButton("New Maze (Anim)", new Color(135, 206, 250));
            btnGenAnim.addActionListener(e -> mazePanel.generateNewMaze(true));
            sidePanel.add(btnGenAnim);

            sidePanel.add(Box.createRigidArea(new Dimension(0, 15)));
            sidePanel.add(new JSeparator());
            sidePanel.add(Box.createRigidArea(new Dimension(0, 15)));

            // --- ALGORITHMS ---
            sidePanel.add(createLabel("Unweighted Algorithms"));
            JButton btnBFS = createStyledButton("BFS (Shortest Step)", Color.CYAN);
            btnBFS.addActionListener(e -> mazePanel.runAlgorithm("BFS"));
            sidePanel.add(btnBFS);
            sidePanel.add(Box.createRigidArea(new Dimension(0, 5)));

            JButton btnDFS = createStyledButton("DFS (Random Path)", Color.MAGENTA);
            btnDFS.addActionListener(e -> mazePanel.runAlgorithm("DFS"));
            sidePanel.add(btnDFS);
            sidePanel.add(Box.createRigidArea(new Dimension(0, 15)));

            sidePanel.add(createLabel("Weighted Algorithms (Terrain)"));
            JButton btnDijkstra = createStyledButton("Dijkstra (Min Cost)", new Color(255, 165, 0));
            btnDijkstra.addActionListener(e -> mazePanel.runAlgorithm("Dijkstra"));
            sidePanel.add(btnDijkstra);
            sidePanel.add(Box.createRigidArea(new Dimension(0, 5)));

            JButton btnAStar = createStyledButton("A* (Heuristic)", new Color(138, 43, 226));
            btnAStar.addActionListener(e -> mazePanel.runAlgorithm("A*"));
            sidePanel.add(btnAStar);

            sidePanel.add(Box.createRigidArea(new Dimension(0, 25)));

            // --- STATS ---
            JPanel statsPanel = new JPanel(new BorderLayout());
            statsPanel.setBackground(Color.WHITE);
            statsPanel.setBorder(BorderFactory.createTitledBorder("Run Stats"));
            statsPanel.setPreferredSize(new Dimension(190, 70));
            statsPanel.setMaximumSize(new Dimension(190, 70));
            statsPanel.add(statusLabel, BorderLayout.CENTER);
            sidePanel.add(statsPanel);

            sidePanel.add(Box.createRigidArea(new Dimension(0, 15)));

            // --- LEGEND ---
            JPanel legendPanel = new JPanel(new GridLayout(6, 1, 2, 2));
            legendPanel.setBackground(new Color(240, 240, 240));
            legendPanel.setBorder(BorderFactory.createTitledBorder("Terrain Legend"));
            legendPanel.setMaximumSize(new Dimension(190, 160));
            legendPanel.add(createLegendItem(Color.WHITE, "Grass (Cost: 1)"));
            legendPanel.add(createLegendItem(new Color(139, 69, 19), "Mud (Cost: 5)"));
            legendPanel.add(createLegendItem(new Color(0, 100, 255), "Water (Cost: 10)"));
            legendPanel.add(createLegendItem(Color.GREEN, "Start Point"));
            legendPanel.add(createLegendItem(Color.RED, "Exit Point"));
            sidePanel.add(legendPanel);

            frame.add(sidePanel, BorderLayout.EAST);
            frame.pack();
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setLocationRelativeTo(null);
            frame.setResizable(false);
            frame.setVisible(true);
        });
    }

    private static JLabel createLabel(String text) {
        JLabel l = new JLabel(text);
        l.setAlignmentX(Component.CENTER_ALIGNMENT);
        l.setFont(new Font("Arial", Font.BOLD, 11));
        return l;
    }
    private static JButton createStyledButton(String text, Color baseColor) {
        JButton btn = new JButton(text);
        btn.setAlignmentX(Component.CENTER_ALIGNMENT);
        btn.setMaximumSize(new Dimension(190, 35));
        btn.setBackground(baseColor);
        btn.setForeground(Color.BLACK);
        btn.setFocusPainted(false);
        btn.setFont(new Font("Arial", Font.BOLD, 11));
        return btn;
    }
    private static JPanel createLegendItem(Color color, String text) {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        p.setBackground(new Color(240, 240, 240));
        JPanel colorBox = new JPanel();
        colorBox.setPreferredSize(new Dimension(15, 15));
        colorBox.setBackground(color);
        colorBox.setBorder(BorderFactory.createLineBorder(Color.BLACK));
        JLabel label = new JLabel(text);
        label.setFont(new Font("Arial", Font.PLAIN, 11));
        p.add(colorBox); p.add(label);
        return p;
    }
}