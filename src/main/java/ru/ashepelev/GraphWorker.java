package ru.ashepelev;

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

        //Получим корень дерева
        Node root = getRoot();

        // Первично расположим дерево: y - уровень, x - номер в обходе dfs
        primary_place(root, 0, 0);

        // Сдвинем дерево по-плотнее
        compress(root);
    }

    // Получение корня дерева
    private Node getRoot() {
        return graph.nodes.values().stream().filter(n -> n.parentId == null).findFirst().orElse(null);
    }

    // Первично расположим дерево: y - уровень, x - номер в обходе dfs
    private int primary_place(Node root, int x, int y) {
        // Если лист, установим координаты SCALE_X и SCALE_Y используются для удобного целочисленного деления
        if (root.childIds.isEmpty()) {
            root.x = x * SCALE_X;
            root.y = y * SCALE_Y;
            return x + 1;
        }

        // Если не лист, расположим половину детей рекурсивно (живем в парадигме произвольного дерева)
        int i = 0;
        int root_x = x;
        for (; i < root.childIds.size() / 2; i++) {
            root_x = primary_place(this.graph.nodes.get(root.childIds.get(i)), root_x, y + 1);
        }

        // Затем расположим себя
        root.x = root_x++ * SCALE_X;
        root.y = y * SCALE_Y;

        // Наконец расположим остальных детей
        for (; i < root.childIds.size(); i++) {
            root_x = primary_place(graph.nodes.get(root.childIds.get(i)), root_x, y + 1);
        }

        // Вернем координату самого правого листа
        return root_x;
    }

    private void compress(Node root) {

        //Если лист, оставим на месте
        if (root.childIds.isEmpty()) {
            root.x_left.put(root.y, root.x);
            root.x_right.put(root.y, root.x);
            return;
        }

        // Запустим сжатие для сначала для детей
        root.childIds.stream()
                .map(id -> graph.nodes.get(id))
                .forEach(this::compress);

        //Найдем самого левого и самого правого детей
        Node firstChild = graph.nodes.get(root.childIds.get(0));
        Node lastChild = graph.nodes.get(root.childIds.get(root.childIds.size() - 1));

        // Получим границы первого ребенка
        root.x_left = firstChild.x_left;
        root.x_right = firstChild.x_right;

        // Проитерируемся по детям, начиная со второго
        for (int i = 1; i < root.childIds.size(); ++i) {
            Node curChild = graph.nodes.get(root.childIds.get(i));

            // Найдем величину, на сколько можно сдвинуть текущего ребенка к предыдущим. Пройдем по слоям,
            // найдем расстояние между поддеревьями на каждом уровне. Возьмем минимум.
            int shift = curChild.x_left.entrySet().stream()
                    .map(cur -> cur.getValue() - ofNullable(root.x_right.get(cur.getKey())).orElse(MIN_VALUE / 2))
                    .reduce(MAX_VALUE / 2, Math::min) - SCALE_X;


            // Рекурсивно подвинем текущего ребенка вместе с поддеревом влево к предыдущим
            dfs(graph.nodes.get(root.childIds.get(i)), node -> {
                node.x -= shift;
                node.x_left = node.x_left.entrySet().stream()
                        .collect(toMap(Map.Entry::getKey, entry -> entry.getValue() - shift));
                node.x_right = node.x_right.entrySet().stream()
                        .collect(toMap(Map.Entry::getKey, entry -> entry.getValue() - shift));
            });


            // Обновим границы текущего поддерева
            curChild.x_left.forEach((key, value) ->
                    root.x_left.put(key, min(ofNullable(root.x_left.get(key)).orElse(MAX_VALUE / 2), value)));
            curChild.x_right.forEach((key, value) ->
                    root.x_right.put(key, max(ofNullable(root.x_right.get(key)).orElse(MIN_VALUE / 2), value)));
        }

        // После сжатия поддеревьев расположим вершину по середине между крайними детьми
        root.x = (firstChild.x + lastChild.x) / 2;
        root.x_left.put(root.y, root.x);
        root.x_right.put(root.y, root.x);
    }

    private void dfs(Node root, Consumer<Node> consumer) {
        consumer.accept(root);
        root.childIds.forEach(childId -> dfs(graph.nodes.get(childId), consumer));
    }
}
