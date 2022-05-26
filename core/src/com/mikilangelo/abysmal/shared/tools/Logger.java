package com.mikilangelo.abysmal.shared.tools;

import com.mikilangelo.abysmal.shared.Settings;

import java.sql.Timestamp;

public class Logger {

  private static final boolean IS_ENABLED = true;

  public static void log(Object classObject, String method, String message) {
    if (IS_ENABLED || Settings.debug) {
      log(classObject.getClass().getSimpleName(), method, message);
    }
  }

  public static void log(String className, String method, String message) {
    if (IS_ENABLED || Settings.debug) {
      method = method.length() == 0 ? "()" : "." + method + "()";
      System.out.println(time() + " [ " + className + method + " ] " + message);
    }
  }

  private static String padRight(String s, int n) {
    return s + "000000000000000000000000".substring(0, n - s.length());
  }

  private static String time() {
    return padRight(
            new Timestamp(System.currentTimeMillis()).toString()
            .replace(".", ":")
            .replace("-", "."), 23);
  }
}
