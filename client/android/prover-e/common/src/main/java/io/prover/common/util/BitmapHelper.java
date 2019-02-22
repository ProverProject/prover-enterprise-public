package io.prover.common.util;

import android.graphics.Bitmap;
import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

public class BitmapHelper {

    public static void saveARGB(int[] argb, int width, int height, String path) {
        if (!path.startsWith("/")) {
            path = Environment.getExternalStorageDirectory().getPath() + File.separator + path;
        }

        Bitmap bitmap = Bitmap.createBitmap(argb, width, height, Bitmap.Config.ARGB_8888);
        try {
            OutputStream os = new FileOutputStream(path);
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, os);
            os.close();
        } catch (IOException e) {
            Log.e("BitmapHelper", e.getLocalizedMessage(), e);
        }
    }

    public static void saveARGB(byte[] argb, int width, int height, String path) {

        int[] colors = new int[width * height];
        for (int i = 0; i < colors.length; i++) {
            int pos = i * 4;
            colors[i] = (argb[pos] & 0xff)
                    | (argb[pos + 1] & 0xff) << 8
                    | (argb[pos + 2] & 0xff) << 16
                    | (argb[pos + 3] & 0xff) << 24;
        }

        saveARGB(colors, width, height, path);
    }

    public static void saveGrayscale(byte[] grayscale, int width, int height, String path) {

        int[] colors = new int[width * height];
        for (int i = 0; i < colors.length; i++) {

            int value = grayscale[i] & 0xff;
            colors[i] = 0xff000000 | value << 16 | value << 8 | value;
        }

        saveARGB(colors, width, height, path);
    }
}
