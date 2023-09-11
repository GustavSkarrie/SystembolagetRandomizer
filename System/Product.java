import java.awt.image.BufferedImage;

import javax.swing.ImageIcon;

public class Product {
    String name;
    double price;
    String type;
    BufferedImage image;

    public Product(String name, double price, String type, BufferedImage image) {
        this.name = name;
        this.price = price;
        this.type = type;
        this.image = image;
    }

    String getName() {
        return name;
    }

    double getPrice() {
        return price;
    }

    String getType() {
        return type;
    }

    BufferedImage getImage() {
        return image;
    }
}
