package ru.ashepelev;

import lombok.RequiredArgsConstructor;
import ru.ashepelev.dto.Graph;
import ru.ashepelev.dto.Node;

import java.util.Map;
import java.util.function.Consumer;

import static java.lang.Integer.MAX_VALUE;
import static java.lang.Integer.MIN_VALUE;
import static java.lang.Math.max;
import static java.lang.Math.min;
import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.toMap;

public class GraphWorker {
    private final Graph graph;
    private final int SCALE_X = 6;
    private final int SCALE_Y = 1;

    public GraphWorker(Graph graph) {
        this.graph = graph;
    }

    public void placeGraph() {
        Node root = getRoot();
        primary_place(root, 0, 0);
        avg_place(root);
        compress(root);
    }

    private Node getRoot() {
        return graph.nodes.values().stream().filter(n -> n.parentId == null).findFirst().orElse(null);
    }

    private int primary_place(Node root, int x, int y) {
        if (root.childIds.isEmpty()) {
            root.x = x * SCALE_X;
            root.y = y * SCALE_Y;
            return x + 1;
        }
        int i = 0;
        int root_x = x;
        for (; i < root.childIds.size() / 2; i++) {
            root_x = primary_place(this.graph.nodes.get(root.childIds.get(i)), root_x, y + 1);
        }

        root.x = root_x++ * SCALE_X;
        root.y = y * SCALE_Y;

        for (; i < root.childIds.size(); i++) {
            root_x = primary_place(graph.nodes.get(root.childIds.get(i)), root_x, y + 1);
        }
        return root_x;
    }

    private void avg_place(Node root) {
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

    private void compress(Node root) {
        if (root.childIds.isEmpty()) {
            root.x_left.put(root.y, root.x);
            root.x_right.put(root.y, root.x);
            return;
        }

        root.childIds.stream()
                .map(id -> graph.nodes.get(id))
                .forEach(this::compress);

        Node firstChild = graph.nodes.get(root.childIds.get(0));
        Node lastChild = graph.nodes.get(root.childIds.get(root.childIds.size() - 1));

        root.x_left = firstChild.x_left;
        root.x_right = firstChild.x_right;

        for (int i = 1; i < root.childIds.size(); ++i) {
            Node curChild = graph.nodes.get(root.childIds.get(i));

            int shift = curChild.x_left.entrySet().stream()
                    .map(cur -> cur.getValue() - ofNullable(root.x_right.get(cur.getKey())).orElse(MIN_VALUE / 2))
                    .reduce(MAX_VALUE / 2, Math::min) - SCALE_X;

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

    private void dfs(Node root, Consumer<Node> consumer) {
        consumer.accept(root);
        root.childIds.forEach(childId -> dfs(graph.nodes.get(childId), consumer));
    }
}
