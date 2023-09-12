import java.awt.Color;
import java.awt.Dimension;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

public class UIProduct {

    Product product;
    Picture picture;
    Picture background;
    BufferedImage image;

    int width;
    int height;

    UIProduct(Product product, String file, Window aWindow, int x, int y, int width, int height) {
        this.product = product;
        
        try {
            image = ImageIO.read(new File(file));
            background = new Picture(x, y, width, height, image);
        } catch (IOException e) {}     
        
        Dimension dim = background.getCenter();
        picture = new Picture(x, y, 0.2f, product.getImage());
        picture.setCenter((int)dim.getWidth(), (int)dim.getHeight());
        setSize(width, height);

        aWindow.add(picture);
        aWindow.add(background);

        this.width = width;
        this.height = height;
        //aWindow.Refresh();
    }

    public void setProduct(Product product, String file) {
        this.product = product;
        picture.setImage(product.getImage());

        try {
            image = ImageIO.read(new File(file));
            background.setImage(image);
        } catch (IOException e) {}  

        setSize(width, height);
    }

    public void setSize(int width, int height) {
        this.width = width;
        this.height = height;

        background.setSize(width, height);
        picture.setSize((int)((height - 30) * ((float)picture.getWidth() / (float)picture.getHeight())), height - 30);
        Dimension dim = background.getCenter();
        picture.setCenter((int)dim.getWidth(), (int)dim.getHeight());
    }

    public void move(int x, int y) {
        background.move(x, y);
        picture.move(x, y);
    }

    public Product getProduct() {
        return product;
    }
}
