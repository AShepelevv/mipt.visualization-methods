package ru.ashepelev;

import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import org.apache.commons.io.FileUtils;
import org.springframework.core.io.ClassPathResource;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowEvent;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import static java.awt.Color.BLACK;
import static java.awt.event.WindowEvent.WINDOW_CLOSING;
import static java.lang.Integer.MAX_VALUE;
import static java.lang.Integer.MIN_VALUE;
import static java.lang.Math.max;
import static java.lang.Math.min;
import static java.util.Optional.ofNullable;
import static java.util.function.Function.identity;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;
import static javax.swing.WindowConstants.EXIT_ON_CLOSE;

public class GraphicsExporter extends JPanel {
    private final JFrame frame = new JFrame("Plotting result");
    private static Graph graph;
    private static int SCALE_X = 20;
    private static int SCALE_Y = 50;
    private static final int PADDING = 10;
    private static final int RADIUS = 3;
    private static final int MIN_SHIFT = SCALE_X;
    private static final int SIZE_X = 1_000;
    private static final int SIZE_Y = 700;

    public GraphicsExporter() {
        Canvas canvas = new Canvas();
        canvas.setSize(SIZE_X, SIZE_Y);
        this.setSize(SIZE_X, SIZE_Y);
        frame.add(this);
        frame.add(canvas);
        frame.setDefaultCloseOperation(EXIT_ON_CLOSE);
        frame.pack();
        frame.setVisible(true);
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

        System.out.println(scale_x + " " + scale_y);

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

    public static void save(JFrame frame) {
        try {
            Rectangle rectangle = new Rectangle(frame.getBounds());
            File file = new File("plotting_result.png");
            ImageIO.write(
                    new Robot().createScreenCapture(rectangle),
                    "png",
                    file);
            System.out.println("File Path: " + file.getAbsolutePath());
        } catch (Exception exception) {
            System.out.println("Something went wrong :(");
            exception.printStackTrace();
        }
    }

    public static void main(String[] args) throws Exception {
        File source = new ClassPathResource(
                "graph.xml").getFile();

        XmlMapper xmlMapper = new XmlMapper();
//

        String xml = FileUtils.readFileToString(source, StandardCharsets.UTF_8);
        graph = xmlMapper.readValue(xml, GraphML.class).getGraph();
        graph.nodes = graph.node.stream().collect(toMap(n -> n.id, identity()));
        graph.edge.forEach(edge -> {
            graph.nodes.get(edge.source).childIds.add(edge.target);
            graph.nodes.get(edge.target).parentId = edge.source;
        });
        Node root = graph.nodes.values().stream().filter(n -> n.parentId == null).findFirst().orElse(null);
        primary_place(root, 0, 0);
        avg_place(root);
        compress(root);

        var frame = new GraphicsExporter().frame;
        save(frame);
        frame.dispatchEvent(new WindowEvent(frame, WINDOW_CLOSING));
    }

    private static int primary_place(Node root, int x, int y) {
        if (root.childIds.isEmpty()) {
            root.x = x * SCALE_X;
            root.y = y * SCALE_Y;
            return x + 1;
        }
        int i = 0;
        int root_x = x;
        for (; i < root.childIds.size() / 2; i++) {
            root_x = primary_place(graph.nodes.get(root.childIds.get(i)), root_x, y + 1);
        }

        root.x = root_x++ * SCALE_X;
        root.y = y * SCALE_Y;

        for (; i < root.childIds.size(); i++) {
            root_x = primary_place(graph.nodes.get(root.childIds.get(i)), root_x, y + 1);
        }
        return root_x;
    }

    private static void avg_place(Node root) {
        if (root.childIds.isEmpty()) {
            return;
        }
        int min = MAX_VALUE / 100;
        int max = MIN_VALUE / 100;
        for (String childId : root.childIds) {
            avg_place(graph.nodes.get(childId));
            min = Math.min(min, graph.nodes.get(childId).x);
            max = max(max, graph.nodes.get(childId).x);
        }
        root.x = (max + min) / 2;
    }

    private static void compress(Node root) {
        if (root.childIds.isEmpty()) {
            root.x_left.put(root.y, root.x);
            root.x_right.put(root.y, root.x);
            return;
        }

        root.childIds.stream()
                .map(id -> graph.nodes.get(id))
                .forEach(GraphicsExporter::compress);

        Node firstChild = graph.nodes.get(root.childIds.get(0));
        Node lastChild = graph.nodes.get(root.childIds.get(root.childIds.size() - 1));

        root.x_left = firstChild.x_left;
        root.x_right = firstChild.x_right;

        for (int i = 1; i < root.childIds.size(); ++i) {
            Node curChild = graph.nodes.get(root.childIds.get(i));

            int shift = curChild.x_left.entrySet().stream()
                    .map(cur -> cur.getValue() - ofNullable(root.x_right.get(cur.getKey())).orElse(MIN_VALUE / 2))
                    .reduce(MAX_VALUE / 2, Math::min) - MIN_SHIFT;

            dfs(graph.nodes.get(root.childIds.get(i)), node -> {
                node.x -= shift;
                node.x_left = node.x_left.entrySet().stream()
                        .collect(toMap(Map.Entry::getKey, entry -> entry.getValue() - shift));
                node.x_right = node.x_right.entrySet().stream()
                        .collect(toMap(Map.Entry::getKey, entry -> entry.getValue() - shift));
            });

            curChild.x_left.forEach((key, value) ->
                    root.x_left.put(key, min(ofNullable(root.x_left.get(key)).orElse(MAX_VALUE / 2), value)));

            curChild.x_right.forEach((key, value) ->
                    root.x_right.put(key, max(ofNullable(root.x_right.get(key)).orElse(MIN_VALUE / 2), value)));
        }

        root.x = (firstChild.x + lastChild.x) / 2;
        root.x_left.put(root.y, root.x);
        root.x_right.put(root.y, root.x);
    }

    private static void dfs(Node root, Consumer<Node> consumer) {
        consumer.accept(root);
        root.childIds.forEach(childId -> dfs(graph.nodes.get(childId), consumer));
    }
}
