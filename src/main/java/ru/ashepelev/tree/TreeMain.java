package ru.ashepelev.tree;

import ru.ashepelev.common.GraphDrawer;
import ru.ashepelev.common.GraphReader;
import ru.ashepelev.dto.Graph;

import java.io.IOException;

public class TreeMain {
    public static void main(String[] args) throws IOException {

        // Прочитаем граф из файла src/main/resources/tree.xml
        Graph graph = new GraphReader("tree.xml").read();

        // Разметим граф алгоритмом LayeredTree
        new TreePlacer(graph).placeGraph();

        // Нарисуем граф в окне
        var frame = new GraphDrawer().draw(graph);
    }
}
