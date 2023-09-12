import java.io.FileReader;
import java.io.FileWriter;
import java.awt.image.BufferedImage;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.time.LocalDate;
import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.text.html.HTMLDocument.BlockElement;
import java.net.URL;
import java.text.Normalizer;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;


public class AlkoMain {
    List<Product> vin = new ArrayList<>();
    List<Product> ol = new ArrayList<>();
    List<Product> sprit = new ArrayList<>();
    List<Product> cider = new ArrayList<>();
    float deltaTime = 0;

    public static void main(String[] args) {
        AlkoMain running = new AlkoMain();
        running.run();
    }

    public void run() {
        System.out.println("running");
        String s = "Tjena";
        s = s.substring(0, 4) + "Ja";
        System.out.println(s);
        getData("data.json");

        System.out.println("Vin: " + vin.size());
        System.out.println("Ol: " + ol.size());
        System.out.println("Sprit: " + sprit.size());
        System.out.println("Cider: " + cider.size());

        Window window = new Window(1200, 600, "Alkohol e gott");
        UIProduct temp = new UIProduct(ol.get(0), "image/blue.png", window, 10, 50, 150, 150);
        setRandom(temp);

        saveToJSON();

        //temp.setSize(150, 150);

        boolean running = true;

        while(running) {
            //temp.move(1, 0);
            window.Refresh();
        }
    }

    public void getData(String fileName) {
        
        List<Product> products = new ArrayList<>();
        JSONParser parser = new JSONParser();

        try {
            JSONArray tempArray = (JSONArray) parser.parse(new FileReader(fileName));

            for (Object o: tempArray) {
                JSONObject object = (JSONObject) o;
                
                if (!getBool(object, "isCompletelyOutOfStock") ||
                !getBool(object, "isTemporaryOutOfStock") ||
                LocalDate.now().isBefore(getDate(object,"productLaunchDate")) ||
                !getString(object, "categoryLevel1").equals("Alkoholfritt")) {
                    if (hasImage(object)) {
                        Product product = createProduct(object);

                        switch(product.getType()){
                            case "Vin":
                                vin.add(product);
                                break;

                            case "Al":
                                ol.add(product);
                                break;

                            case "Cider & blanddrycker":
                                cider.add(product);
                                break;

                            case "Sprit":
                                sprit.add(product);
                                break;

                            default:
                                System.out.println(product.getType());
                                break;
                        }
                    } 
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void saveToJSON() {
        try {
            JSONArray array = new JSONArray();

            FileWriter file = new FileWriter("output.json");

            for (Product product : cider)
                array.add(product.getJSON());

            for (Product product : ol)
                array.add(product.getJSON());

            for (Product product : sprit)
                array.add(product.getJSON());

            for (Product product : vin)
                array.add(product.getJSON());

            file.write(array.toJSONString());
            file.close();
        } catch (Exception e) {
            // TODO: handle exception
        }

    }

    public void setRandom(UIProduct uiProduct) {
        Random rand = new Random();
        float temp = rand.nextFloat();

        if (temp < 0.50) { //öl 50 procent chans
            Product pro = ol.get(rand.nextInt(ol.size()));

            if (pro.name.contains("Norrlands"))
                uiProduct.setProduct(pro, "image/rainbow.png");
            else
                uiProduct.setProduct(pro, "image/blue.png");
        }
        else if (temp < 0.85) { //cider 35 procent chans
            Product pro = cider.get(rand.nextInt(cider.size()));
            uiProduct.setProduct(pro, "image/pink.png");
        }
        else if (temp < 0.95) { //vin 10 procent risk
            Product pro = vin.get(rand.nextInt(vin.size()));
            uiProduct.setProduct(pro, "image/red.png");
        }
        else { //sprit 5 procent risk
            Product pro = sprit.get(rand.nextInt(sprit.size()));
            uiProduct.setProduct(pro, "image/yellow.png");
        }
    }

    public Product createProduct(JSONObject object) throws IOException {
        var name = getString(object, "productNameBold") + " - " + getString(object, "productNameThin");
        var price = getDouble(object, "price");
        var type = getString(object, "categoryLevel1");
        var image = getImage(object, "images");
        var ulr = getULR(object, "images");
        return new Product(name, price, type, image, ulr);
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

        try {
            String temp = Normalizer.normalize((String) object.get(key), Normalizer.Form.NFD);
            temp = temp.replaceAll("[^\\p{ASCII}]", "");
            return temp;
        }
        catch (Exception e) {
            return (String) object.get(key);
        }
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

    String getULR(JSONObject object, String key) {
        JSONArray a = (JSONArray) object.get(key);
        JSONObject o = (JSONObject) a.get(0);
        try {
            return (String) o.get("imageUrl") + "_400.png";
        } catch (Exception e) {
            return null;
        }
    }

    boolean hasImage(JSONObject object) {
        JSONArray a = (JSONArray) object.get("images");

        if (a.size() > 0)
            return true;
        
        return false;
    }
}
