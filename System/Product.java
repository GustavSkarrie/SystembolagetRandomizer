import javax.swing.ImageIcon;

public class Product {
    String name;
    double price;
    String type;
    ImageIcon image;

    public Product(String name, double price, String type, ImageIcon image) {
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

    ImageIcon getImage() {
        return image;
    }
}
