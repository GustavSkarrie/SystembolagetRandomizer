import java.io.FileReader;
import java.awt.image.BufferedImage;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.time.LocalDate;
import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.text.html.HTMLDocument.BlockElement;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;


public class AlkoMain {
    public static void main(String[] args) {
        AlkoMain running = new AlkoMain();
        running.run();
    }

    public void run() {
        System.out.println("running");
        String s = "Tjena";
        s = s.substring(0, 4) + "Ja";
        System.out.println(s);
        List<Product> products = getData("data.json");
        System.out.println(products.size());
        Window window = new Window(420, 420, "Alkohol e gott");
        UIProduct temp = new UIProduct(products.get(4), null, window);

        //2011-10-01T00:00:00
    }

    public List<Product> getData(String fileName) {
        
        List<Product> products = new ArrayList<>();
        JSONParser parser = new JSONParser();

        try {
            JSONArray tempArray = (JSONArray) parser.parse(new FileReader(fileName));

            for (Object o: tempArray) {
                JSONObject object = (JSONObject) o;
                
                if (!getBool(object, "isCompletelyOutOfStock") ||
                !getBool(object, "isTemporaryOutOfStock") ||
                LocalDate.now().isBefore(getDate(object,"productLaunchDate")) ||
                !getString(object, "categoryLevel1").equals("Alkoholfritt"))
                    if (hasImage(object))
                        products.add(createProduct(object));
            }


        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ParseException e) {
            e.printStackTrace();
        }

        return products;
    }

    public Product createProduct(JSONObject object) throws IOException {
        var name = getString(object, "productNameBold") + " - " + getString(object, "Imperial Cuvée Organic");
        var price = getDouble(object, "price");
        var type = getString(object, "categoryLevel1");
        var image = getImage(object, "images");
        var date = getDate(object,"productLaunchDate");
        return new Product(name, price, type, image, date);
    }

    LocalDate getDate(JSONObject object,String key) {
        // Sätt ihop stringen här.
        String str = getString(object, key).substring(0,10);
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-d");
        
        LocalDate d1 = LocalDate.parse(str, dtf);
        return d1;
    }

    double getDouble(JSONObject object, String key) {
        try {
            return ((Long) object.get(key)).doubleValue();
        } 
        catch (Exception e) {
            return ((double) object.get(key));
        }
    }

    Boolean getBool(JSONObject object, String key) {
        return (Boolean) object.get(key);
    }

    String getString(JSONObject object, String key) {
        return (String) object.get(key);
    }

    BufferedImage getImage(JSONObject object, String key) throws IOException {
        JSONArray a = (JSONArray) object.get(key);
        JSONObject o = (JSONObject) a.get(0);
        URL url = new URL((String) o.get("imageUrl") + "_400.png");
        //URL url = new URL("https://cdn5.vectorstock.com/i/1000x1000/78/59/happy-grin-emoji-instant-messaging-icon-imag-vector-17067859.jpg");
        System.out.println(url);
        BufferedImage c = ImageIO.read(url);
        return c;
    }

    boolean hasImage(JSONObject object) {
        JSONArray a = (JSONArray) object.get("images");

        if (a.size() > 0)
            return true;
        
        return false;
    }
}
