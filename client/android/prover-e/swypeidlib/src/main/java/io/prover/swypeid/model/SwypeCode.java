package io.prover.swypeid.model;

import android.graphics.Matrix;

import java.util.Arrays;

public class SwypeCode {
    public final String swypeCode;

    private final int[] directions;

    private String swypeCodeV1;
    private String swypeCodeV2;
    private boolean cantConvertToV1;

    public SwypeCode(int[] directions) {
        this.directions = directions;
        swypeCode = getCodeV2();
    }

    public SwypeCode(String swypeCode) {
        this.swypeCode = swypeCode;
        if (swypeCode.charAt(0) == '5') {
            directions = directionsFromCodeV1(swypeCode);
        } else if (swypeCode.charAt(0) == '*') {
            directions = directionsFromCodeV2(swypeCode);
        } else {
            throw new RuntimeException("Can't parse code: " + swypeCode);
        }
    }

    private static Matrix getRotationMatrix(int orientationHint) {
        Matrix matrix = new Matrix();
        if (orientationHint == 0)
            return matrix;

        matrix.postRotate(orientationHint, 1, 1);
        return matrix;
    }

    public static int[] directionsFromCodeV2(String codeV2) {
        int directions[] = new int[codeV2.length() - 1];
        for (int i = 1; i < codeV2.length(); ++i) {
            directions[i - 1] = codeV2.charAt(i) - '0';
        }
        return directions;
    }

    public static int[] directionsFromCodeV1(String codeV1) {
        int directions[] = new int[codeV1.length() - 1];
        SwypePoint prev = SwypePoint.fromSwypePointV1(codeV1.charAt(0) - '1');
        for (int i = 1; i < codeV1.length(); ++i) {
            SwypePoint cur = SwypePoint.fromSwypePointV1(codeV1.charAt(i) - '1');
            directions[i - 1] = cur.minus(prev).direction();
            prev = cur;
        }
        return directions;
    }

    public int length() {
        return directions.length;
    }

    public String getCodeV2() {
        if (swypeCodeV2 == null) {
            char[] chars = new char[directions.length + 1];
            chars[0] = '*';
            for (int i = 0; i < directions.length; i++) {
                chars[i + 1] = (char) ('0' + directions[i]);
            }
            swypeCodeV2 = new String(chars);
        }
        return swypeCodeV2;
    }

    public String getCodeV1() {
        if (swypeCodeV1 == null && !cantConvertToV1) {
            char[] chars = new char[directions.length + 1];
            chars[0] = '5';
            SwypePoint pt = new SwypePoint(0, 0);
            for (int i = 0; i < directions.length; i++) {
                pt.offset(SwypePoint.fromDirection(directions[i]));
                if (pt.fitsV1())
                    chars[i + 1] = (char) ('0' + pt.toSwypePointV1());
                else {
                    cantConvertToV1 = true;
                    return swypeCodeV1 = null;
                }
            }
            swypeCodeV1 = new String(chars);
        }
        return swypeCodeV1;
    }

/*    public SwypeCode rotateV1(int orientationHint) {
        if (orientationHint == 0)
            return this;
        char[] chars = getCodeV1().toCharArray();
        float[] pt = new float[2];
        Matrix m = getRotationMatrix(-orientationHint);
        for (int i = 0; i < chars.length; i++) {
            char aChar = chars[i];
            int pos = aChar - '1';
            pt[0] = pos % 3;
            pt[1] = pos / 3;
            m.mapPoints(pt);
            pos = Math.round(pt[1] * 3 + pt[0]);

            chars[i] = (char) ('1' + pos);
        }
        return new SwypeCode(new String(chars));
    }*/

    public SwypeCode rotate(int orientationHint) {
        orientationHint = (orientationHint + 360) % 360;
        if (orientationHint == 0)
            return this;

        int diff = getDirectionDiffForOrientationHint(orientationHint);
        int[] newDirs = new int[directions.length];
        for (int i = 0; i < newDirs.length; i++) {
            newDirs[i] = (directions[i] + diff - 1) % 8 + 1;
        }
        return new SwypeCode(newDirs);
    }

    private int getDirectionDiffForOrientationHint(int orientationHint) {
        switch (orientationHint) {
            case 90:
                return 6;
            case 180:
                return 4;
            case 270:
                return 2;
        }
        return 0;
    }

    public SwypeCode flipVertical() {
        int[] newDirections = new int[directions.length];
        for (int i = 0; i < directions.length; i++) {
            newDirections[i] = SwypePoint.flipDirectionVertical(directions[i]);
        }
        return new SwypeCode(newDirections);
    }

    public SwypeCode flipHorizontal() {
        int[] newDirections = new int[directions.length];
        for (int i = 0; i < directions.length; i++) {
            newDirections[i] = SwypePoint.flipDirectionHorizontal(directions[i]);
        }
        return new SwypeCode(newDirections);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof SwypeCode)) return false;
        SwypeCode swypeCode = (SwypeCode) o;
        return Arrays.equals(directions, swypeCode.directions);
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(directions);
    }

    public int getDirectionAtIndex(int index) {
        return directions[index];
    }

}
