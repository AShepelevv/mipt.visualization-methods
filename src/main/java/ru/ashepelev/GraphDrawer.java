package ru.ashepelev;

import ru.ashepelev.dto.Graph;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowEvent;
import java.io.File;

import static java.awt.Color.BLACK;
import static java.awt.event.WindowEvent.WINDOW_CLOSING;
import static java.lang.Integer.MAX_VALUE;
import static java.lang.Integer.MIN_VALUE;
import static javax.swing.WindowConstants.EXIT_ON_CLOSE;

public class GraphDrawer extends JPanel {
    private final JFrame frame = new JFrame("Plotting result");
    private Graph graph;
    private final int PADDING = 10;
    private final int RADIUS = 3;
    private final int SIZE_X = 500;
    private final int SIZE_Y = 500;

    public GraphDrawer() {
        Canvas canvas = new Canvas();
        canvas.setSize(SIZE_X, SIZE_Y);
        this.setSize(SIZE_X, SIZE_Y);
        frame.add(this);
        frame.add(canvas);
        frame.setDefaultCloseOperation(EXIT_ON_CLOSE);
        frame.pack();
        frame.setVisible(true);
    }

    public JFrame draw(Graph graph) {
        this.graph = graph;
        return frame;
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        g.setColor(BLACK);

        int min_x = graph.nodes.values().stream().map(n -> n.x).reduce(MAX_VALUE / 2, Math::min);
        int max_x = graph.nodes.values().stream().map(n -> n.x).reduce(MIN_VALUE / 2, Math::max);
        int min_y = graph.nodes.values().stream().map(n -> n.y).reduce(MAX_VALUE / 2, Math::min);
        int max_y = graph.nodes.values().stream().map(n -> n.y).reduce(MIN_VALUE / 2, Math::max);

        double scale_x = (SIZE_X - 2 * PADDING) / ((double) max_x - min_x);
        double scale_y = (SIZE_Y - 2 * PADDING) / ((double) max_y - min_y);

        graph.nodes.values().forEach(node -> g.fillRoundRect(
                (int) ((node.x - min_x) * scale_x - RADIUS) + PADDING,
                (int) ((node.y - min_y) * scale_y - RADIUS) + PADDING,
                2 * RADIUS,
                2 * RADIUS,
                RADIUS,
                RADIUS
        ));

        graph.edge.forEach(edge -> g.drawLine(
                (int) ((graph.nodes.get(edge.source).x - min_x) * scale_x) + PADDING,
                (int) ((graph.nodes.get(edge.source).y - min_y) * scale_y) + PADDING,
                (int) ((graph.nodes.get(edge.target).x - min_x) * scale_x) + PADDING,
                (int) ((graph.nodes.get(edge.target).y - min_y) * scale_y + PADDING)
        ));
    }
}
