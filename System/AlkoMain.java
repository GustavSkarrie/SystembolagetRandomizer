import java.io.FileReader;
import java.io.FileWriter;
import java.awt.image.BufferedImage;
import java.io.File;
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
import java.awt.Image;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;


public class AlkoMain {
    List<Product> vin = new ArrayList<>();
    List<Product> ol = new ArrayList<>();
    List<Product> sprit = new ArrayList<>();
    List<Product> cider = new ArrayList<>();
    float lastTime = 0;
    float deltaTime = 0;
    float curSpeed = 0;
    float timer = 0;

    ImageIcon blue;
    ImageIcon pink;
    ImageIcon red;
    ImageIcon yellow;
    ImageIcon rainbow;
    ImageIcon line;

    static int size = 190;

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

        blue = loadImage("image/blue.png", size, size);
        pink = loadImage("image/pink.png", size, size);
        red = loadImage("image/red.png", size, size);
        yellow = loadImage("image/yellow.png", size, size);
        rainbow = loadImage("image/rainbow.png", size, size);
        line = loadImage("image/line.png", size + 20, size + 20);

        Window window = new Window(1200, 600, "Alkohol e gott");
        //UIProduct temp = new UIProduct(ol.get(0), "image/blue.png", window, 10, 50, 150, 150);
        //setRandom(temp);

        saveToJSON();

        //temp.setSize(150, 150);

        Picture middle = new Picture(600, 300, size + 20, size + 20, line);
        middle.setCenter(1200/2, 600/2);
        window.add(middle);

        boolean running = true;
        List<UIProduct> products = roll(window);

        lastTime = System.nanoTime();

        while(running) {
            float time = System.nanoTime();
            deltaTime = (time - lastTime) / 1000000;

            if (timer > 0)
                System.out.println("time: " + timer);
            //System.out.println("speed: " + curSpeed);

            timer -= deltaTime / 1000;

            if (timer < 0 && curSpeed != 0)
                curSpeed = clamp((curSpeed - 0.0002f) / 1.0003f , 0, 100);

            for (UIProduct uiProduct : products) {
                if (uiProduct.Update(curSpeed * deltaTime))
                    setRandom(uiProduct);
            }

            //temp.move(1, 0);
            window.Refresh();
            lastTime = time;
        }
    }

    public float getDeltaTime() {
        return deltaTime;
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

    public float clamp(float value, float min, float max) {
        if (value > max)
            return max;
        if (value < min)
            return min;

        return value;
    }

    public List<UIProduct> roll(Window aWindow) {
        Random rand = new Random();
        List<UIProduct> products = new ArrayList<>();

        for (int i = 0; i < 20; i++) {
            UIProduct product = new UIProduct(ol.get(0), blue, aWindow, 2000 + (size + 10) * i, 600/2 - size/2, size, size);
            setRandom(product);
            products.add(product);
        }

        curSpeed = 2.5f;
        timer = rand.nextFloat(6) + 5;
        return products;
    }

    public void setRandom(UIProduct uiProduct) {
        Random rand = new Random();
        float temp = rand.nextFloat();

        if (temp < 0.50) { //öl 50 procent chans
            Product pro = ol.get(rand.nextInt(ol.size()));

            if (pro.name.contains("Norrlands"))
                uiProduct.setProduct(pro, rainbow);
            else
                uiProduct.setProduct(pro, blue);
        }
        else if (temp < 0.85) { //cider 35 procent chans
            Product pro = cider.get(rand.nextInt(cider.size()));
            uiProduct.setProduct(pro, pink);
        }
        else if (temp < 0.95) { //vin 10 procent risk
            Product pro = vin.get(rand.nextInt(vin.size()));
            uiProduct.setProduct(pro, red);
        }
        else { //sprit 5 procent risk
            Product pro = sprit.get(rand.nextInt(sprit.size()));
            uiProduct.setProduct(pro, yellow);
        }
    }

    public Product createProduct(JSONObject object) throws IOException {
        var name = getString(object, "productNameBold") + " - " + getString(object, "productNameThin");
        var price = getDouble(object, "price");
        var type = getString(object, "categoryLevel1");
        var buffImage = getBuffImage(object, "images");
        var image = getImage(buffImage);
        var ulr = getULR(object, "images");
        return new Product(name, price, type, image, buffImage, ulr);
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

    BufferedImage getBuffImage(JSONObject object, String key) throws IOException {
        JSONArray a = (JSONArray) object.get(key);
        JSONObject o = (JSONObject) a.get(0);
        URL url = new URL((String) o.get("imageUrl") + "_400.png");
        //URL url = new URL("https://cdn5.vectorstock.com/i/1000x1000/78/59/happy-grin-emoji-instant-messaging-icon-imag-vector-17067859.jpg");
        System.out.println(url);
        BufferedImage c = ImageIO.read(url);
        return c;
    }

    ImageIcon getImage(BufferedImage buffImage) {
        ImageIcon image = new ImageIcon(buffImage.getScaledInstance((size - 30) * buffImage.getWidth() / buffImage.getHeight(), size - 30, Image.SCALE_FAST));
        return image;
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

    public ImageIcon loadImage(String file, int width, int height) {

        try {
            BufferedImage b = ImageIO.read(new File(file));
            return new ImageIcon(b.getScaledInstance(width, height, Image.SCALE_FAST)); 
        } catch (Exception e) {
            return null;
        }

    }
}
