package com.mikilangelo.abysmal.screens.menu.components;

import com.mikilangelo.abysmal.screens.menu.models.ServerDto;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public abstract class ServerProvider {

  private static final String mainServerUrl = "http://localhost:80/node/provide";
  public static ServerDto server;

  public final static Runnable findGlobalServer = () -> {
    try {
      System.out.println("LoadServer action started");
      URL url = new URL(mainServerUrl);
      HttpURLConnection connection = (HttpURLConnection) url.openConnection();
      connection.setRequestMethod("GET");
      connection.setDoOutput(true);
      OutputStream requestStream = connection.getOutputStream();
      requestStream.write(0);
      requestStream.flush();
      requestStream.close();
      InputStream responseStream = connection.getInputStream();
      BufferedReader rd = new BufferedReader(new InputStreamReader(responseStream));
      StringBuilder responseBuilder = new StringBuilder(); // or StringBuffer if Java version 5+
      String line;
      while ((line = rd.readLine()) != null) {
        responseBuilder.append(line);
        responseBuilder.append('\r');
      }
      rd.close();
      String response = responseBuilder.toString();
      System.out.println(response);
      server = new ServerDto();
      server.ip = getProperty(response, "ip").replace("::fff:", "");
      server.udpPort = Integer.parseInt(getProperty(response, "udpPort"));
      server.playersAmount = Integer.parseInt(getProperty(response, "playersAmount"));
    } catch (Exception ignore) {
      server = null;
      System.out.println("LoadServer action ended with error");
    }
  };

  private static String getProperty(String json, String param) {
    String value = json.split("\"" + param + "\":")[1].split("}")[0];
    value = value.contains(",") ? value.split(",")[0] : value;
    return value.replace("\"", "").trim();
  }
}
