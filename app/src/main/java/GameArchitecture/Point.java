package GameArchitecture;

public class Point {
    public final int x;
    public final int y;

    public Point(int x, int y) {
        this.x = x;
        this.y = y;
    }

    @Override
    public String toString() {
        if (x >= 0 && x < 26)
            return (char)('A' + x) + Integer.toString(y + 1);
        else
            return "(" + (x + 1) + ", " + (y + 1) + ')';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Point point = (Point) o;
        return x == point.x &&
                y == point.y;
    }

    @Override
    public int hashCode() {
        return Long.valueOf(x << 32 + y).hashCode();
    }
}
