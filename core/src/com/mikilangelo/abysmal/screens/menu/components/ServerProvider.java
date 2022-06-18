package com.mikilangelo.abysmal.screens.menu.components;

import com.mikilangelo.abysmal.screens.game.enemies.online.UdpServer;
import com.mikilangelo.abysmal.screens.game.enemies.online.data.BroadcastRequest;
import com.mikilangelo.abysmal.screens.game.enemies.online.data.BroadcastResponse;
import com.mikilangelo.abysmal.screens.menu.models.ServerDto;
import com.mikilangelo.abysmal.shared.tools.HttpRequest;
import com.mikilangelo.abysmal.shared.tools.Logger;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.Arrays;

public abstract class ServerProvider {

  private static final String mainServerUrl = "https://abysmal-space.herokuapp.com";
  public static ServerDto globalServer;
  public static ServerDto localServer;

  public final static Runnable findGlobalServer = () -> {
    try {
      String response = HttpRequest.GET(mainServerUrl + "/nodes/provide");
      Logger.log("ServerProvider", "findGlobalServer", "response: " + response);
      globalServer = new ServerDto();
      globalServer.ip = getProperty(response, "ip").replace("::ffff:", "");
      globalServer.udpPort = Integer.parseInt(getProperty(response, "udpPort"));
      globalServer.playersAmount = Integer.parseInt(getProperty(response, "playersAmount"));
      globalServer.seed = Long.parseLong(getProperty(response, "seed"));
      long t0 = System.currentTimeMillis();
      response = HttpRequest.GET(mainServerUrl + "/time");
      long serverTimestamp = Long.parseLong(getProperty(response, "timestamp"));
      long t1 = System.currentTimeMillis();
      int requestDuration = (int) (t1 - t0);
      globalServer.correction = serverTimestamp - (t1 - Math.round(requestDuration * 0.1f));
      // globalServer.correction = serverTimestamp - t1 - Math.round(requestDuration * 0.25f);
      Logger.log("ServerProvider", "findGlobalServer", "correction: " + globalServer.correction);
    } catch (Exception ignore) {
      Logger.log("ServerProvider", "findGlobalServer", "error getting global server:");
      ignore.printStackTrace();
      globalServer = null;
    }
  };

  public final static Runnable findLocalServer = () -> {
    try {
      final byte[] message = new BroadcastRequest().compress();
      final DatagramSocket socket = new DatagramSocket();
      socket.setBroadcast(true);
      final BroadcastReceiveThread receiveThread = new BroadcastReceiveThread(socket);
      receiveThread.start();
      int attempts = 0;
      while (!receiveThread.serverFound && attempts < 10) {
        attempts++;
        Logger.log("ServerProvider", "findLocalSever", "attempt " + attempts);
        DatagramPacket packet = new DatagramPacket(
                message, message.length,
                InetAddress.getByName("255.255.255.255"),
                UdpServer.PORT
        );
        socket.send(packet);
        Logger.log("ServerProvider", "findLocalSever", "data sent");
        Thread.sleep(250);
      }
      receiveThread.interrupt();

    } catch (Exception e) {
      e.printStackTrace();
      localServer = null;
    }
  };

  private static String getProperty(String json, String param) {
    String value = json.split("\"" + param + "\":")[1].replace("}", "");
    value = value.contains(",") ? value.split(",")[0] : value;
    return value.replace("\"", "").trim();
  }

  private static class BroadcastReceiveThread extends Thread {

    private final DatagramSocket socket;
    private boolean serverFound = false;

    private BroadcastReceiveThread(DatagramSocket socket) {
      this.socket = socket;
    }

    @Override
    public void run() {
      byte attempts = 0;
      while (!serverFound && attempts < 100) {
        try {
          attempts++;
          Thread.sleep(1);
          Logger.log(this, "run", "trying receive data");
          DatagramPacket receivePacket = new DatagramPacket(new byte[64], 64);
          socket.receive(receivePacket);
          BroadcastResponse response = new BroadcastResponse(receivePacket.getData(), receivePacket.getLength());
          final long t = System.currentTimeMillis();
          localServer = new ServerDto();
          localServer.ip = receivePacket.getAddress().getHostAddress();
          localServer.udpPort = receivePacket.getPort();
          Logger.log(this, "run", "server is " + localServer.ip + ":" + localServer.udpPort);
          localServer.playersAmount = 1;
          localServer.correction = response.timestamp - t;
          localServer.seed = response.seed;
          Logger.log(this, "run", "correction: " + localServer.correction);
        } catch (Exception e) {
          e.printStackTrace();
        }
      }
    }

    @Override
    public void interrupt() {
      super.interrupt();
      this.socket.close();
    }
  }

  private byte[] trimPackage(byte[] data) {
    int i = (short) data.length - 1;
    while (i >= 0 && data[i] == 0) {
      i--;
    }
    return Arrays.copyOf(data, i+1);
  }

}
