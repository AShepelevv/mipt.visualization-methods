package ru.ashepelev.label;

import java.io.IOException;

public class LabelMain {
    public static void main(String[] args) throws IOException {
        // Прочитаем данные и построим граф импликаций
        var labelPoints =  new LabelReader("label.txt").read();

        // Попробуем решить задачу LabelPlacement, в случае отсутствия решения кидает исключение
        // и пишем, что нет решения
        try {
            new LabelPlacer(labelPoints).place();
        } catch (NoSolutionException e) {
            System.out.println(e.getMessage());
            return;
        }

        // рисуем точки и подписи
        new LabelDrawer().draw(labelPoints);
    }
}
