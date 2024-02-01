import java.io.FileReader;
import java.io.FileWriter;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.time.LocalDate;
import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import java.net.URL;
import java.text.Normalizer;
import java.time.format.DateTimeFormatter;
import java.awt.Desktop;
import java.awt.Image;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;


public class AlkoMain {
    HashMap<Product, JSONObject> vin = new HashMap<>();
    HashMap<Product, JSONObject> ol = new HashMap<>();
    HashMap<Product, JSONObject> sprit = new HashMap<>();
    HashMap<Product, JSONObject> cider = new HashMap<>();
    float lastTime = 0;
    float deltaTime = 0;
    float curSpeed = 0;
    float timer = 0;

    List<UIProduct> products = new ArrayList<>();
    boolean rolling = false;

    JButton rollButton;
    JButton refreshButton;
    JButton removeButton;

    ImageIcon blue;
    ImageIcon pink;
    ImageIcon red;
    ImageIcon yellow;
    ImageIcon rainbow;
    ImageIcon line;

    Product defultProduct;

    static int size = 190;

    boolean isFirst = true;

    public static void main(String[] args) {
        System.setProperty("file.encoding", "UTF-8");
        AlkoMain running = new AlkoMain();
        running.run();
    }

    public void run() {
        System.out.println("running");
        load();

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

        

        createButtons(window);

        boolean running = true;

        lastTime = System.nanoTime();

        System.out.println("Loading init list");
        while(running) {
            float time = System.nanoTime();
            deltaTime = (time - lastTime) / 1000000;

            if (rolling) {
                timer -= deltaTime / 1000;

                if (timer < 0 && curSpeed != 0)
                    curSpeed = clamp((curSpeed - 0.0003f) / 1.0005f , 0, 100);

                if (curSpeed == 0) {
                    Product pro = getMiddle(products);
                    System.out.println(pro.getLink());
                    openLink(pro.getLink());
                    createRemoveButton(window);
                    createButtons(window);
                    rolling = false;
                }
                    
                for (UIProduct uiProduct : products) {
                    if (uiProduct.Update(curSpeed * deltaTime))
                        uiProduct = getRandomUIProduct();
                }
            }

            window.Refresh();
            lastTime = time;
        }
    }

    private UIProduct getRandomUIProduct(){
        Random rand = new Random();
        return products.get(rand.nextInt(products.size()));
    }

    public float getDeltaTime() {
        return deltaTime;
    }

    public void createRemoveButton(Window aWindow) {
        removeButton = new JButton("Remove product");
        removeButton.setBounds(160 + 10, 10, 160, 50);
        removeButton.addActionListener((actionListener) -> {removeMiddle(aWindow);});

        aWindow.add(removeButton);
    }

    public void createButtons(Window aWindow) {
        rollButton = new JButton("Roll");
        rollButton.setBounds(1200/2 - 100, 600/2 + 120, 200, 70);
        rollButton.addActionListener((actionListener) -> {roll(aWindow);});

        refreshButton = new JButton("Refresh (> 1h)");
        refreshButton.setBounds(10, 10, 160, 50);
        refreshButton.addActionListener((actionListener) -> {refreshData(aWindow);});

        aWindow.add(rollButton);
        aWindow.add(refreshButton);
    }

    public void refreshData(Window aWindow) {
        aWindow.removeComp(rollButton);
        aWindow.removeComp(refreshButton);

        if (removeButton != null)
            aWindow.removeComp(removeButton);

        removeProducts(aWindow);

        try {
            vin = new HashMap<>();
            ol = new HashMap<>();
            cider = new HashMap<>();
            sprit = new HashMap<>();
            
            final Process p = new ProcessBuilder("python", "system.py").start();
            try(InputStreamReader isr = new InputStreamReader(p.getInputStream())) {
                int c;
                while((c = isr.read()) >= 0) {
                    System.out.print((char) c);
                    System.out.flush();
                }
            }

            System.out.println("Running python: " + p.isAlive());
            p.waitFor();
            getData("data.json");
            saveToJSON();
            createButtons(aWindow);

        } catch (Exception e) {
            System.out.println("wrong");
            // TODO: handle exception
        }

    }

