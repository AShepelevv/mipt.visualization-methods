package ru.ashepelev.label.graph;

import java.util.ArrayList;
import java.util.List;

public class ImplicationNode {
    public int minX;
    public int maxX;
    public int minY;
    public int maxY;
    public boolean used = false;
    public int comp = -1;
    public boolean isActual = false;
    public List<Integer> children = new ArrayList<>();
    public List<Integer> parents = new ArrayList<>();
    public ImplicationNode(int minX, int maxX, int minY, int maxY) {
        this.minX = minX;
        this.maxX = maxX;
        this.minY = minY;
        this.maxY = maxY;
    }
}
