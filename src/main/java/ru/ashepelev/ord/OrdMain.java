package ru.ashepelev.ord;

import ru.ashepelev.common.GraphDrawer;
import ru.ashepelev.common.GraphReader;
import ru.ashepelev.dto.Graph;

import java.io.IOException;

public class OrdMain {
    public static void main(String[] args) throws IOException {
        // Прочитаем граф из файла src/main/resources/tree.xml
        Graph graph = new GraphReader("ord.xml").read();

        // Разметим граф алгоритмом LayeredTree
        new OrdPlacer(graph).placeGraph();

        // Нарисуем граф в окне
        var frame = new GraphDrawer().draw(graph);
    }
}
