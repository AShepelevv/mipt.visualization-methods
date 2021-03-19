package ru.ashepelev.dto;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;

import java.util.List;
import java.util.Map;

public class Graph {
    @JacksonXmlElementWrapper(useWrapping = false)
    public List<Node> node;
    @JacksonXmlElementWrapper(useWrapping = false)
    public List<Edge> edge;
    @JacksonXmlProperty
    public String id;
    @JacksonXmlProperty
    public String edgedefault;

    public Map<String, Node> nodes;
}
