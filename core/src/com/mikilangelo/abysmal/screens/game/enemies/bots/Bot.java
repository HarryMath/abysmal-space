package com.mikilangelo.abysmal.screens.game.enemies.bots;

import static com.mikilangelo.abysmal.screens.game.GameScreen.SCREEN_WIDTH;

import com.badlogic.gdx.math.MathUtils;
import com.mikilangelo.abysmal.shared.repositories.ExplosionsRepository;
import com.mikilangelo.abysmal.screens.game.enemies.Enemy;
import com.mikilangelo.abysmal.screens.game.actors.ship.Ship;
import com.mikilangelo.abysmal.screens.game.objectsData.DestroyableObjectData;
import com.mikilangelo.abysmal.shared.tools.CalculateUtils;

import java.util.Locale;

class Bot extends Enemy {

  private final BotStrategy strategy;

  public Bot(Ship ship) {
    super(ship);
    final String name = ship.def.name.toLowerCase(Locale.ROOT);
//    if (name.contains("defender")) {
//      this.strategy =
//              CalculateUtils.getProbability(0.5f) ? new StrategyHitAndRun() :
//              CalculateUtils.getProbability(0.3f) ? new StrategyWave() :
//              CalculateUtils.getProbability(0.4f) ? new StrategyKamikaze() : new StrategySniper();
//    } else if (name.contains("invader")) {
//      this.strategy = CalculateUtils.getProbability(0.4f) ?
//              new StrategySniper() : new StrategyKamikaze();
//    } else if (name.contains("hyperion")) {
//      this.strategy = CalculateUtils.getProbability(0.6f) ?
//              new StrategyHitAndRun() : new StrategySniper();
//    } else if (name.contains("alien")){
//      this.strategy = CalculateUtils.getProbability(0.81f) ?
//              new StrategyHitAndRun() : new StrategyKamikaze();
//    } else {
//      System.out.println("Unknown ship: " + name);
//      this.strategy = CalculateUtils.getProbability(0.333f) ?
//              new StrategyHitAndRun() : CalculateUtils.getProbability(0.5f) ?
//              new StrategyKamikaze() : new StrategySniper();
//    }
    this.strategy = new StrategyHitAndRun();
  }

  public boolean control(float playerX, float playerY, float playerAngle, float delta) {
    ship.distance = CalculateUtils.distance(playerX, playerY, ship.x, ship.y);
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
