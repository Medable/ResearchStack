package org.researchstack.backbone.omron.controller.util;

import androidx.annotation.NonNull;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigDecimal;

import jp.co.ohq.utility.StringEx;

@SuppressWarnings("unused")
public class Common {

    private final static int MAX_FRACTION_DIGITS = 3;

    private Common() {
        // must be not create instance.
    }

    @NonNull
    public static String getNumberString(BigDecimal value) {
        return StringEx.toNumberString(value);
    }

    @NonNull
    public static String getNumberStringWithUnit(BigDecimal value, @NonNull String unit) {
        return getNumberString(value) + " " + unit;
    }

    @NonNull
    public static String getDecimalString(BigDecimal value, int minDigits) {
        return StringEx.toDecimalString(value, minDigits, MAX_FRACTION_DIGITS);
    }

    @NonNull
    public static String getDecimalStringWithUnit(BigDecimal value, int minDigits, @NonNull String unit) {
        return getDecimalString(value, minDigits) + " " + unit;
    }

    public static String getPercentString(BigDecimal value, int minDigits) {
        return getDecimalString(value.multiply(new BigDecimal("100.0")), minDigits);
    }

    public static String getPercentStringWithUnit(BigDecimal value, int minDigits) {
        return StringEx.toPercentString(value, minDigits, MAX_FRACTION_DIGITS);
    }

    private static String readKernelVersion() {
        InputStream inputStream = null;
        BufferedReader bufferedReader = null;
        try {
            Process process = Runtime.getRuntime().exec(new String[]{
                    "cat", "/proc/version"
            });

            if (process.waitFor() == 0) {
                inputStream = process.getInputStream();
            } else {
                inputStream = process.getErrorStream();
            }
            bufferedReader = new BufferedReader(new InputStreamReader(inputStream), 1024);
            return bufferedReader.readLine();
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        } finally {
            try {
                if (inputStream != null) {
                    inputStream.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

            try {
                if (bufferedReader != null) {
                    bufferedReader.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return null;
    }
}