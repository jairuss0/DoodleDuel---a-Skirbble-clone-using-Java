package skribbl_clone;


import java.awt.Color;
import java.awt.Point;

// - purpose of the class: to ensure the only new drawing reflects the current size and color

public class DrawPoint {
    private Point point;
    private Color color;
    private int size;
    
    public DrawPoint(Point point, Color color, int size){
        this.point = point;
        this.color = color;
        this.size = size;
    }
    
    public Point getPoint(){
        return point;
    }
    
    public Color getColor(){
        return color;
    }
    
    public int getSize(){
        return size;
    }
    
    
}
