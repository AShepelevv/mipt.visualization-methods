package ru.ashepelev.common;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowEvent;
import java.io.File;

import static java.awt.event.WindowEvent.WINDOW_CLOSING;

public class ImageSaver {
    private final JFrame frame;

    public ImageSaver(JFrame frame) {
        this.frame = frame;
    }

    public void save(String filename) {
        try {
            Thread.sleep(1000);
            Rectangle rectangle = new Rectangle(frame.getBounds());
            File file = new File(filename);
            ImageIO.write(
                    new Robot().createScreenCapture(rectangle),
                    "png",
                    file);
            System.out.println("File Path: " + file.getAbsolutePath());
        } catch (Exception exception) {
            System.out.println("Something went wrong :(");
            exception.printStackTrace();
        }
//        frame.dispatchEvent(new WindowEvent(frame, WINDOW_CLOSING));
    }
}
