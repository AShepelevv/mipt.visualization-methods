package ru.ashepelev.label;

import ru.ashepelev.common.Placer;
import ru.ashepelev.label.graph.ImplicationNode;

import java.util.ArrayList;
import java.util.List;

import static java.util.Collections.reverse;
import static java.util.stream.Collectors.toList;

public class LabelPlacer implements Placer {
    private final LabeledPoints labeledPoints;
    private final List<Boolean> used = new ArrayList<>();
    private final List<Integer> order = new ArrayList<>();
    private final List<Integer> comp = new ArrayList<>();

    public LabelPlacer(LabeledPoints labeledPoints) {
        this.labeledPoints = labeledPoints;
    }

    @Override
    public void place() throws NoSolutionException {
        // Топологически сортируем вершины
        setOrder();
        // Определяем компоненты связанности
        setComp();
        // Проверяем, что решение существует
        if (!checkSolutionExistence()) throw new NoSolutionException();
        // Выбираем вариант расположения, при котором достигается решение
        filter();
    }

    private void filter() {
        List<ImplicationNode> nodes = labeledPoints.graph.nodes;
        for (int i = 0; i < nodes.size(); ++i) {
            // Есть в топологическом порядке компонента связанности вершины их позже вершины !x, тогда выбираем x,
            // иначе !x
            nodes.get(nodes.get(i).comp > nodes.get(i ^ 1).comp ? i : i ^ 1).isActual = true;
        }
    }

    private void setOrder() {
        List<ImplicationNode> nodes = labeledPoints.graph.nodes;
        for (int i = 0; i < nodes.size(); i++) {
            ImplicationNode node = nodes.get(i);
            if (!node.used) orderDfs(node, i);
        }
    }

    private void orderDfs(ImplicationNode node, int index) {
        node.used = true;
        for (int i : node.children) {
            var next = labeledPoints.graph.nodes.get(i);
            if (!next.used) orderDfs(next, i);
        }
        order.add(index);
    }

    private void setComp() {
        int compIndex = 0;
        List<ImplicationNode> orderedNodes = order.stream()
                .map(index -> labeledPoints.graph.nodes.get(index))
                .collect(toList());
        reverse(orderedNodes);
        for (ImplicationNode node : orderedNodes) {
            if (node.comp == -1) compDfs(node, compIndex++);
        }
    }

    private void compDfs(ImplicationNode node, int compIndex) {
        node.comp = compIndex;
        for (int i : node.parents) {
            var next = labeledPoints.graph.nodes.get(i);
            if (next.comp == -1) compDfs(next, compIndex);
        }
    }

    // Проверяем, что x и !x лежат в разных компонентах связанности, иначе решения не существует
    private boolean checkSolutionExistence() {
        List<ImplicationNode> nodes = labeledPoints.graph.nodes;
        for (int i = 0; i < nodes.size(); i++) {
            if (nodes.get(i).comp == nodes.get(i ^ 1).comp) return false;
        }
        return true;
    }

}
