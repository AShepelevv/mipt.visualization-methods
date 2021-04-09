package ru.ashepelev.common;

import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import lombok.RequiredArgsConstructor;
import org.apache.commons.io.FileUtils;
import org.springframework.core.io.ClassPathResource;
import ru.ashepelev.dto.Graph;
import ru.ashepelev.dto.GraphML;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

import static java.util.function.Function.identity;
import static java.util.stream.Collectors.toMap;

public class GraphReader {
    private final String filePath;

    public GraphReader(String path) {
        this.filePath = path;
    }

    public Graph read() throws IOException {
        File source = new ClassPathResource(filePath).getFile();

        XmlMapper xmlMapper = new XmlMapper();

        String xml = FileUtils.readFileToString(source, StandardCharsets.UTF_8);
        Graph graph = xmlMapper.readValue(xml, GraphML.class).getGraph();
        graph.nodes = graph.node.stream()
                .collect(toMap(n -> n.id, identity()));
        graph.edge.forEach(edge -> {
            graph.nodes.get(edge.source).childIds.add(edge.target);
            graph.nodes.get(edge.target).parentId = edge.source;
        });
        return graph;
    }
}
