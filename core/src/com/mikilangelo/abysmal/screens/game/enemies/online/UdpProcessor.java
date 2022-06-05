package com.mikilangelo.abysmal.screens.game.enemies.online;

import com.mikilangelo.abysmal.screens.game.enemies.online.data.PlayerState;
import com.mikilangelo.abysmal.shared.tools.Logger;

import java.net.DatagramPacket;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

public abstract class UdpProcessor {

  private final Map<String, Integer> playersToCreate = new HashMap<>();
  protected final AtomicBoolean isStopped = new AtomicBoolean(false);

  protected byte[] output;
  protected DatagramPacket outputPacket;
  protected DatagramPacket inputPacket;
  protected final PlayerState state = new PlayerState();
  protected byte missedFrames = 0;
  protected Thread receiveThread;

  protected boolean isNewPlayer(String genId) {
    if (genId.length() <= 2) {
      return false;
    }
    Logger.log(this, "verifyNewPlayer", "new player: " + genId);
    if (playersToCreate.containsKey(genId)) {
      int packagesReceive = playersToCreate.get(genId);
      packagesReceive += 1;
      playersToCreate.put(genId, packagesReceive);
      if (packagesReceive > 5) {
        playersToCreate.remove(genId);
        return true;
      }
    } else {
      playersToCreate.put(genId, 0);
    }
    return false;
  }

  public void dispose() {
    playersToCreate.clear();
  }
}
