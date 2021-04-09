package ru.ashepelev.ord;

import ru.ashepelev.common.GraphPlacer;
import ru.ashepelev.dto.Edge;
import ru.ashepelev.dto.Graph;
import ru.ashepelev.dto.Node;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.lang.Integer.MAX_VALUE;
import static java.lang.Math.*;
import static java.util.Collections.swap;
import static java.util.Comparator.comparingInt;
import static java.util.Comparator.reverseOrder;
import static java.util.stream.Collectors.toList;
import static ru.ashepelev.dto.Node.dummy;

public class OrdPlacer implements GraphPlacer {
    private final Graph graph;
    private final int X_SCALE = 10;
    private final int Y_SCALE = 10;
    private int W;

    public OrdPlacer(Graph graph) {
        this.graph = graph;
    }

    @Override
    public void placeGraph() {
        // Отсортируем вершины
        sort();
        placeDummy();
        flipAndScale();
        minimizeDummy();
    }


    public void placeGraph(int W) {
        this.W = W;
        sort();
        placeCoffman(W);
        flipAndScale();
        minimizeDummy();
    }

    private class NodeComparator implements Comparator<Node> {
        @Override
        public int compare(Node a, Node b) {
            var aParentMarks = graph.nodes.values().stream()
                    .filter(node -> node.childIds.contains(a.id))
                    .map(node -> node.ordMark)
                    .sorted(reverseOrder())
                    .collect(toList());
            var bParentMarks = graph.nodes.values().stream()
                    .filter(node -> node.childIds.contains(b.id))
                    .map(node -> node.ordMark)
                    .sorted(reverseOrder())
                    .collect(toList());

            for (int i = 0; i < min(aParentMarks.size(), bParentMarks.size()); ++i) {
                if (aParentMarks.get(i) < bParentMarks.get(i)) return -1;
                if (aParentMarks.get(i) > bParentMarks.get(i)) return 1;
            }

            return Integer.compare(aParentMarks.size(), bParentMarks.size());
        }
    }

    private void sort() {
        // Получим множество id вершин с родителями
        var childIds = graph.nodes.values().stream()
                .flatMap(node -> node.childIds.stream())
                .collect(Collectors.toSet());

        // Найдем id вершин без предков
        var rootIds = graph.nodes.keySet().stream()
                .filter(key -> !childIds.contains(key))
                .collect(toList());

        // Заведем счетчик лэйблов
        int curMark = 0;

        // Промаркеруем вершины без родителей
        for (String key : rootIds) graph.nodes.get(key).ordMark = curMark++;

        // Выберем вершины с родителями, которые пока еще не промаркерованы
        var nodesWithParents = childIds.stream()
                .map(id -> graph.nodes.get(id))
                .collect(toList());

        // Пока остались немаркерованные вершины
        while (!nodesWithParents.isEmpty()) {
            // Отсортируем
            nodesWithParents.sort(new NodeComparator());
            // Промаркеруем первую
            nodesWithParents.get(0).ordMark = curMark++;
            // Удалим из списка немаркерованных
            nodesWithParents.remove(0);
        }
    }

    private void placeCoffman(int W) {
        var nodesInMarkedOrder = nodesInMarkedOrder();

        List<Integer> layerSizes = new ArrayList<>();

        for (Node curNode : nodesInMarkedOrder) {
            int maxLayer = -1;
            for (String id : curNode.childIds) {
                maxLayer = max(maxLayer, graph.nodes.get(id).y);
            }
            curNode.y = maxLayer + 1;
            while (true) {
                if (curNode.y >= layerSizes.size()) {
                    layerSizes.add(1);
                    break;
                } else if (layerSizes.get(curNode.y) >= W) {
                    ++curNode.y;
                } else {
                    layerSizes.set(curNode.y, layerSizes.get(curNode.y) + 1);
                    break;
                }
            }
            curNode.x = layerSizes.get(curNode.y);
        }
    }

    private void placeDummy() {
        placeCoffman(MAX_VALUE);
        var nodesInMarkedOrder = nodesInMarkedOrder();
        for (int i = 1; i < nodesInMarkedOrder.size(); ++i) {
            Node curNode = nodesInMarkedOrder.get(i);
            int childCount = nodesInMarkedOrder.get(i).childIds.size();
            var parents = parentsOf(curNode);
            long parentCount = parents.size();
            if (childCount >= parentCount) continue;
            curNode.y = parents.stream().min(comparingInt((Node a) -> a.y)).get().y - 1;
        }
    }

