package com.mikilangelo.abysmal.screens.game.enemies.online;


import static com.mikilangelo.abysmal.screens.game.GameScreen.world;

import com.badlogic.gdx.Gdx;
import com.mikilangelo.abysmal.screens.game.actors.ship.Laser;
import com.mikilangelo.abysmal.screens.game.actors.ship.Ship;
import com.mikilangelo.abysmal.screens.game.enemies.Enemy;
import com.mikilangelo.abysmal.screens.game.enemies.online.data.PlayerState;
import com.mikilangelo.abysmal.screens.game.enemies.online.data.ShotData;
import com.mikilangelo.abysmal.screens.game.enemies.online.data.SimplifiedState;
import com.mikilangelo.abysmal.shared.repositories.ExplosionsRepository;
import com.mikilangelo.abysmal.shared.repositories.LasersRepository;
import com.mikilangelo.abysmal.shared.tools.CalculateUtils;
import com.mikilangelo.abysmal.shared.tools.Logger;

public class Player extends Enemy {

  public final String generationId;
  private boolean isPowerApplied = false;
  private long lastUpdateTimestamp = 0; // timestamp of the latest package
  private long lastStateTimeStamp = 0; // last time when update was
  private boolean isDead = false;
  private float currentPower = 0.5f;

  public Player(Ship ship, String id, String bodyId) {
    super(ship);
    this.generationId = id;
    ship.generationId = bodyId;
  }

  public Player(Ship ship, String id) {
    this(ship, id, id);
  }

  public void makeDead() {this.isDead = true;}

  public boolean isDead(float playerX, float playerY, float delta, long timestamp) {
    ship.distance = CalculateUtils.distance(playerX, playerY, ship.x, ship.y);
    if (
        isDead ||
        (ship.bodyData.health < -20 && timestamp - lastUpdateTimestamp > 500)
    ) {
      if (ship.distance < 100) {
        ExplosionsRepository.addShipExplosion(
                ship.x, ship.y, 1 - ship.distance * 0.01f,
                (ship.x - playerX) / ship.distance );
      }
      ship.destroy(world);
      return true;
    }
    if (ship.distance < 50) {
      ship.move(delta);
      if (isPowerApplied) {
        ship.kak();
        this.currentPower = this.currentPower * 0.96f + 0.04f;
      }
      ship.currentPower = this.currentPower;
    }
    return false;
  }

  public void update(final SimplifiedState data, long currentTime) {
    Gdx.app.postRunnable(() -> {
      if (!world.isLocked()) {
        ship.setSimpleState(data);
        lastUpdateTimestamp = currentTime;
        lastStateTimeStamp = currentTime - 5;
      }
    });
  }

  public float update(final PlayerState data, long currentTime) {
    Gdx.app.postRunnable(() -> {
      if (!world.isLocked() && data.timestamp > lastStateTimeStamp) {
        final float deltaTime = (currentTime - data.timestamp) * 0.001f;
        // Logger.log(this, "update", "delta is " + deltaTime + "s");
        isPowerApplied = data.isUnderControl;
        ship.bodyData.health = data.health;
        if (data.health <= 0) {
          this.isDead = true;
          return;
        }
        final long packagesDelay = currentTime - lastUpdateTimestamp;
        if (packagesDelay > 200) {
          ship.setState(data, deltaTime * 1.1f, 100f / packagesDelay);
        } else {
          ship.setState(data, deltaTime * 0.8f);
        }
        if (data.isUnderControl) {
          this.currentPower = 0.5f * (this.currentPower + 0.2f + data.currentPower * 0.8f);
        }
        lastUpdateTimestamp = currentTime;
        lastStateTimeStamp = data.timestamp;
      }
    });
    return (currentTime - data.timestamp) * 0.001f;
  }

  public void shot(ShotData shotData, long currentTime) {
    Gdx.app.postRunnable(() -> {
      if (shotData.withSound) {
        ship.playShotSound(shotData.gunId);
      }
      Laser l = new Laser(
              shotData.gunId < 0 ? ship.def.laserDefinition :
                      ship.def.turretDefinitions.get(shotData.gunId).laserDefinition,
              shotData,
              ship.bodyId,
              (currentTime - shotData.timestamp) * 0.001f
      );
      if (shotData.gunId < 0) {
        LasersRepository.addSimple(l);
      } else {
        LasersRepository.addTurret(l);
      }
    });
  }
}