package ru.ashepelev.ord;

import ru.ashepelev.common.Placer;
import ru.ashepelev.dto.Edge;
import ru.ashepelev.dto.Graph;
import ru.ashepelev.dto.Node;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import static java.lang.Integer.MAX_VALUE;
import static java.lang.Integer.compare;
import static java.lang.Math.*;
import static java.util.Collections.swap;
import static java.util.Comparator.comparingInt;
import static java.util.Comparator.reverseOrder;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Stream.concat;
import static ru.ashepelev.dto.Node.dummy;

public class OrdPlacer implements Placer {
    private final Graph graph;
    private final int X_SCALE = 1000;
    private final int Y_SCALE = 1000;
    private int W;

    public OrdPlacer(Graph graph) {
        this.graph = graph;
    }

    @Override
    public void place() {
        // Топологически отсортируем вершины
        sort();
        // Расположим граф методом dummy вершин
        placeDummy();
        // Перевернем граф для удобства и увеличим абсолюьные значения координаты, чтобы удобно целочисленно делить
        flipAndScale();
        // Минимизируем число пересечений ребер
        minimizeIntersectionCount();
    }


    public void placeGraph(int W) {
        this.W = W;
        // Топологически отсортируем вершины
        sort();
        // Расположим граф методом Грэхема—Коффмана
        placeCoffman(W);
        // Перевернем граф для удобства и увеличим абсолюьные значения координаты, чтобы удобно целочисленно делить
        flipAndScale();
        // Минимизируем число пересечений ребер
        minimizeIntersectionCount();
        // Уплотним dummy вершины, чтобы ширина слоя была <= W + 2 (dummy могут быть снаружи от нормальных вершин)
        compress();
    }

    // Компаратор для топологической сортировки вершин
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

        // Найдем id вершин без предков (все остальные)
        var rootIds = graph.nodes.keySet().stream()
                .filter(key -> !childIds.contains(key))
                .collect(toList());

        // Заведем счетчик лэйблов
        int curMark = 0;

        // Промаркеруем вершины без родителей
        for (String key : rootIds) graph.nodes.get(key).ordMark = curMark++;

        // Выберем вершины с родителями, которые пока еще не промаркерованы
        var noMarkChildren = childIds.stream()
                .map(id -> graph.nodes.get(id))
                .collect(toList());