    private void flipAndScale() {
        graph.nodes.values().forEach(node -> {
            node.x = node.x * X_SCALE;
            node.y = -node.y * Y_SCALE;
        });
    }

    private void minimizeDummy() {
        addDummies();
        stretch();
        reorder();
    }

    private void addDummies() {
        int dummyCount = 0;
        for (Edge edge : graph.edge) {
            Node source = graph.nodes.get(edge.source);
            Node target = graph.nodes.get(edge.target);
            if (abs(source.y - target.y) == 1) continue;

            Node dummy = source;

            for (int y = source.y + Y_SCALE; y < target.y; y += Y_SCALE) {
                String id = "dummy" + dummyCount++;
                dummy.childIds.add(id);
                dummy = dummy(id, (target.x + source.x) / 2, y);
                graph.nodes.put(id, dummy);

            }
            dummy.childIds.add(target.id);
            source.childIds.remove(target.id);
        }
    }

    private void stretch() {
        graph.nodes.values().stream()
                .map(node -> node.y)
                .distinct()
                .forEach(level -> {
                    var nodesOnLevel = graph.nodes.values().stream()
                            .filter(node -> node.y == level)
                            .sorted(comparingInt((Node a) -> a.x))
                            .collect(toList());
                    int lastX = 0;
                    for (Node node : nodesOnLevel) {
                        node.x = lastX;
                        lastX += X_SCALE;
                    }
                });
    }

    private void reorder() {
        var levels = graph.nodes.values().stream()
                .map(node -> node.y)
                .distinct()
                .sorted(reverseOrder())
                .collect(toList());

        for (int level : levels) {
            var nodesOnLevel = graph.nodes.values().stream()
                    .filter(node -> node.y == level)
                    .sorted(comparingInt((Node a) -> a.x))
                    .collect(toList());

            int iteration = 0;
            while (iteration++ < 100) {
                for (int j = 0; j < nodesOnLevel.size() - 1; j++) {
                    Node node1 = nodesOnLevel.get(j);
                    Node node2 = nodesOnLevel.get(j + 1);
                    var node1ParentXs = parentsOf(node1).stream()
                            .map(node -> node.x)
                            .collect(toList());
                    var node2ParentXs = parentsOf(node2).stream()
                            .map(node -> node.x)
                            .collect(toList());

                    optimizeIntersections(nodesOnLevel, j, node1, node2, node1ParentXs, node2ParentXs);
                }
            }

            iteration = 0;
            while (iteration++ < 100) {
                for (int j = 0; j < nodesOnLevel.size() - 1; j++) {
                    Node node1 = nodesOnLevel.get(j);
                    Node node2 = nodesOnLevel.get(j + 1);
                    var node1ChildsXs = node1.childIds.stream()
                            .map(id -> graph.nodes.get(id).x)
                            .collect(toList());
                    var node2ChildsXs = node2.childIds.stream()
                            .map(id -> graph.nodes.get(id).x)
                            .collect(toList());

                    optimizeIntersections(nodesOnLevel, j, node1, node2, node1ChildsXs, node2ChildsXs);
                }
            }
        }
    }

    private void optimizeIntersections(List<Node> nodesOnLevel, int j, Node node1, Node node2, List<Integer> node1NeighboursXs, List<Integer> node2NeighboursXs) {
        int intersectionCount = 0;
        int invertIntersectionCount = 0;

        for (int x1 : node1NeighboursXs) {
            for (int x2 : node2NeighboursXs) {
                intersectionCount += node1.x - node2.x > 0 == x1 - x2 < 0 ? 1 : 0;
                invertIntersectionCount += node1.x - node2.x < 0 == x1 - x2 < 0 ? 1 : 0;
            }
        }

        if (invertIntersectionCount < intersectionCount) {
            int tmp = node1.x;
            node1.x = node2.x;
            node2.x = tmp;
            swap(nodesOnLevel, j, j + 1);
        }
    }

    private List<Node> nodesInMarkedOrder() {
        return graph.nodes.values().stream()
                .sorted((Node a, Node b) -> Integer.compare(b.ordMark, a.ordMark))
                .collect(toList());
    }

    private List<Node> parentsOf(Node curNode) {
        return graph.nodes.values().stream()
                .filter(node -> node.childIds.contains(curNode.id)).collect(toList());
    }
}
