package ru.ashepelev;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowEvent;
import java.io.File;

import static java.awt.Color.BLACK;
import static java.awt.event.WindowEvent.WINDOW_CLOSING;
import static javax.swing.WindowConstants.EXIT_ON_CLOSE;

public class GraphicsExporter extends JPanel {
    private final JFrame frame = new JFrame("Plotting result");

    public GraphicsExporter() {
        Canvas canvas = new Canvas();
        canvas.setSize(400, 400);
        this.setSize(400, 400);
        frame.add(this);
        frame.add(canvas);
        frame.setDefaultCloseOperation(EXIT_ON_CLOSE);
        frame.pack();
        frame.setVisible(true);
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        g.setColor(BLACK);
        g.drawLine(100, 100, 300, 200);
    }

    public static void save(JFrame frame) {
        try {
            Rectangle rectangle = new Rectangle(frame.getBounds());
            File file = new File("plotting_result.png");
            ImageIO.write(
                    new Robot().createScreenCapture(rectangle),
                    "png",
                    file);
            System.out.println("File Path: " + file.getAbsolutePath());
        } catch (Exception exception) {
            System.out.println("Something went wrong :(");
            exception.printStackTrace();
        }
    }

    public static void main(String[] args) {
        var frame = new GraphicsExporter().frame;
        save(frame);
        frame.dispatchEvent(new WindowEvent(frame, WINDOW_CLOSING));
    }
}
