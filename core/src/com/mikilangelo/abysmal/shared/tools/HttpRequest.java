package com.mikilangelo.abysmal.shared.tools;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public abstract class HttpRequest {

  public static String GET(String endpoint) throws IOException {
    URL url = new URL(endpoint);
    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
    connection.setRequestMethod("GET");
    connection.setRequestProperty("content-type", "application/json");
    System.out.println("[GET] response code: " + connection.getResponseCode());
    InputStream responseStream = connection.getInputStream();
    BufferedReader rd = new BufferedReader(new InputStreamReader(responseStream));
    StringBuilder responseBuilder = new StringBuilder(); // or StringBuffer if Java version 5+
    String line;
    while ((line = rd.readLine()) != null) {
      responseBuilder.append(line);
      responseBuilder.append('\r');
    }
    rd.close();
    return responseBuilder.toString().trim();
  }
}
