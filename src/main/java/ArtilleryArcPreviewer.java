import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.List;

/// The goal of this class is to calculate the shooting arc of the artillery.
/// The artillery arc is the path that the artillery shell will take when fired.
/// The artillery arc must be calculated with the given parameters:
/// the server tickrate
/// the initial speed of the artillery shell
/// the gravity of the game
/// the pitch of the artillery barrel (in radians)
///     0.0 is horizontal (shooting straight, parallel to the ground)
///     + is shooting downwards
///     - is shooting upwards
/// also have a max and min pitch, where the max pitch is for example -1.4 radians and the min pitch is 0.8 radians
///
/// the server calculates the artillery shell's path using the following formula:
/// ```rust
/// let dt = 1.0 / tick_rate as f32;
/// velocity.y -= gravity * dt;
/// transform.translation += velocity * dt;
///```
///
/// The artillery arc previewer will calculate the artillery arc and display it on the screen.
/// it must have input fields for the parameters, and a button to calculate the arc.
/// it must display the arc on the screen as a line.
public class ArtilleryArcPreviewer extends JPanel {
    private final JTextField tickRateField;
    private final JTextField speedField;
    private final JTextField gravityField;
    private final JTextField pitchField;
    private final JButton calculateButton;
    private final JTextField gridWidthField;   // number of cells horizontally
    private final JTextField gridHeightField;  // number of cells vertically
    private final JTextField cellSizeField;    // pixel size per cell

    // Margin around the grid
    private final int margin = 40;

    // Store the arc points
    private List<Point2D.Double> arcPoints;

    public ArtilleryArcPreviewer() {
        setLayout(new BorderLayout());

        // Control panel at the top
        JPanel controlPanel = new JPanel(new FlowLayout());

        // Existing input fields
        tickRateField = new JTextField("5", 5);
        speedField = new JTextField("30", 5);
        gravityField = new JTextField("18", 5);
        pitchField = new JTextField("-0.8", 5);

        // New input fields
        gridWidthField = new JTextField("50", 5);
        gridHeightField = new JTextField("50", 5);
        cellSizeField = new JTextField("10", 5);

        calculateButton = new JButton("Calculate Arc");

        // Add components to the control panel
        controlPanel.add(new JLabel("Tick Rate:"));
        controlPanel.add(tickRateField);
        controlPanel.add(new JLabel("Initial Speed:"));
        controlPanel.add(speedField);
        controlPanel.add(new JLabel("Gravity:"));
        controlPanel.add(gravityField);
        controlPanel.add(new JLabel("Pitch (radians):"));
        controlPanel.add(pitchField);

        // New grid controls
        controlPanel.add(new JLabel("Grid Width:"));
        controlPanel.add(gridWidthField);
        controlPanel.add(new JLabel("Grid Height:"));
        controlPanel.add(gridHeightField);
        controlPanel.add(new JLabel("Cell Size(px):"));
        controlPanel.add(cellSizeField);

        controlPanel.add(calculateButton);
        add(controlPanel, BorderLayout.NORTH);

        arcPoints = new ArrayList<>();

        // Calculate the arc when the button is pressed
        calculateButton.addActionListener((ActionEvent e) -> {
            calculateArc();
            repaint();
        });
    }

    public static void main(String[] args) {
        JFrame frame = new JFrame("Artillery Arc Previewer");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(1000, 700);
        frame.add(new ArtilleryArcPreviewer());
        frame.setVisible(true);
    }

    // Calculate the projectile arc using your server simulation formula
    private void calculateArc() {
        try {
            double tickRate = Double.parseDouble(tickRateField.getText());
            double speed = Double.parseDouble(speedField.getText());
            double gravity = Double.parseDouble(gravityField.getText());
            double pitch = Double.parseDouble(pitchField.getText());

            double dt = 1.0 / tickRate;
            double vx = speed * Math.cos(pitch);
            double vy = -speed * Math.sin(pitch);

            double x = 0;
            double y = 0;
            arcPoints.clear();
            arcPoints.add(new Point2D.Double(x, y));

            for (int i = 0; i < 1000; i++) {
                vy -= gravity * dt;
                x += vx * dt;
                y += vy * dt;
                arcPoints.add(new Point2D.Double(x, y));

                // Break out if we go too far on X, just to limit the loop
                if (x > 1000) {
                    break;
                }
            }
        } catch (NumberFormatException ex) {
            arcPoints.clear();
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;

        // Read user-specified grid settings
        int userGridWidth, userGridHeight;
        double userCellSize;
        try {
            userGridWidth = Integer.parseInt(gridWidthField.getText());
            userGridHeight = Integer.parseInt(gridHeightField.getText());
            userCellSize = Double.parseDouble(cellSizeField.getText());
        } catch (NumberFormatException e) {
            // If invalid, just bail out
            return;
        }

        // The total pixel area of the grid (excluding the margin)
        int gridPixelWidth = (int) (userGridWidth * userCellSize);
        int gridPixelHeight = (int) (userGridHeight * userCellSize);

        // Draw grid lines
        g2.setColor(Color.LIGHT_GRAY);
        // Vertical lines
        for (int col = 0; col <= userGridWidth; col++) {
            int x = margin + (int) (col * userCellSize);
            g2.drawLine(x, margin, x, margin + gridPixelHeight);
        }
        // Horizontal lines
        for (int row = 0; row <= userGridHeight; row++) {
            int y = margin + (int) (row * userCellSize);
            g2.drawLine(margin, y, margin + gridPixelWidth, y);
        }

        // Draw coordinate labels
        g2.setColor(Color.BLACK);
        Font font = new Font("SansSerif", Font.PLAIN, 10);
        g2.setFont(font);

        // X labels (top)
        for (int col = 0; col <= userGridWidth; col++) {
            String label = String.valueOf(col);
            int x = margin + (int) (col * userCellSize);
            int labelWidth = g2.getFontMetrics().stringWidth(label);
            g2.drawString(label, x - labelWidth / 2, margin - 5);
        }
        // Y labels (left)
        for (int row = 0; row <= userGridHeight; row++) {
            // If you want Y=0 at bottom, invert: int labelVal = userGridHeight - row;
            int labelVal = row;
            String label = String.valueOf(labelVal);
            int y = margin + (int) (row * userCellSize);
            int labelWidth = g2.getFontMetrics().stringWidth(label);
            g2.drawString(label, margin - labelWidth - 5, y + 5);
        }

        // Draw the artillery arc
        if (arcPoints == null || arcPoints.size() < 2) {
            return;
        }
        g2.setColor(Color.RED);
        for (int i = 1; i < arcPoints.size(); i++) {
            Point2D.Double p1 = arcPoints.get(i - 1);
            Point2D.Double p2 = arcPoints.get(i);

            // Convert arc coordinates to screen coordinates
            // Note: Y=0 at top means we invert the Y coordinate
            int screenX1 = margin + (int) (p1.x * userCellSize);
            int screenY1 = margin + gridPixelHeight - (int) (p1.y * userCellSize);
            int screenX2 = margin + (int) (p2.x * userCellSize);
            int screenY2 = margin + gridPixelHeight - (int) (p2.y * userCellSize);

            g2.drawLine(screenX1, screenY1, screenX2, screenY2);
        }
    }
}