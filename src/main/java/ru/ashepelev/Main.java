package ru.ashepelev;

import ru.ashepelev.dto.Graph;

import java.io.IOException;

public class Main {
    public static void main(String[] args) throws IOException {
        Graph graph = new GraphReader("graph.xml").read();
        new GraphWorker(graph).placeGraph();
        var frame = new GraphDrawer().draw(graph);
        new ImageSaver(frame).save("plotting_result.png");
    }
}
