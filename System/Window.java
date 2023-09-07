import java.util.Vector;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

public class Window {
    
    JFrame myWindow;

    Window(int width, int height, String windowName) {
        myWindow = new JFrame(windowName);

        myWindow.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        myWindow.add(new JPanel());
        myWindow.setSize(width, height);
        myWindow.setLayout(null);
        myWindow.setVisible(true);
        //myWindow.pack();
    }
}
