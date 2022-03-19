package com.mikilangelo.abysmal.screens.game.controllers;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.mikilangelo.abysmal.screens.game.actors.ship.Ship;
import com.mikilangelo.abysmal.screens.game.components.Camera;

public interface GameController {
  void init(Ship ship, float w, float h);

  boolean process(Ship ship, Camera camera, float delta);

  void drawInterface(Batch batch);

  void resizeComponents(float w, float h);

  void dispose();
}
