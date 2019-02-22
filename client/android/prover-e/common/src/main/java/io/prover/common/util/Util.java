package io.prover.common.util;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

import java.math.BigInteger;
import java.util.Arrays;

/**
 * Created by babay on 12.01.2018.
 */

public class Util {
    public static void hideKeyboard(View any) {
        InputMethodManager imm = (InputMethodManager) any.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(any.getWindowToken(), 0);
    }

    public static String toDecimalString(BigInteger value, int pointShift, int minDigitsAfterPoint, String point, String splitChar) {
        String valueStr = value.toString();
        if (valueStr.length() < pointShift + 1) {
            int prependZeroes = pointShift + 1 - valueStr.length();
            char[] charArray = new char[prependZeroes];
            Arrays.fill(charArray, '0');
            valueStr = new String(charArray) + valueStr;
        }
        String part1 = valueStr.substring(0, valueStr.length() - pointShift);
        StringBuilder result = new StringBuilder();
        if (splitChar == null) {
            result.append(part1);
        } else {
            int firstPart = part1.length() % 3;
            if (firstPart == 0)
                firstPart = 3;
            int parts = (part1.length() - 1) / 3 + 1;
            result.append(part1, 0, firstPart);
            for (int i = 1; i < parts; i++) {
                result.append(splitChar);
                result.append(part1, firstPart + 3 * (i - 1), firstPart + 3 * i);
            }
        }

        int minBackZeroPos = valueStr.length() - pointShift + minDigitsAfterPoint;
        int backZeroes = 0;
        for (int i = valueStr.length() - 1; i >= minBackZeroPos; i--) {
            if (valueStr.charAt(i) != '0') {
                break;
            }
            ++backZeroes;
        }

        if (backZeroes < pointShift) {
            result.append(point);
            result.append(valueStr, valueStr.length() - pointShift, valueStr.length() - backZeroes);
        }


        return result.toString();
    }

    public static void copyToClipboard(Context context, String message) {
        ClipboardManager clipboard = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText(message, message);
        clipboard.setPrimaryClip(clip);
    }
}
