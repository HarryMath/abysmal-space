package com.mikilangelo.abysmal.screens.menu.components;

import com.mikilangelo.abysmal.screens.menu.models.ServerDto;
import com.mikilangelo.abysmal.shared.tools.HttpRequest;

public abstract class ServerProvider {

  private static final String mainServerUrl = "https://abysmal-space.herokuapp.com";
  public static ServerDto server;

  public final static Runnable findGlobalServer = () -> {
    try {
      String response = HttpRequest.GET(mainServerUrl + "/nodes/provide");
      System.out.println("[ServerProvider] response: " + response);
      server = new ServerDto();
      server.ip = getProperty(response, "ip").replace("::ffff:", "");
      server.udpPort = Integer.parseInt(getProperty(response, "udpPort"));
      server.playersAmount = Integer.parseInt(getProperty(response, "playersAmount"));
      server.seed = Long.parseLong(getProperty(response, "seed"));
      long t0 = System.currentTimeMillis();
      response = HttpRequest.GET(mainServerUrl + "/time");
      long serverTimestamp = Long.parseLong(getProperty(response, "timestamp"));
      long t1 = System.currentTimeMillis();
      int requestDuration = (int) (t1 - t0);
      server.correction = serverTimestamp - t1 - Math.round(requestDuration * 0.25f);
      System.out.println("correction is: " + server.correction);
    } catch (Exception ignore) {
      System.out.println("[ServerProvider] error getting global server");
      ignore.printStackTrace();
      server = null;
    }
  };

  private static String getProperty(String json, String param) {
    String value = json.split("\"" + param + "\":")[1].replace("}", "");
    value = value.contains(",") ? value.split(",")[0] : value;
    return value.replace("\"", "").trim();
  }
}
