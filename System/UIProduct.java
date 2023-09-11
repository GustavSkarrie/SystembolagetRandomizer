import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

public class UIProduct {

    Product product;
    Picture picture;
    Picture background;
    BufferedImage image;

    UIProduct(Product product, Color color, Window aWindow) {
        this.product = product;
        picture = new Picture(10, 10, 0.2f, product.getImage());
        aWindow.add(picture);
        try {
            image = ImageIO.read(new File("blue.png"));
            background = new Picture(10, 10, picture.getHeight(), picture.getHeight(), image);
            aWindow.add(background);
        } catch (IOException e) {}        
    }
}
