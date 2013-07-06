package pl.edu.agh.io.mushrooming.loaders;

import android.graphics.Bitmap;

/**
 * Created with IntelliJ IDEA.
 * User: prot
 * Date: 11.05.13
 * Time: 18:17
 */
public class Element {
    private final int value;
    private final Bitmap miniBitmap;
    private Bitmap bitmap;
    private int x;
    private int y;
    private int destinationX;
    private int destinationY;
    private int currentFieldId;
    private int wantedId;

    public Element(Bitmap bitmap, int x, int y, int value, Bitmap miniBitmap) {
        this.bitmap = bitmap;
        this.x = x;
        this.y = y;
        this.value = value;
        this.miniBitmap = miniBitmap;
        destinationX = x;
        destinationY = y;
        currentFieldId = 0;
        wantedId = 0;
    }

    public Bitmap getBitmap() {
        return bitmap;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public void setX(int x) {
        this.x = x;
    }

    public void setY(int y) {
        this.y = y;
    }

    public int getDestinationX() {
        return destinationX;
    }

    public void setDestinationX(int destinationX) {
        this.destinationX = destinationX;
    }

    public int getDestinationY() {
        return destinationY;
    }

    public void setDestinationY(int destinationY) {
        this.destinationY = destinationY;
    }

    public int getCurrentFieldId() {
        return currentFieldId;
    }

    public void setCurrentFieldId(int currentFieldId) {
        this.currentFieldId = currentFieldId;
    }

    public int getValue() {
        return value;
    }

    public void setWantedId(int wantedId) {
        this.wantedId = wantedId;
    }

    public int getWantedId() {
        return wantedId;
    }

    public Bitmap getMiniBitmap() {
        return miniBitmap;
    }
}
