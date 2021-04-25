package ru.ashepelev.label;

import org.springframework.core.io.ClassPathResource;
import ru.ashepelev.label.graph.ImplicationNode;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

import static java.lang.Integer.parseInt;
import static java.util.stream.Collectors.toList;

public class LabelReader {
    private final String filePath;

    public LabelReader(String path) {
        this.filePath = path;
    }

    public LabeledPoints read() throws IOException {
        // Читаем файл
        var path = new ClassPathResource(filePath).getURL().getPath();
        var reader = new BufferedReader(new FileReader(path));
        var lines = reader.lines().collect(toList());
        var labeledPoints = new LabeledPoints();

        // Парсим построчно данные о точках и подписях
        for (String line : lines) {
            var blocks = line.split("\t");
            var shifts = blocks[2].split(" ");
            var point = new Point(
                    parseInt(blocks[0].split(",")[0]),
                    parseInt(blocks[0].split(",")[1]),
                    parseInt(blocks[1].split(",")[0]),
                    parseInt(blocks[1].split(",")[1]),
                    new int[]{parseInt(shifts[0].split(",")[0]), parseInt(shifts[1].split(",")[0])},
                    new int[]{parseInt(shifts[0].split(",")[1]), parseInt(shifts[1].split(",")[1])}
            );
            labeledPoints.points.add(point);
        }

        // Точки с возможными вариантами расположения подписи мапим в вершины графа импликаций x -> {node(x), node(!x)}
        for (Point point : labeledPoints.points) {
            labeledPoints.graph.nodes.add(new ImplicationNode(
                    point.x - point.shiftX[0],
                    point.x - point.shiftX[0] + point.width,
                    point.y - point.shiftY[0],
                    point.y - point.shiftY[0] + point.height
            ));
            labeledPoints.graph.nodes.add(new ImplicationNode(
                    point.x - point.shiftX[1],
                    point.x - point.shiftX[1] + point.width,
                    point.y - point.shiftY[1],
                    point.y - point.shiftY[1] + point.height
            ));
        }

        // Перебираем все пары вершин и проводим ребра импликаций и обратные ребра в технических нуждах
        for (int i = 0; i < labeledPoints.points.size(); ++i) {
            for (int j = 2 * (i + 1); j < labeledPoints.graph.nodes.size(); ++j) {
                ImplicationNode nodeFalse = labeledPoints.graph.nodes.get(2 * i);
                ImplicationNode nodeTrue = labeledPoints.graph.nodes.get(2 * i + 1);
                ImplicationNode node = labeledPoints.graph.nodes.get(j);
                // Если пересекаются
                if (!(nodeFalse.maxX < node.minX || nodeFalse.minX > node.maxX ||
                        nodeFalse.maxY < node.minY || nodeFalse.minY > node.maxY)) {
                    // Прямое ребро
                    nodeFalse.children.add(j ^ 1);
                    // Обратное
                    labeledPoints.graph.nodes.get(j ^ 1).parents.add(2 * i);
                    // Сестринское прямое
                    node.children.add(2 * i + 1);
                    // Сестринское обратное
                    nodeTrue.parents.add(j);
                }

                if (!(nodeTrue.maxX < node.minX || nodeTrue.minX > node.maxX ||
                        nodeTrue.maxY < node.minY || nodeTrue.minY > node.maxY)) {
                    // Прямое ребро
                    nodeTrue.children.add(j ^ 1);
                    // Обратное
                    labeledPoints.graph.nodes.get(j ^ 1).parents.add(2 * i + 1);
                    // Сестринское прямое
                    node.children.add(2 * i);
                    // Сестринское обратное
                    nodeFalse.parents.add(j);
                }
            }
        }

        return labeledPoints;
    }
}
