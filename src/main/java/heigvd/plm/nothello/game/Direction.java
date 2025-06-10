package heigvd.plm.nothello.game;

public class Direction {
    private int x;
    private int y;

    public Direction(int x, int y) {
        if (x < -1 || y < -1 || x > 1 || y > 1 || (x == 0 && y == 0)) {
            throw new IllegalArgumentException("Invalid direction: (" + x + ", " + y + "). Must be in range [-1, 1] and not (0, 0).");
        }
        this.x = x;
        this.y = y;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public static Direction[] getAllDirections() {
        return new Direction[]{
            new Direction(-1, -1), new Direction(-1, 0), new Direction(-1, 1),
            new Direction(0, -1), new Direction(0, 1),
            new Direction(1, -1), new Direction(1, 0), new Direction(1, 1)
        };
    }
}