    public void getData(String fileName) {
        JSONParser parser = new JSONParser();

        try {
            JSONArray tempArray = (JSONArray) parser.parse(new FileReader(fileName));
            int i = 0;

            for (Object o: tempArray) {
                System.out.println(i + "/" + tempArray.size());
                i++;

                JSONObject object = (JSONObject) o;
                
                if (!getBool(object, "isCompletelyOutOfStock") ||
                !getBool(object, "isTemporaryOutOfStock") ||
                LocalDate.now().isBefore(getDate(object,"productLaunchDate")) ||
                !getString(object, "categoryLevel1").equals("Alkoholfritt")) {
                    if (hasImage(object)) {
                        Product product = createProduct(object);

                        switch(product.getType()){
                            case "Vin":
                                if (getDouble(object, "volume") > 500 || product.getPrice() > 150)
                                    break;

                                vin.put(product, object);
                                break;

                            case "Ol":
                                ol.put(product, object);
                                break;

                            case "Cider & blanddrycker":
                                cider.put(product, object);
                                break;

                            case "Sprit":
                                if (product.getPrice() > 380)
                                    break;

                                sprit.put(product, object);
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

    public void load() {
        JSONParser parser = new JSONParser();

        try {
            JSONArray tempArray = (JSONArray) parser.parse(new FileReader("output.json"));
            int i = 0;

            for (Object object : tempArray) {
                JSONObject jsonObject = (JSONObject) object;

                System.out.println(i + "/" + tempArray.size());
                i++;

                Product product = loadProduct(jsonObject);

                if(i == 1)
                    defultProduct = product;
                switch(product.getType()){
                    case "Vin":
                        vin.put(product, jsonObject);
                        break;

                    case "Ol":
                        ol.put(product, jsonObject);
                        break;

                    case "Cider & blanddrycker":
                        cider.put(product, jsonObject);
                        break;

                    case "Sprit":
                        sprit.put(product, jsonObject);
                        break;
                }
            }

        } catch (Exception e) {
            // TODO: handle exception
        }
    }

    public void removeMiddle(Window aWindow) {
        Product product = getMiddle(products);

        aWindow.removeComp(rollButton);
        aWindow.removeComp(refreshButton);
        aWindow.removeComp(removeButton);
        removeProducts(aWindow);

        removeAll(product);

        saveToJSON();
        createButtons(aWindow);
    }

    public void removeAll(Product aProduct) {
        removeFromMap(vin, aProduct);
        removeFromMap(ol, aProduct);
        removeFromMap(cider, aProduct);
        removeFromMap(sprit, aProduct);
        /*vin.removeIf((element) -> (element.getId() == aProduct.getId()));
        ol.removeIf((element) -> (element.getId() == aProduct.getId()));
        cider.removeIf((element) -> (element.getId() == aProduct.getId()));
        sprit.removeIf((element) -> (element.getId() == aProduct.getId()));*/
    }

    private void removeFromMap(HashMap<Product, JSONObject> map, Product aProduct){
        for(Map.Entry<Product, JSONObject> entry : map.entrySet()){
            if(entry.getKey().getId() == aProduct.getId())
                vin.remove(aProduct);
        }
    }

    public Product loadProduct(JSONObject object) throws IOException {
        var name = getString(object, "name");
        var price = getDouble(object, "price");
        var type = getString(object, "type");
        var id = getString(object, "id");
        var url = getString(object, "url");
        return new Product(name, price, type, id, url);
    }

    public Product getMiddle(List<UIProduct> products) {
        for (UIProduct product : products) {
            if (product.isInside(1200/2))
                return product.getProduct();
        }

        return null;
    }

    void openLink(String link) {
        Desktop desktop = Desktop.isDesktopSupported() ? Desktop.getDesktop() : null;

        if (desktop != null && desktop.isSupported(Desktop.Action.BROWSE)) {
            try {
                URL url = new URL(link);
                desktop.browse(url.toURI());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void saveToJSON() {
        try {
            JSONArray array = new JSONArray();

            FileWriter file = new FileWriter("output.json");
            for (Map.Entry<Product, JSONObject> entry : cider.entrySet())
                array.add(entry.getKey().getJSON());

            for (Map.Entry<Product, JSONObject> entry : ol.entrySet())
                array.add(entry.getKey().getJSON());

            for (Map.Entry<Product, JSONObject> entry : vin.entrySet())
                array.add(entry.getKey().getJSON());

            for (Map.Entry<Product, JSONObject> entry : sprit.entrySet())
                array.add(entry.getKey().getJSON());

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

    public void removeProducts(Window aWindow) {
        for (UIProduct product : products) {
            product.remove(aWindow);
        }

        //products = new ArrayList<>();
    }

    public void roll(Window aWindow) {
        Random rand = new Random();
        aWindow.removeComp(rollButton);
        aWindow.removeComp(refreshButton);

        if (removeButton != null)
            aWindow.removeComp(removeButton);

        removeProducts(aWindow);

        rollButton = null;
        refreshButton = null;
        removeButton = null;

        Picture middle = new Picture(600, 300, size + 20, size + 20, line);
        middle.setCenter(1200/2, 600/2);
        aWindow.add(middle);

        List<UIProduct> products = new ArrayList<>();
        for (int i = 0; i < 250; i++) {//25 works
            UIProduct product = new UIProduct(defultProduct, blue, aWindow, 2000 + (size + 10) * i, 600/2 - size/2, size, size);
            setRandom(product);
            products.add(product);
            System.out.println(i + " : " + 250);
        }
        

        curSpeed = 2.5f;
        timer = rand.nextFloat(2) + 2;
        rolling = true;
        this.products = products;
    }

    private void setPic(Product product){
        try{
            var buffImage = getBuffImage(product.getJSON(), "url");
            var image = getImage(buffImage);
            product.setBufferedImage(buffImage);
            product.setImage(image);
        }catch(IOException e){
            System.out.println("did not find pic");
        }
    }

    public void setRandom(UIProduct uiProduct) {
        Random rand = new Random();
        float temp = rand.nextFloat();
        List<Product> keyList;

        if (temp < 0.40) { //öl 40 procent chans
            keyList = new ArrayList<>(ol.keySet());
            Product pro = keyList.get(rand.nextInt(keyList.size()));            
            setPic(pro);
            if (pro.name.contains("Norrlands"))
                uiProduct.setProduct(pro, rainbow);
            else
                uiProduct.setProduct(pro, blue);
        }
        else if (temp < 0.70) { //cider 30 procent chans
            keyList = new ArrayList<>(cider.keySet());
            Product pro = keyList.get(rand.nextInt(keyList.size()));
            setPic(pro);
            uiProduct.setProduct(pro, pink);
        }
        else if (temp < 0.88) { //vin 18 procent risk
            keyList = new ArrayList<>(vin.keySet());
            Product pro = keyList.get(rand.nextInt(keyList.size()));
            setPic(pro);
            uiProduct.setProduct(pro, red);
        }
        else { //sprit 12 procent risk
            keyList = new ArrayList<>(sprit.keySet());
            Product pro = keyList.get(rand.nextInt(keyList.size()));
            setPic(pro);
            uiProduct.setProduct(pro, yellow);
        }
    }

    public Product createProduct(JSONObject object) throws IOException {
        var name = getString(object, "productNameBold");
        var price = getDouble(object, "price");
        var type = getString(object, "categoryLevel1");
        var id = getString(object, "productNumber");
        var buffImage = getBuffImage(object, "images", "imageUrl");
        var image = getImage(buffImage);
        var ulr = getULR(object, "images");
        return new Product(name, price, type, id, image, buffImage, ulr);
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
            String temp = Normalizer.normalize(object.get(key).toString(), Normalizer.Form.NFD);
            temp = temp.replaceAll("[^\\p{ASCII}]", "");
            return temp;
        }
        catch (Exception e) {
            return (String) object.get(key);
        }
    }

    BufferedImage getBuffImage(JSONObject object, String key, String key2) throws IOException {
        JSONArray a = (JSONArray) object.get(key);
        JSONObject o = (JSONObject) a.get(0);
        URL url = new URL((String) o.get(key2) + "_400.png");
        BufferedImage c = ImageIO.read(url);
        return c;
    }

    BufferedImage getBuffImage(JSONObject object, String key) throws IOException {
        URL url = new URL((String) object.get(key));
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
