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
    String id;
    ImageIcon image;
    BufferedImage buffImage;
    String url;

    public Product(String name, double price, String type, String id, ImageIcon image, BufferedImage buffImage, String url) {
        this.name = name;
        this.price = price;
        this.type = type;
        this.id = id;
        this.image = image;
        this.buffImage = buffImage;
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

    ImageIcon getImage() {
        return image;
    }

    public JSONObject getJSON() {
        JSONObject object = new JSONObject();
        object.put("name", name);
        object.put("price", price);
        object.put("type", type);
        object.put("id", id);
        object.put("url", url);
        return object;
    }

    public String getLink() {
        String type = this.type;
        type = type.replaceAll("&","-");
        type = type.replaceAll("\\s", "");

        String name = this.name;
        name = name.replaceAll("\\s", "-");

        String link = "https://www.systembolaget.se/produkt/" + type + "/" + name + "-" + id; 
        return link;
    }

    public int getId() {
        int i = Integer.parseInt(id);
        return i;
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof Product) {
            Product p = (Product) o;
            //System.err.println(this.getId() == p.getId());

            return this.getId() == p.getId();
        }

        return false;
    } 
}
