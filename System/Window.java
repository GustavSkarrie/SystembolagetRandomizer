import java.util.ArrayList;
import java.util.Vector;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.json.simple.parser.ParseException;

import java.net.URL;
import java.awt.Dimension;
import java.awt.Image;
import java.util.List;
import java.awt.image.BufferedImage;
import java.io.FileNotFoundException;
import java.io.IOException;

public class Window {
    
    JFrame myWindow;

    List<UIObject> objects;

    Window(int width, int height, String windowName) {
        myWindow = new JFrame(windowName);

        myWindow.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        myWindow.add(new JPanel());
        myWindow.setSize(width, height);
        myWindow.setLayout(null);
        myWindow.setVisible(true);
        myWindow.setPreferredSize(new Dimension(width, height)); 

        objects = new ArrayList<>();
        //myWindow.pack();

        /*
        try {
            URL url = new URL("https://product-cdn.systembolaget.se/productimages/507861/507861_400.png");
            BufferedImage bufferedImage = ImageIO.read(url);
            Picture tempPic = new Picture(10, 10, 0.2f, bufferedImage);
            myWindow.add(tempPic.getObject());
            Refresh();

        } catch (Exception e) {
            e.printStackTrace();
        }
        */
    }

    void Update() {
        for (UIObject uiObject : objects) {
            uiObject.Update();
        }
    }

    void add(UIObject object) {
        objects.add(object);
        myWindow.add(object.getObject());
        JPanel panel = new JPanel();
        //Refresh();
    }

    void Refresh() {
        myWindow.invalidate();
        myWindow.validate();
        myWindow.repaint();
        /* 
        myWindow.setVisible(false);
        myWindow.setVisible(true);
        */
    }
}
