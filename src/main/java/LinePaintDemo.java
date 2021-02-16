import javax.swing.*;
import java.awt.*;

public class LinePaintDemo extends JPanel {

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        g.setColor(Color.BLUE);
        g.drawLine(getWidth()/2, getHeight()/2, 22, getHeight()/2);
        g.drawLine(getWidth()/2, getHeight()/2+10, 22, getHeight()/2+10);
        g.drawLine(getWidth()/2, getHeight()/2+20, 22, getHeight()/2+20);
    }

    public static void main(String[] args) {
        EventQueue.invokeLater(new Runnable() {
            public void run() {
                JFrame frame = new JFrame();
                frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                frame.add(new LinePaintDemo());
                frame.setSize(300,300);
                frame.setVisible(true);
            }
        });
    }
}
