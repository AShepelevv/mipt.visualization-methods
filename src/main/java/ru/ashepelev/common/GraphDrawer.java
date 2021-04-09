package ru.ashepelev.common;

import ru.ashepelev.dto.Graph;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.AffineTransform;

import static java.awt.Color.BLACK;
import static java.lang.Integer.MAX_VALUE;
import static java.lang.Integer.MIN_VALUE;
import static javax.swing.WindowConstants.EXIT_ON_CLOSE;

public class GraphDrawer extends JPanel {
    private final JFrame frame = new JFrame("Plotting result");
    private Graph graph;
    private final int PADDING = 50;
    private final int RADIUS = 3;
    private final int SIZE_X = 1000;
    private final int SIZE_Y = 1000;
    private final int ARR_SIZE = 21;
    private final int SHIFT = 10;

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

        graph.nodes.values().forEach(node -> {
            if (node.isDummy) return;
            int x = (int) ((node.x - min_x) * scale_x - RADIUS) + PADDING;
            int y = (int) ((node.y - min_y) * scale_y - RADIUS) + PADDING;
            g.fillRoundRect(x, y, 2 * RADIUS, 2 * RADIUS, RADIUS, RADIUS);

            var g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON);
            Font font = new Font("Serif", Font.PLAIN, 10);
            g2.setFont(font);

//            g2.drawString(node.id, 40, 120);
//            System.out.println(node.x + " " + node.y);
            g2.drawString(node.id, x + SHIFT, y + SHIFT / 2);
        });


        graph.nodes.values().forEach(node -> node.childIds.forEach(childId -> {
            int x1 = (int) ((graph.nodes.get(node.id).x - min_x) * scale_x) + PADDING;
            int y1 = (int) ((graph.nodes.get(node.id).y - min_y) * scale_y) + PADDING;
            int x2 = (int) ((graph.nodes.get(childId).x - min_x) * scale_x) + PADDING;
            int y2 = (int) ((graph.nodes.get(childId).y - min_y) * scale_y) + PADDING;
            if (graph.nodes.get(childId).isDummy) {
                g.drawLine(x1, y1, x2, y2);
            } else {
                drawArrow(g,x1, y1, x2, y2);
            }
        }));
    }

    private void drawArrow(Graphics g1, int x1, int y1, int x2, int y2) {
        Graphics2D g = (Graphics2D) g1.create();

        double dx = x2 - x1;
        double dy = y2 - y1;
        double angle = Math.atan2(dy, dx);
        int len = (int) Math.sqrt(dx * dx + dy * dy);
        AffineTransform at = AffineTransform.getTranslateInstance(x1, y1);
        at.concatenate(AffineTransform.getRotateInstance(angle));
        g.transform(at);

        // Draw horizontal arrow starting in (0, 0)
        g.drawLine(0, 0, len, 0);
        g.fillPolygon(new int[]{len, len - ARR_SIZE, len - ARR_SIZE, len},
                new int[]{0, -ARR_SIZE / 7, ARR_SIZE / 7, 0}, 4);
    }
}
