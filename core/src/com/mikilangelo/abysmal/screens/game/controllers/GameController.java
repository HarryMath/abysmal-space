package com.mikilangelo.abysmal.screens.game.controllers;

import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.mikilangelo.abysmal.screens.game.actors.ship.PlayerShip;
import com.mikilangelo.abysmal.screens.game.actors.ship.Ship;
import com.mikilangelo.abysmal.screens.game.components.Camera;

public interface GameController {
  void init(PlayerShip ship, float w, float h);

  InputProcessor getGestureListener();

  boolean process(PlayerShip ship, Camera camera, float delta);

  void drawInterface(Batch batch, BitmapFont font);

  void resizeComponents(float w, float h);

  void dispose();
}
