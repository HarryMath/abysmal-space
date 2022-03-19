package com.mikilangelo.abysmal.screens.game.controllers;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.mikilangelo.abysmal.screens.game.actors.ship.Ship;

public interface GameController {
  void init(Ship ship, float w, float h);

  void handleControls(float delta);

  void drawInterface(Batch batch);

  void resizeComponents(float w, float h);

  void dispose();
}
