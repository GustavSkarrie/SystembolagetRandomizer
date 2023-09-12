import java.awt.Dimension;
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

    public void setImage(BufferedImage bufferedImage) {
        this.bufferedImage = bufferedImage;
        Image tempImg = bufferedImage.getScaledInstance(width, height, Image.SCALE_SMOOTH);
        this.image = new ImageIcon(tempImg);

        object.setIcon(image);
        object.setBounds(x, y, width, height);
    }

    @Override
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

    public void setCenter(int x, int y) {
        setPosition(x - width/2, y - height/2);
    }

    public Dimension getCenter() {
        int x = this.x + width/2;
        int y = this.y + height/2;
        return new Dimension(x, y);
    }

    @Override
    public int getWidth() {
        return width;
    }

    @Override
    public int getHeight() {
        return height;
    }

    @Override
    public void move(int x, int y) {
        this.x += x;
        this.y += y;
        object.setBounds(this.x, this.y, width, height);
    }
    
}
