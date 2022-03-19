package com.mikilangelo.abysmal.controller;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.mikilangelo.abysmal.screens.game.actors.ship.Ship;
import com.mikilangelo.abysmal.screens.game.components.Camera;
import com.mikilangelo.abysmal.screens.game.controllers.GameController;
import com.mikilangelo.abysmal.screens.game.controllers.TouchHandler;
import com.mikilangelo.abysmal.screens.game.uiElements.ButtonShot;
import com.mikilangelo.abysmal.screens.game.uiElements.JoystickController;
import com.mikilangelo.abysmal.screens.game.uiElements.JoystickShooter;

public class SensorController implements GameController {

  private TouchHandler touch1Handler;
  private TouchHandler touch2Handler;

  private JoystickController shipController;
  private JoystickShooter turretShooter;
  private ButtonShot shotButton;

  @Override
  public void init(Ship ship, float w, float h) {
    touch1Handler = new TouchHandler(ship);
    touch2Handler = new TouchHandler(ship);

    shipController = new JoystickController(w, h);
    turretShooter = new JoystickShooter(w, h, ship.def.turretDefinitions.size > 0);
    shotButton = new ButtonShot(w, h, ship.def.lasersAmount > 0);
  }

  @Override
  public boolean process(Ship ship, Camera camera, float delta) {
    boolean isScreenUsed = false;
    if (Gdx.input.isTouched(1)) {
      touch2Handler.touchX = Gdx.input.getX(1);
      touch2Handler.touchY = Gdx.input.getY(1);
      isScreenUsed = touch2Handler.handleTouch(shipController, turretShooter, shotButton, delta);
    } else {
      touch2Handler.endTouch();
    }
    if (Gdx.input.isTouched(0)) {
      touch1Handler.touchX = Gdx.input.getX(0);
      touch1Handler.touchY = Gdx.input.getY(0);
      isScreenUsed = touch1Handler.handleTouch(shipController, turretShooter, shotButton, delta)
              || isScreenUsed;
    } else {
      touch1Handler.endTouch();
    }
    return isScreenUsed;
  }

  @Override
  public void drawInterface(Batch batch) {
    turretShooter.draw(batch);
    shipController.draw(batch);
    shotButton.draw(batch);
  }

  @Override
  public void resizeComponents(float w, float h) {
    shipController.handleScreenResize(w, h);
    turretShooter.handleScreenResize(w, h);
    shotButton.handleScreenResize(w, h);
  }

  @Override
  public void dispose() {
    touch1Handler = null;
    touch2Handler = null;
    shipController = null;
    turretShooter = null;
    shotButton = null;
  }
}
