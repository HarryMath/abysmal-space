package com.mikilangelo.abysmal.enemies.bots;

import com.mikilangelo.abysmal.models.game.Ship;

public interface BotStrategy {
  void perform(Ship bot, float playerX, float playerY, float playerAngle, float delta);
}
