package chess.utils;

import java.awt.Point;

public class ImmutXY {
    private final int x; // col
    private final int y; // row

    public ImmutXY(int x_col, int y_row) {
        this.x = x_col;
        this.y = y_row;
    }
    public ImmutXY(Point p) {
        this.x = p.x;
        this.y = p.y;
    }
    public int getX() {
        return x;
    }
    public int getY() {
        return y;
    }

    @Override
    public String toString() {
        return "(col=" + x + ", row=" + y + ")";
    }

    public String toAlgebraic() { return String.format("%c%d", 'a' + x, 8 - y); }
    public String getAlgebraicFile() { return String.valueOf((char) ('a' + x)); }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null) return false;
        if (!(o instanceof ImmutXY)) return false;
        ImmutXY other = (ImmutXY) o;
        return this.x == other.x && this.y == other.y;
    }

    @Override
    public int hashCode() {
        return 31*x + 31*y;
    }
}
