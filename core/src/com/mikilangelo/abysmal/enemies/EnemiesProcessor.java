package com.mikilangelo.abysmal.enemies;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.mikilangelo.abysmal.models.game.Ship;
import com.mikilangelo.abysmal.ui.gameElemets.Radar;

public interface EnemiesProcessor {

  Ship getNearestEnemy(float playerX, float playerY);

  void generateEnemies(Ship ship);

  void process(Ship player, float delta);

  void drawAll(Batch objectsBatch, float delta);

  void drawAtRadar(Batch batch, Radar radar);

  void dispose();

  void shot();
}
