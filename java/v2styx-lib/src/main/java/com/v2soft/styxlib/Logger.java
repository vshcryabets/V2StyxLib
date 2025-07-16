package com.v2soft.styxlib;

import java.io.PrintStream;

public class Logger {
    public static final PrintStream DEBUG = System.out;

    private static void message(String tag, String id, String message) {
        String currentTime = java.time.LocalTime.now().toString();
        long tid = Thread.currentThread().getId();
        DEBUG.println(currentTime + "\t" + tid + "\t" + id + "\t" + tag + ":\t" + message);
    }

    public static void d(String tag, String message) {
        message(tag, "D", message);
    }

    public static void info(String tag, String message) {
        message(tag, "I", message);
    }

    public static void e(String tag, String message) {
        message(tag, "E", message);
    }
}
