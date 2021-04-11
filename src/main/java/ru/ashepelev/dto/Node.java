package ru.ashepelev.dto;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.lang.Integer.MAX_VALUE;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Node {
    @JacksonXmlProperty
    public String id;

    public String parentId;
    public List<String> childIds = new ArrayList<>();

    public int x = MAX_VALUE / 4;
    public int y = MAX_VALUE / 4;

    public Map<Integer, Integer> xLeft = new HashMap<>();
    public Map<Integer, Integer> xRight = new HashMap<>();

    public int ordMark = MAX_VALUE;
    public boolean isDummy = false;

    public static Node dummy(String id, int x, int y) {
        Node node = new Node();
        node.isDummy = true;
        node.id = id;
        node.x = x;
        node.y = y;
        return node;
    }
}
