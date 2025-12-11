import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionListener;

public class ControlPanel extends JPanel {
    private final DiceFacePanel dicePanel;
    private final JLabel dirLabel, statusLabel;
    private final JTextArea pathArea;
    private final CowboyTheme.Button rollBtn;

    public ControlPanel(ActionListener onRoll) {
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setOpaque(false);
        // Padding agar konten tidak mepet border background baru
        setBorder(new EmptyBorder(25, 20, 25, 20));
        setPreferredSize(new Dimension(380, 420));

        // JUDUL
        JLabel title = new JLabel("CONTROLS");
        title.setFont(CowboyTheme.FONT_TITLE.deriveFont(28f));
        title.setForeground(new Color(60, 30, 10)); // Coklat Sangat Tua
        title.setAlignmentX(CENTER_ALIGNMENT);
        add(title);
        add(Box.createVerticalStrut(25));

        // AREA DADU
        JPanel diceBox = new JPanel(new FlowLayout(FlowLayout.CENTER, 30, 0));
        diceBox.setOpaque(false);

        dicePanel = new DiceFacePanel();
        dicePanel.setPreferredSize(new Dimension(100, 100));
        diceBox.add(dicePanel);

        dirLabel = new JLabel("?", SwingConstants.CENTER);
        dirLabel.setFont(new Font("Rockwell", Font.BOLD, 60));
        dirLabel.setForeground(new Color(101, 67, 33));
        diceBox.add(dirLabel);

        add(diceBox);
        add(Box.createVerticalStrut(30));

        // TOMBOL
        rollBtn = new CowboyTheme.Button("ROLL DICE");
        rollBtn.setAlignmentX(CENTER_ALIGNMENT);
        rollBtn.setMaximumSize(new Dimension(220, 60));
        rollBtn.addActionListener(onRoll);
        add(rollBtn);

        add(Box.createVerticalStrut(25));

        // STATUS
        statusLabel = new JLabel("WAITING...", SwingConstants.CENTER);
        statusLabel.setFont(CowboyTheme.FONT_TEXT.deriveFont(Font.BOLD, 14f));
        statusLabel.setForeground(new Color(60, 30, 10));
        statusLabel.setAlignmentX(CENTER_ALIGNMENT);
        add(statusLabel);

        add(Box.createVerticalStrut(10));

        // LOG AREA
        pathArea = new JTextArea(3, 20);
        pathArea.setEditable(false);
        pathArea.setLineWrap(true);
        pathArea.setFont(new Font("Courier New", Font.BOLD, 12));
        pathArea.setBackground(new Color(255, 250, 240)); // Kertas Putih Gading
        pathArea.setForeground(Color.BLACK);
        pathArea.setBorder(BorderFactory.createLineBorder(new Color(160, 110, 60), 2));

        JScrollPane scroll = new JScrollPane(pathArea);
        scroll.setBorder(null);
        scroll.setMaximumSize(new Dimension(320, 70));
        add(scroll);
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // GAMBAR BACKGROUND KERTAS TUA / KULIT DI BELAKANG KONTROL
        // Ini memisahkan kontrol dari background kayu panel kanan
        g2.setColor(new Color(235, 215, 180)); // Warna Kulit Terang / Tan
        g2.fillRoundRect(0, 0, getWidth(), getHeight(), 30, 30);

        // Border Hiasan
        g2.setColor(new Color(160, 82, 45)); // Sienna
        g2.setStroke(new BasicStroke(3));
        g2.drawRoundRect(5, 5, getWidth()-10, getHeight()-10, 25, 25);

        // Paku di sudut
        g2.setColor(new Color(60, 30, 10));
        g2.fillOval(15, 15, 8, 8);
        g2.fillOval(getWidth()-23, 15, 8, 8);
        g2.fillOval(15, getHeight()-23, 8, 8);
        g2.fillOval(getWidth()-23, getHeight()-23, 8, 8);
    }

    // ... (Sisa method updateDice, setStatus dll SAMA) ...
    public void updateDice(int val, int mod) {
        dicePanel.setValue(val);
        dirLabel.setText(mod == 1 ? "▲" : "▼");
        dirLabel.setForeground(mod == 1 ? new Color(34, 139, 34) : new Color(178, 34, 34));
    }

    public void setStatus(String t, Color c) { statusLabel.setText(t); statusLabel.setForeground(c); }
    public void setPath(String t) { pathArea.setText(t); }
    public void toggleBtn(boolean b) { rollBtn.setEnabled(b); }
}