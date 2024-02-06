import java.awt.Color;
import java.awt.Dimension;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;

public class UIProduct {

    Product product;
    Picture picture;
    Picture background;
    ImageIcon image;

    int width;
    int height;

    float boundary = -200;

    UIProduct(Product product, ImageIcon icon, Window aWindow, int x, int y, int width, int height) {
        this.product = product;
        this.image = icon;
        this.width = width;
        this.height = height;
        background = new Picture(x, y, width, height, icon);
        
        Dimension dim = background.getCenter();
        picture = new Picture(x, y, width, height, product.getImage());
        picture.setCenter((int)dim.getWidth(), (int)dim.getHeight());

        aWindow.add(picture);
        aWindow.add(background);

        this.width = width;
        this.height = height;
    }

    UIProduct(Product product, ImageIcon icon) {
        this.product = product;
        this.image = icon;
    }

    public void init(Window window,int x, int y, int width, int height){
        this.width = width;
        this.height = height;
        background = new Picture(x, y, width, height, image);
        
        Dimension dim = background.getCenter();
        picture = new Picture(x, y, width, height, product.getImage());
        picture.setCenter((int)dim.getWidth(), (int)dim.getHeight());

        window.add(picture);
        window.add(background);

        this.width = width;
        this.height = height;
    }

    public boolean Update (float speed) {
        move(-speed, 0);
        if (background.getX() < boundary) {
            move((AlkoMain.size + 10) * 20, 0);
            return true;
        }

        return false;
    }

    public void setBoundary(float boundary) {
        this.boundary = boundary;
    }

    public void setProduct(Product product, ImageIcon icon) {
        this.product = product;
        //picture.setImage(product.getImage());
        this.image = icon;
        //background.setImage(icon);
    }

    public boolean isInside(int x) {
        if (x >= this.background.getX() - 5 && x <= this.background.getX() + this.width + 5)
            return true;

        return false;
    }

    public void setSize(int width, int height) {
        this.width = width;
        this.height = height;

        background.setSize(width, height);
        picture.setSize((int)((height - 30) * ((float)picture.getWidth() / (float)picture.getHeight())), height - 30);
        Dimension dim = background.getCenter();
        picture.setCenter((int)dim.getWidth(), (int)dim.getHeight());
    }

    public void move(float x, float y) {
        background.move(x, y);
        picture.move(x, y);
    }

    public void setPos(int x, int y) {
        background.setPosition(x, y);
        Dimension dim = background.getCenter();
        picture.setCenter((int)dim.getWidth(), (int)dim.getHeight());
    }

    public Product getProduct() {
        return product;
    }

    public void remove(Window aWindow) {
        aWindow.remove(background);
        aWindow.remove(picture);
    }
}
