import java.awt.Image;
import java.awt.image.BufferedImage;

import javax.swing.ImageIcon;
import javax.swing.JLabel;

public class Picture implements UIObject {

    int x;
    int y;
    int width;
    int height;
    ImageIcon image;
    JLabel object;
    BufferedImage bufferedImage;

    Picture(int x, int y, int width, int height, BufferedImage image) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.bufferedImage = image;

        Image tempImg = bufferedImage.getScaledInstance(width, height, Image.SCALE_SMOOTH);
        this.image = new ImageIcon(tempImg);

        object = new JLabel(this.image);
        object.setBounds(x, y, width, height);
        //object.setSize(this.width, this.height);
    }

    Picture(int x, int y, float size, BufferedImage image) {
        this.x = x;
        this.y = y;
        this.width = (int)(image.getWidth() * size);
        this.height = (int)(image.getHeight() * size);
        this.bufferedImage = image;

        Image tempImg = bufferedImage.getScaledInstance(width, height, Image.SCALE_SMOOTH);
        this.image = new ImageIcon(tempImg);

        object = new JLabel(this.image);
        object.setBounds(x, y, width, height);
        //object.setSize(this.width, this.height);
    }

    public JLabel getObject() {
        return object;
    }

    @Override
    public void Update() {

    }

    @Override
    public void setSize(int width, int height) {
        Image tempImg = bufferedImage.getScaledInstance(width, height, Image.SCALE_SMOOTH);
        image = new ImageIcon(tempImg);
        object.setIcon(image);
    }

    @Override
    public void setSize(float size) {
        Image tempImg = bufferedImage.getScaledInstance((int)(width * size), (int)(height * size), Image.SCALE_SMOOTH);
        image = new ImageIcon(tempImg);
        object.setIcon(image);
    }

    @Override
    public void setPosition(int x, int y) {
        this.x = x;
        this.y = y;
        object.setBounds(this.x, this.y, width, height);
    }
    
}