        // Пока остались немаркерованные вершины
        while (!noMarkChildren.isEmpty()) {
            // Отсортируем
            noMarkChildren.sort(new NodeComparator());
            // Промаркеруем первую
            noMarkChildren.get(0).ordMark = curMark++;
            // Удалим из списка немаркерованных
            noMarkChildren.remove(0);
        }
    }

    private void placeCoffman(int W) {
        // Возмем вершины с топологическом порядке
        var nodesInMarkedOrder = nodesInMarkedOrder();

        // Заведем массив с размерами слоев
        List<Integer> layerSizes = new ArrayList<>();

        // Проитерируемся по вершинам
        for (Node curNode : nodesInMarkedOrder) {
            int maxLayer = -1;

            // Найдем максимальный слой среди детей
            for (String id : curNode.childIds) {
                maxLayer = max(maxLayer, graph.nodes.get(id).y);
            }

            // Назначим себя на следующий
            curNode.y = maxLayer + 1;

            // Будем поднимать себя выше, пока не найдем места
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

            // Назначим себе x равный номеру в слое
            curNode.x = layerSizes.get(curNode.y);
        }
    }

    private void placeDummy() {
        // Прогоним алгоритм Грэхема—Коффмана без ограничений на ширину
        placeCoffman(MAX_VALUE);

        // Возмем вершины с топологическом порядке
        var nodesInMarkedOrder = nodesInMarkedOrder();

        // Проитерируемся по вершинам
        for (int i = 1; i < nodesInMarkedOrder.size(); ++i) {
            Node curNode = nodesInMarkedOrder.get(i);

            // Найдем число детей
            int childCount = nodesInMarkedOrder.get(i).childIds.size();
            var parents = parentsOf(curNode);

            //Найдем число родителей
            long parentCount = parents.size();

            // Если детей >= родителей, тогда вершина уже расмоложена оптимально
            if (childCount >= parentCount) continue;

            // Иначе поднимем ее до y самого низкого родителя минус 1
            curNode.y = parents.stream().min(comparingInt((Node a) -> a.y)).get().y - 1;
        }
    }

    // Перевернем граф для удобства и увеличим абсолюьные значения координаты, чтобы удобно целочисленно делить
    private void flipAndScale() {
        graph.nodes.values().forEach(node -> {
            node.x = node.x * X_SCALE;
            node.y = -node.y * Y_SCALE;
        });
    }

    private void minimizeIntersectionCount() {
        // Добавим в граф dummy вершины
        addDummies();
        // По слойно расположим вершины на равном расстоянии
        stretch();
        // Послойно переставим вершины так, чтобы минимизаровать число пересечений
        reorder();
    }

    private void addDummies() {
        int dummyCount = 0;

        // Проитерируемся по ребрам
        for (Edge edge : graph.edge) {
            Node source = graph.nodes.get(edge.source);
            Node target = graph.nodes.get(edge.target);

            // Скипнем ребро, если его концы лежат на соседних уровнях
            if (abs(source.y - target.y) == 1) continue;

            // Проитерируемся про промежуточным уровням, расставим, создадим там dummy вершины с x, средним между началом и концом
            Node dummy = source;
            for (int y = source.y + Y_SCALE; y < target.y; y += Y_SCALE) {
                String id = "dummy" + dummyCount++;
                dummy.childIds.add(id);
                dummy = dummy(id, (target.x + source.x) / 2, y);
                graph.nodes.put(id, dummy);
            }
            dummy.childIds.add(target.id);

            // Удалим конец ребра из списка детей начала ребра
            source.childIds.remove(target.id);
        }
    }

    // По слойно расположим вершины на равном расстоянии (тут все просто)
    private void stretch() {
        graph.nodes.values().stream()
                .map(node -> node.y)
                .distinct()
                .forEach(level -> {
                    List<Node> nodesOnLevel = getNodesOnLevel(level);
                    int lastX = 0;
                    for (Node node : nodesOnLevel) {
                        node.x = lastX;
                        lastX += X_SCALE;
                    }
                });
    }

    // Уплотним dummy вершины, чтобы ширина слоя была <= W + 2 (dummy могут быть снаружи от нормальных вершин)
    private void compress() {
        graph.nodes.values().stream()
                .map(node -> node.y)
                .distinct()
                // Итрерируемся по слоям
                .forEach(level -> {
                    List<Node> nodesOnLevel = getNodesOnLevel(level);
                    int lastX = 0;
                    List<Node> dummies = new ArrayList<>();
                    for (Node node : nodesOnLevel) {
                        if (node.isDummy) {
                            // Набираем dummy вершины между нормальными
                            dummies.add(node);
                        } else {
                            // Если очередная вершины нормальная, тогда равномерно распределям dummy междуу предыдущей нормальной и текущей
                            compressDummies(lastX, dummies);
                            node.x = lastX += X_SCALE;
                        }
                    }
                    if (!dummies.isEmpty()) {
                        compressDummies(lastX, dummies);
                    }
                });
    }

    // Равномерно распределям dummy междуу предыдущей нормальной и текущей
    private void compressDummies(int lastX, List<Node> dummies) {
        int dx = X_SCALE / (dummies.size() + 1);
        for (int i = 0; i < dummies.size(); ++i) {
            dummies.get(i).x = lastX + dx * (i + 1);
        }
        dummies.clear();
    }

    private void reorder() {
        // Найдем все уровни
        var levels = graph.nodes.values().stream()
                .map(node -> node.y)
                .distinct()
                .sorted()
                .collect(toList());

        // Проитерируемся по уровням
        for (int i = 1; i < levels.size(); i++) {
            int level = levels.get(i);
            // Найдем вершины на этом уровне
            List<Node> nodesOnLevel = getNodesOnLevel(level);

            // Сделаем n^2 итераций по оптимизации количества пересечений, где n -- число вершин на уровне
            for (int h = 0; h < nodesOnLevel.size(); ++h) {
                for (int j = 0; j < nodesOnLevel.size() - 1; j++) {
                    Node node1 = nodesOnLevel.get(j);
                    Node node2 = nodesOnLevel.get(j + 1);

                    // Попробуем переставить вершины местами, если стало лучше, оставим, если нет -- вернем
                    // Здесь передаем только родителей вершин, то есть переставляем детей
                    // при зафиксированных родителях, идем сверху вниз
                    optimizeIntersections(nodesOnLevel, j, node1, node2, parentsOf(node1), parentsOf(node2));
                }
            }
        }

        // Еще раз проитерируемся по уровням
        for (int i = levels.size() - 1; i > 0; i--) {
            int level = levels.get(i);
            // Найдем вершины на этом уровне
            List<Node> nodesOnLevel = getNodesOnLevel(level);

            // Сделаем n^2 итераций по оптимизации количества пересечений, где n -- число вершин на уровне
            for (int h = 0; h < nodesOnLevel.size(); ++h) {
                for (int j = 0; j < nodesOnLevel.size() - 1; j++) {
                    Node node1 = nodesOnLevel.get(j);
                    Node node2 = nodesOnLevel.get(j + 1);
                    var node1Neighbors = concat(
                            node1.childIds.stream().map(id -> graph.nodes.get(id)),
                            parentsOf(node1).stream()
                    ).collect(toList());
                    var node2Neighbors = concat(
                            node2.childIds.stream().map(id -> graph.nodes.get(id)),
                            parentsOf(node2).stream()
                    ).collect(toList());

                    // Попробуем переставить вершины местами, если стало лучше, оставим, если нет -- вернем
                    // Здесь передаем все соседей вершин, то есть переставляем вершины
                    // при зафиксированных родителях и дететях, считаем пересечения и снизу и сверху, идем снизу вверх
                    optimizeIntersections(nodesOnLevel, j, node1, node2, node1Neighbors, node2Neighbors);
                }
            }
        }
    }

    private List<Node> getNodesOnLevel(int level) {
        return graph.nodes.values().stream()
                .filter(node -> node.y == level)
                .sorted(comparingInt((Node a) -> a.x))
                .collect(toList());
    }

    private void optimizeIntersections(List<Node> nodesOnLevel, int j, Node node1, Node node2, List<Node> node1Neighbors, List<Node> node2Neighbors) {
        int intersectionCount = 0;
        int invertIntersectionCount = 0;

        // Итерируемся по всем переданным ребрам
        for (Node n1 : node1Neighbors) {
            for (Node n2 : node2Neighbors) {
                // Если вторые конца ребер лежат на разных уровнях, скипаем
                if (n1.y != n2.y) continue;
                // Считаем число пересечений сейчас
                intersectionCount += node1.x - node2.x > 0 == n1.x - n2.x < 0 ? 1 : 0;
                // И если переставить
                invertIntersectionCount += node1.x - node2.x < 0 == n1.x - n2.x < 0 ? 1 : 0;
            }
        }

        // Если стало лучше, переставляем вершины местами
        if (invertIntersectionCount < intersectionCount) {
            int tmp = node1.x;
            node1.x = node2.x;
            node2.x = tmp;
            swap(nodesOnLevel, j, j + 1);
        }
    }

    private List<Node> nodesInMarkedOrder() {
        return graph.nodes.values().stream()
                .sorted((Node a, Node b) -> compare(b.ordMark, a.ordMark))
                .collect(toList());
    }

    private List<Node> parentsOf(Node curNode) {
        return graph.nodes.values().stream()
                .filter(node -> node.childIds.contains(curNode.id)).collect(toList());
    }
}
