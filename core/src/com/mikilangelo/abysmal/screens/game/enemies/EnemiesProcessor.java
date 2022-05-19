package com.mikilangelo.abysmal.screens.game.enemies;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.mikilangelo.abysmal.screens.game.actors.ship.Ship;
import com.mikilangelo.abysmal.screens.game.enemies.online.data.ShotData;
import com.mikilangelo.abysmal.screens.game.uiElements.Radar;

public interface EnemiesProcessor {

  Ship getNearestEnemy(float playerX, float playerY);

  void generateEnemies(Ship ship);

  void process(Ship player, float delta);

  void drawAll(Batch batch, float delta);

  void drawAtRadar(Batch batch, Radar radar);

  void drawAtRadar(Batch batch, Radar radar, float cameraRotation);

  void dispose();

  void shot(ShotData shotData);

  void explodeAsteroid(long asteroidId, float x, float y, float angle);
}
