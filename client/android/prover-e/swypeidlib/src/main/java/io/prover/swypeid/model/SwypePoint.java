package io.prover.swypeid.model;

public class SwypePoint {

    public int x, y;

    public SwypePoint(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public static SwypePoint fromSwypePointV1(int point) {
        int x = point % 3 - 1;
        int y = 1 - point / 3;
        return new SwypePoint(x, y);
    }

    /**
     * x from left to right
     * y from bottom to top
     *
     * @param direction
     * @return
     */
    public static SwypePoint fromDirection(int direction) {
        direction = (direction - 1) % 8 + 1;
        switch (direction) {
            case 1:
                return new SwypePoint(0, -1);

            case 2:
                return new SwypePoint(-1, -1);
            case 3:
                return new SwypePoint(-1, 0);
            case 4:
                return new SwypePoint(-1, 1);
            case 5:
                return new SwypePoint(0, 1);
            case 6:
                return new SwypePoint(1, 1);
            case 7:
                return new SwypePoint(1, 0);
            case 8:
                return new SwypePoint(1, -1);
            default:
                throw new RuntimeException("can't parse direction: " + direction);
        }
    }

    public static int flipDirectionVertical(int direction) {
        direction = (direction - 1) % 8 + 1;
        switch (direction) {
            case 1:
                return 5;
            case 2:
                return 4;
            case 3:
                return 3;
            case 4:
                return 2;
            case 5:
                return 1;
            case 6:
                return 8;
            case 7:
                return 7;
            case 8:
                return 6;

            default:
                throw new RuntimeException("can't parse direction: " + direction);
        }
    }

    public static int flipDirectionHorizontal(int direction) {
        direction = (direction - 1) % 8 + 1;
        switch (direction) {
            case 1:
                return 1;
            case 2:
                return 8;
            case 3:
                return 7;
            case 4:
                return 6;
            case 5:
                return 5;
            case 6:
                return 4;
            case 7:
                return 3;
            case 8:
                return 2;

            default:
                throw new RuntimeException("can't parse direction: " + direction);
        }
    }

    public SwypePoint minus(SwypePoint other) {
        return new SwypePoint(x - other.x, y - other.y);
    }

    public int direction() {
        switch (x) {
            case 0:
                if (y > 0)
                    return 5;
                else
                    return 1;

            case 1:
                switch (y) {
                    case 1:
                        return 6;
                    case 0:
                        return 7;
                    case -1:
                        return 8;
                }
            case -1:
                switch (y) {
                    case 1:
                        return 4;
                    case 0:
                        return 3;
                    case -1:
                        return 2;
                }

        }
        throw new RuntimeException(String.format("Not implemented for: (%d,%d)", x, y));
    }

    public void offset(SwypePoint o) {
        x += o.x;
        y += o.y;
    }

    public int toSwypePointV1() {
        return (1 - y) * 3 + x + 1 + 1;
    }

    @Override
    public String toString() {
        return "(" + x + "," + y + ")";
    }

    public boolean fitsV1() {
        return y >= -1 && y <= 1 && x >= -1 && x <= 1;
    }
}
