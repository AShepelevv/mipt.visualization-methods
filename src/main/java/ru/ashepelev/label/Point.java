package ru.ashepelev.label;

public class Point {
    public int x;
    public int y;
    public int width;
    public int height;
    public int[] shiftX = new int[2];
    public int[] shiftY = new int[2];


    public Point(int x, int y, int width, int height, int[] shiftX, int[] shiftY) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.shiftX = shiftX;
        this.shiftY = shiftY;
    }
}
