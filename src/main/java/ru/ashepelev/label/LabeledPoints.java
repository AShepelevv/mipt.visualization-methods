package ru.ashepelev.label;

import lombok.NoArgsConstructor;
import ru.ashepelev.dto.Graph;
import ru.ashepelev.label.graph.ImplicationGraph;

import java.util.ArrayList;
import java.util.List;

@NoArgsConstructor
public class LabeledPoints {
    public ImplicationGraph graph = new ImplicationGraph();
    public List<Point> points = new ArrayList<>();
}
