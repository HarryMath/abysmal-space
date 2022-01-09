package com.mikilangelo.abysmal.enemies.bots;

import static com.mikilangelo.abysmal.ui.screens.GameScreen.SCREEN_WIDTH;

import com.badlogic.gdx.math.MathUtils;
import com.mikilangelo.abysmal.components.repositories.ExplosionsRepository;
import com.mikilangelo.abysmal.models.game.Ship;
import com.mikilangelo.abysmal.models.objectsData.DestroyableObjectData;
import com.mikilangelo.abysmal.tools.Geometry;

import java.util.Locale;

class Bot {

  private final BotStrategy strategy;
  Ship ship;

  public Bot(Ship ship) {
    this.ship = ship;
    final String name = ship.def.name.toLowerCase(Locale.ROOT);
    if (name.contains("defender")) {
      this.strategy =
              Geometry.getProbability(0.5f) ? new StrategyHitAndRun() :
              Geometry.getProbability(0.3f) ? new StrategyWave() :
              Geometry.getProbability(0.4f) ? new StrategyKamikaze() : new StrategySniper();
    } else if (name.contains("invader")) {
      this.strategy = Geometry.getProbability(0.4f) ?
              new StrategySniper() : new StrategyKamikaze();
    } else if (name.contains("hyperion")) {
      this.strategy = Geometry.getProbability(0.6f) ?
              new StrategyHitAndRun() : new StrategySniper();
    } else if (name.contains("alien")){
      this.strategy = Geometry.getProbability(0.81f) ?
              new StrategyHitAndRun() : new StrategyKamikaze();
    } else {
      System.out.println("Unknown ship: " + name);
      this.strategy = Geometry.getProbability(0.333f) ?
              new StrategyHitAndRun() : Geometry.getProbability(0.5f) ?
              new StrategyKamikaze() : new StrategySniper();
    }
  }

  public boolean control(float playerX, float playerY, float playerAngle, float delta) {
    ship.distance = Geometry.distance(playerX, playerY, ship.x, ship.y);
    if (ship.distance > SCREEN_WIDTH * 9) {
      return false;
    }
    if (((DestroyableObjectData) ship.body.getUserData()).getHealth() <= 0) {
      if (ship.distance < 100) {
        ExplosionsRepository.addShipExplosion(ship.x, ship.y,
                1 - ship.distance * 0.01f, (ship.x - playerX) / ship.distance);
      }
      return false;
    }
    ship.move(delta);
    if (ship.distance > ship.def.radarPower) {
      ship.control((ship.angle + MathUtils.random(-0.0009f, 0.003f)) % MathUtils.PI2, 0.8f, delta);
      return true;
    }
    strategy.perform(ship, playerX, playerY, playerAngle, delta);
    return true;
  }
}
