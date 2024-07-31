import java.awt.*;

public class Rect {
    final int x;
    final int y;
    final int w;
    final int h;

    public Rect(int x, int y, int w, int h) {
        this.x = x;
        this.y = y;
        this.w = w;
        this.h = h;
    }

    public Point Center() {
        return new Point(this.x + (int) (this.w / 2), this.y + (int) (this.h / 2));
    }
}
