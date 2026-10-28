import java.io.FileReader;
import java.io.FileWriter;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.time.LocalDate;
import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JButton;
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
import java.awt.Desktop;
import java.awt.Image;
import java.awt.event.ActionListener;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import java.util.concurrent.*;

public class AlkoMain {
    boolean removeRegionalRestricted = false;

    List<Product> vin = new ArrayList<>();
    List<Product> ol = new ArrayList<>();
    List<Product> sprit = new ArrayList<>();
    List<Product> cider = new ArrayList<>();
    float lastTime = 0;
    float deltaTime = 0;
    float curSpeed = 0;
    float timer = 0;

    List<UIProduct> products = new ArrayList<>();
    boolean rolling = false;

    JButton rollButton;
    JButton refreshButton;
    JButton removeButton;
    JButton onlySprit;

    ImageIcon blue;
    ImageIcon pink;
    ImageIcon red;
    ImageIcon yellow;
    ImageIcon rainbow;
    ImageIcon line;

    static int size = 190;
    boolean spriteTime = false;

    public static void main(String[] args) {
        System.setProperty("file.encoding", "UTF-8");
        AlkoMain running = new AlkoMain();
        running.run();
    }

    public void run() {
        System.out.println("running");
        // refreshData();
        load("filtered.json", false);

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

        while (running) {
            float time = System.nanoTime();
            deltaTime = (time - lastTime) / 1000000;

            if (rolling) {
                // if (timer > 0)
                // System.out.println("time: " + timer);
                // System.out.println("speed: " + curSpeed);

                timer -= deltaTime / 1000;

                if (timer < 0 && curSpeed != 0)
                    curSpeed = clamp((curSpeed - 0.0003f) / 1.0005f, 0, 100);

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
                        setRandom(uiProduct);
                }
            }

            // temp.move(1, 0);
            window.Refresh();
            lastTime = time;
        }
    }

    public float getDeltaTime() {
        return deltaTime;
    }

    public void createRemoveButton(Window aWindow) {
        removeButton = new JButton("Remove product");
        removeButton.setBounds(160 + 10, 10, 160, 50);
        removeButton.addActionListener((actionListener) -> {
            removeMiddle(aWindow);
        });

        aWindow.add(removeButton);
    }

    public void createButtons(Window aWindow) {

        rollButton = new JButton("Roll");
        rollButton.setBounds(1200 / 2 - 100, 600 / 2 + 120, 200, 70);
        rollButton.addActionListener((actionListener) -> {
            roll(aWindow);
        });

        refreshButton = new JButton("Refresh (> 1h)");
        refreshButton.setBounds(10, 10, 160, 50);
        refreshButton.addActionListener((actionListener) -> {
            refreshData(aWindow);
        });
        onlySprit = new JButton("Change to sprit only");
        onlySprit.setBounds(10, 60, 160, 50);
        onlySprit.addActionListener((actionListener) -> {
            spriteTime = !spriteTime;
            if (spriteTime)
                onlySprit.setText("Change to everything");
            else
                onlySprit.setText("Change to sprit only");
        });
        aWindow.add(onlySprit);
        aWindow.add(rollButton);
        aWindow.add(refreshButton);
    }

    public void refreshData(Window aWindow) {
        aWindow.removeComp(rollButton);
        aWindow.removeComp(refreshButton);
        aWindow.removeComp(onlySprit);

        if (removeButton != null)
            aWindow.removeComp(removeButton);

        removeProducts(aWindow);

        try {
            vin = new ArrayList<>();
            ol = new ArrayList<>();
            cider = new ArrayList<>();
            sprit = new ArrayList<>();

            final Process p = new ProcessBuilder("python", "system.py").redirectErrorStream(true).start();
            try (InputStreamReader isr = new InputStreamReader(p.getInputStream())) {
                int c;
                while ((c = isr.read()) >= 0) {
                    System.out.print((char) c);
                    System.out.flush();
                }
            }

            System.out.println("Running python: " + p.isAlive());
            p.waitFor();
            
            load("data.json", true);
            saveToJSON();
            createButtons(aWindow);

        } catch (Exception e) {
            System.out.println("wrong");
            // TODO: handle exception
        }

    }

    public boolean objectChecks(JSONObject object) {
        return (!getBool(object, "isCompletelyOutOfStock") &&
                !getBool(object, "isTemporaryOutOfStock") &&
                !getBool(object, "isSupplierTemporaryNotAvailable") &&
                !getBool(object, "isDiscontinued") &&
                !getString(object, "categoryLevel1").equals("Alkoholfritt") &&
                !(removeRegionalRestricted && getBool(object, "isRegionalRestricted")) &&
                !(getString(object, "categoryLevel1").equals("Vin")
                    && getDouble(object, "volume") > 350) &&
                !(getString(object, "categoryLevel1").equals("Sprit")
                    && getDouble(object, "price") > 350) &&
                hasImage(object));
    }

    public void load(String fileName, Boolean doChecks) {
        ExecutorService pool = Executors.newFixedThreadPool(8);
        Monitor mon = new Monitor(vin, ol, sprit, cider);
        JSONParser parser = new JSONParser();

        ArrayList<Future<?>> tasks = new ArrayList<>();

        try {
            JSONArray tempArray = (JSONArray) parser.parse(new FileReader(fileName));
            mon.arraySize = tempArray.size();
            final int stepSize = tempArray.size() / 8;
            for (int j = 0; j < 8; j++) {
                final int J = j;
                Future<?> future = pool.submit(() -> {
                    for (int i = J * stepSize; i < (J + 1) * stepSize; i++) {
                        JSONObject jsonObject = (JSONObject) tempArray.get(i);

                        if (doChecks && !objectChecks(jsonObject)) {
                            mon.skip();
                            continue;
                        }

                        try {
                            Product product = null;

                            if (doChecks)
                                product = createProduct(jsonObject);
                            else
                                product = loadProduct(jsonObject);

                            switch (product.getType()) {
                                case "Vin":
                                    mon.addVin(product);
                                    break;

                                case "Ol":
                                    mon.addOl(product);
                                    break;

                                case "Cider & blanddrycker":
                                    mon.addCider(product);
                                    break;

                                case "Sprit":
                                    mon.addSprit(product);
                                    break;
                            }

                        } catch (Exception e) {
                            mon.error();
                        }
                    }
                    return 0;
                });
                tasks.add(future);
            }
        } catch (Exception e) {
        }
        for (var a : tasks) {
            try {
                a.get();
            } catch (Exception e) {

            }
        }
    }

    private class Monitor {
        public List<Product> vin;
        public List<Product> ol;
        public List<Product> sprit;
        public List<Product> cider;

        public int arraySize = 0;
        private int i = 0;

        boolean removeDupes = true;

        public Monitor(List<Product> vin, List<Product> ol, List<Product> sprit, List<Product> cider) {
            this.vin = vin;
            this.ol = ol;
            this.sprit = sprit;
            this.cider = cider;
        }

        public synchronized void addVin(Product product) {
            if (!removeDupes || !vin.contains(product)) {
                vin.add(product);
                System.out.println(i++ + " / " + arraySize);
            } else {
                System.out.println(i++ + " / " + arraySize + "(removed dupe)");
            }
        }

        public synchronized void addOl(Product product) {
            if (!removeDupes || !ol.contains(product)) {
                ol.add(product);
                System.out.println(i++ + " / " + arraySize);
            } else {
                System.out.println(i++ + " / " + arraySize + "(removed dupe)");
            }
        }

        public synchronized void addSprit(Product product) {
            if (!removeDupes || !sprit.contains(product)) {
                sprit.add(product);
                System.out.println(i++ + " / " + arraySize);
            } else {
                System.out.println(i++ + " / " + arraySize + "(removed dupe)");
            }
        }

        public synchronized void addCider(Product product) {
            if (!removeDupes || !cider.contains(product)) {
                cider.add(product);
                System.out.println(i++ + " / " + arraySize);
            } else {
                System.out.println(i++ + " / " + arraySize + "(removed dupe)");
            }
        }

        public synchronized void skip() {
            System.out.println(i++ + " / " + arraySize + "(skipped)");
        } 

        public synchronized void error() {
            System.out.println(i++ + " / " + arraySize + "(error)");
        }

    }

    public void removeMiddle(Window aWindow) {
        Product product = getMiddle(products);

        aWindow.removeComp(rollButton);
        aWindow.removeComp(refreshButton);
        aWindow.removeComp(removeButton);
        aWindow.removeComp(onlySprit);
        removeProducts(aWindow);

        removeAll(product);

        saveToJSON();
        createButtons(aWindow);
    }

    public void removeAll(Product aProduct) {
        vin.removeIf((element) -> (element.getId() == aProduct.getId()));
        ol.removeIf((element) -> (element.getId() == aProduct.getId()));
        cider.removeIf((element) -> (element.getId() == aProduct.getId()));
        sprit.removeIf((element) -> (element.getId() == aProduct.getId()));
    }

    public Product loadProduct(JSONObject object) throws IOException {
        var name = getString(object, "name");
        var price = getDouble(object, "price");
        var type = getString(object, "type");
        var id = getString(object, "id");
        var url = getString(object, "url");
        var buffImage = getBuffImage(object, "url");
        var image = getImage(buffImage);

        return new Product(name, price, type, id, image, buffImage, url);
    }

    public Product getMiddle(List<UIProduct> products) {
        for (UIProduct product : products) {
            if (product.isInside(1200 / 2))
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

    public void removeProducts(Window aWindow) {
        for (UIProduct product : products) {
            product.remove(aWindow);
        }

        products = new ArrayList<>();
    }

    public void roll(Window aWindow) {
        Random rand = new Random();
        aWindow.removeComp(rollButton);
        aWindow.removeComp(refreshButton);
        aWindow.removeComp(onlySprit);

        if (removeButton != null)
            aWindow.removeComp(removeButton);

        removeProducts(aWindow);

        rollButton = null;
        refreshButton = null;
        removeButton = null;

        Picture middle = new Picture(600, 300, size + 20, size + 20, line);
        middle.setCenter(1200 / 2, 600 / 2);
        aWindow.add(middle);

        List<UIProduct> products = new ArrayList<>();

        for (int i = 0; i < 20; i++) {
            UIProduct product = new UIProduct(ol.get(0), blue, aWindow, 2000 + (size + 10) * i, 600 / 2 - size / 2,
                    size, size);
            setRandom(product);
            products.add(product);
        }

        curSpeed = 2.5f;
        timer = rand.nextFloat(2) + 2;
        rolling = true;
        this.products = products;
    }

    public void setRandom(UIProduct uiProduct) {
        Random rand = new Random();
        float temp = rand.nextFloat();
        if (spriteTime) {
            Product pro = sprit.get(rand.nextInt(sprit.size()));
            uiProduct.setProduct(pro, yellow);
            return;
        }

        if (temp < 0.50) { // öl 50 procent chans
            Product pro = ol.get(rand.nextInt(ol.size()));

            if (pro.name.contains("Norrlands"))
                uiProduct.setProduct(pro, rainbow);
            else
                uiProduct.setProduct(pro, blue);
        } else if (temp < 0.78) { // cider 28 procent chans
            Product pro = cider.get(rand.nextInt(cider.size()));
            uiProduct.setProduct(pro, pink);
        } else if (temp < 0.90) { // vin 12 procent risk
            Product pro = vin.get(rand.nextInt(vin.size()));
            uiProduct.setProduct(pro, red);
        } else { // sprit 12 procent risk
            Product pro = sprit.get(rand.nextInt(sprit.size()));
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

    LocalDate getDate(JSONObject object, String key) {
        // Sätt ihop stringen här.
        String str = getString(object, key).substring(0, 10);
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-d");

        LocalDate d1 = LocalDate.parse(str, dtf);
        return d1;
    }

    double getDouble(JSONObject object, String key) {
        try {
            return ((Long) object.get(key)).doubleValue();
        } catch (Exception e) {
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
        } catch (Exception e) {
            return (String) object.get(key);
        }
    }

    BufferedImage getBuffImage(JSONObject object, String key, String key2) throws IOException {
        JSONArray a = (JSONArray) object.get(key);
        JSONObject o = (JSONObject) a.get(0);
        URL url = new URL((String) o.get(key2) + "_400.png");
        // System.out.println(url);
        BufferedImage c = ImageIO.read(url);
        return c;
    }

    BufferedImage getBuffImage(JSONObject object, String key) throws IOException {
        URL url = new URL((String) object.get(key));
        // System.out.println(url);
        BufferedImage c = ImageIO.read(url);
        return c;
    }

    ImageIcon getImage(BufferedImage buffImage) {
        ImageIcon image = new ImageIcon(buffImage.getScaledInstance(
                (size - 30) * buffImage.getWidth() / buffImage.getHeight(), size - 30, Image.SCALE_FAST));
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
