import javax.swing.JLabel;

interface UIObject {
    
    abstract void Update();
    abstract void setSize(int width, int height);
    abstract void setSize(float size);
    abstract void setPosition(float x, float y);
    abstract void move(float x, float y);
    abstract int getWidth();
    abstract int getHeight();
    abstract float getX();
    abstract JLabel getObject();
}
