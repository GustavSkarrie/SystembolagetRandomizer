import java.awt.image.BufferedImage;
import java.net.URL;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;

import javax.swing.ImageIcon;

import org.json.simple.JSONObject;

public class Product {
    String name;
    double price;
    String type;
    BufferedImage image;
    String url;

    public Product(String name, double price, String type, BufferedImage image, String url) {
        this.name = name;
        this.price = price;
        this.type = type;
        this.image = image;
        this.url = url;
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

    public JSONObject getJSON() {
        JSONObject object = new JSONObject();
        object.put("name", name);
        object.put("price", price);
        object.put("type", type);
        object.put("type", type);
        object.put("url", url);
        return object;
    }
}
