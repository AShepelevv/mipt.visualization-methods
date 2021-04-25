package ru.ashepelev.label;

import javax.swing.*;
import java.awt.*;

import static java.awt.Color.BLACK;
import static javax.swing.WindowConstants.EXIT_ON_CLOSE;

public class LabelDrawer extends JPanel {
    private final JFrame frame = new JFrame("Plotting result");
    private LabeledPoints labeledPoints;
    private final int RADIUS = 2;
    private final int SIZE_X = 500;
    private final int SIZE_Y = 500;
    private final int PADDING = 0;

    public LabelDrawer() {
        Canvas canvas = new Canvas();
        canvas.setSize(SIZE_X + 2 * PADDING, SIZE_Y + 2 * PADDING);
        this.setSize(SIZE_X + 2 * PADDING, SIZE_Y + 2 * PADDING);
        frame.add(this);
        frame.add(canvas);
        frame.setDefaultCloseOperation(EXIT_ON_CLOSE);
        frame.pack();
        frame.setVisible(true);
    }

    public JFrame draw(LabeledPoints labeledPoints) {
        this.labeledPoints = labeledPoints;
        return frame;
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);

        g.setColor(Color.CYAN);

        labeledPoints.graph.nodes.stream()
                .filter(node -> node.isActual)
                .forEach(node -> g.fillRect(node.minX + PADDING, node.minY + PADDING, node.maxX - node.minX, node.maxY - node.minY));

        g.setColor(BLACK);

//        g.drawRect(PADDING, PADDING, SIZE_X, SIZE_Y);

        labeledPoints.points.forEach(point ->
                g.fillRect(point.x - RADIUS + PADDING, point.y - RADIUS + PADDING, 2 * RADIUS, 2 * RADIUS));
    }
}
