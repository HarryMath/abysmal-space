package com.mikilangelo.abysmal.screens.game.enemies.bots;

import com.mikilangelo.abysmal.screens.game.actors.ship.Ship;

public interface BotStrategy {
  void perform(Ship bot, float playerX, float playerY, float playerAngle, float delta);
}
