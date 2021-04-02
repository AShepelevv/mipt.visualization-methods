package ru.ashepelev;

import ru.ashepelev.dto.Graph;

import java.io.IOException;

public class Main {
    public static void main(String[] args) throws IOException {

        // Прочитаем граф из файла src/main/resources/graph.xml
        Graph graph = new GraphReader("graph.xml").read();

        // Разметим граф алгоритмом LayeredTree
        new GraphWorker(graph).placeGraph();

        // Нарисуем граф в окне
        var frame = new GraphDrawer().draw(graph);

        // Передадим окно, чтобы сохранить его screenshot в plotting_result.png
        new ImageSaver(frame).save("plotting_result.png");
    }
}
