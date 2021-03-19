package ru.ashepelev;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.util.Map.of;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Node {
    @JacksonXmlProperty
    public String id;

    public String parentId;
    public List<String> childIds = new ArrayList<>();

    public int x;
    public int y;

    public Map<Integer, Integer> x_left = new HashMap<>();
    public Map<Integer, Integer> x_right = new HashMap<>();
}
