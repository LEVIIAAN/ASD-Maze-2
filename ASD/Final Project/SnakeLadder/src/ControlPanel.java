import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionListener;

public class ControlPanel extends JPanel {
    private final DiceFacePanel dicePanel;
    private final JLabel dirLabel, statusLabel;
    private final JTextArea pathArea;
    private final CyberTheme.Button rollBtn; // Pakai tombol Cyber

    public ControlPanel(ActionListener onRoll) {
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        // Background diatur di main class, tapi set opaque false untuk keamanan
        setOpaque(false);
        setBorder(new EmptyBorder(20, 20, 20, 20));

        JLabel title = new JLabel("CONTROLS");
        title.setFont(CyberTheme.FONT_TITLE);
        title.setForeground(CyberTheme.NEON_CYAN);
        title.setAlignmentX(CENTER_ALIGNMENT);
        add(title);
        add(Box.createVerticalStrut(25));

        JPanel diceBox = new JPanel(new GridLayout(1, 2, 20, 0));
        diceBox.setOpaque(false); // Penting!

        dicePanel = new DiceFacePanel();
        dicePanel.setPreferredSize(new Dimension(100, 100));
        diceBox.add(dicePanel);

        dirLabel = new JLabel("?", SwingConstants.CENTER);
        dirLabel.setFont(new Font("Arial", Font.BOLD, 50));
        // PENTING: Hapus setOpaque(true) dan setBackground(Color.WHITE) yang lama!
        dirLabel.setOpaque(false);
        dirLabel.setForeground(Color.GRAY);
        diceBox.add(dirLabel);

        add(diceBox);
        add(Box.createVerticalStrut(30));

        // Ganti JButton biasa dengan CyberTheme.Button
        rollBtn = new CyberTheme.Button("EXECUTE ROLL", CyberTheme.NEON_CYAN);
        rollBtn.setAlignmentX(CENTER_ALIGNMENT);
        rollBtn.setMaximumSize(new Dimension(200, 50));
        rollBtn.addActionListener(onRoll);
        add(rollBtn);

        add(Box.createVerticalStrut(20));

        statusLabel = new JLabel("AWAITING INPUT...", SwingConstants.CENTER);
        statusLabel.setFont(CyberTheme.FONT_TEXT);
        statusLabel.setForeground(Color.GRAY);
        statusLabel.setAlignmentX(CENTER_ALIGNMENT);
        add(statusLabel);

        add(Box.createVerticalStrut(15));
        pathArea = new JTextArea(4, 20);
        pathArea.setEditable(false);
        pathArea.setLineWrap(true);
        pathArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        pathArea.setBackground(new Color(10, 10, 20)); // Gelap
        pathArea.setForeground(CyberTheme.NEON_CYAN);
        pathArea.setBorder(BorderFactory.createLineBorder(CyberTheme.NEON_CYAN));

        JScrollPane scroll = new JScrollPane(pathArea);
        scroll.setBorder(null);
        add(scroll);
    }

    public void updateDice(int val, int mod) {
        dicePanel.setValue(val);
        dirLabel.setText(mod == 1 ? "▲" : "▼");
        // Warna panah jadi Neon Hijau (Maju) atau Neon Merah/Pink (Mundur)
        dirLabel.setForeground(mod == 1 ? CyberTheme.NEON_GREEN : CyberTheme.NEON_PINK);
    }

    public void setStatus(String t, Color c) { statusLabel.setText(t); statusLabel.setForeground(c); }
    public void setPath(String t) { pathArea.setText(t); }
    public void toggleBtn(boolean b) { rollBtn.setEnabled(b); }
}