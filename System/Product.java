import java.awt.image.BufferedImage;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;

import javax.swing.ImageIcon;

public class Product {
    String name;
    double price;
    String type;
    BufferedImage image;
    LocalDate date;

    public Product(String name, double price, String type, BufferedImage image,LocalDate date) {
        this.name = name;
        this.price = price;
        this.type = type;
        this.image = image;
        this.date = date;
        LocalDate.now();
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

    LocalDate getDate() {
        this.date.now();
        return date;
    }
}
