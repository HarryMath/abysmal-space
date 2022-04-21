package com.mikilangelo.abysmal.desktop.controller;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.mikilangelo.abysmal.screens.game.actors.ship.Ship;
import com.mikilangelo.abysmal.screens.game.components.Camera;
import com.mikilangelo.abysmal.screens.game.controllers.GameController;
import com.mikilangelo.abysmal.screens.game.controllers.TouchHandler;
import com.mikilangelo.abysmal.screens.game.uiElements.ButtonShield;
import com.mikilangelo.abysmal.screens.game.uiElements.ButtonShot;
import com.mikilangelo.abysmal.screens.game.uiElements.JoystickController;
import com.mikilangelo.abysmal.screens.game.uiElements.JoystickShooter;

public class DesktopController implements GameController {
  private TouchHandler touch1Handler;
  private TouchHandler touch2Handler;

  private JoystickController shipController;
  private JoystickShooter turretShooter;
  private ButtonShot shotButton;
  private ButtonShield shieldButton;

  @Override
  public void init(Ship ship, float w, float h) {
    touch1Handler = new TouchHandler(ship);
    touch2Handler = new TouchHandler(ship);

    shipController = new JoystickController(w, h);
    turretShooter = new JoystickShooter(w, h, ship.def.turretDefinitions.size > 0);
    shotButton = new ButtonShot(w, h, ship.def.lasersAmount > 0);
    shieldButton = new ButtonShield(ship.def.shieldRechargeTime / 1000f, w, h);
  }

  @Override
  public boolean process(Ship ship, Camera camera, float delta) {
    shieldButton.process(ship.getShieldAbilityReloadTime());
    if (Gdx.input.isTouched(1)) {
      touch2Handler.touchX = Gdx.input.getX(1);
      touch2Handler.touchY = Gdx.input.getY(1);
      touch2Handler.handleTouch(shipController, turretShooter, shotButton, shieldButton, delta);
    } else {
      touch2Handler.endTouch();
    }
    if (Gdx.input.isTouched(0)) {
      touch1Handler.touchX = Gdx.input.getX(0);
      touch1Handler.touchY = Gdx.input.getY(0);
      touch1Handler.handleTouch(shipController, turretShooter, shotButton, shieldButton, delta);
    } else {
      touch1Handler.endTouch();
    }
    ship.newAngle = ship.angle;
    if (Gdx.input.isKeyPressed(Input.Keys.W) || Gdx.input.isKeyPressed(Input.Keys.UP)) {
      ship.applyImpulse(1, true);
    }
    if (Gdx.input.isKeyPressed(Input.Keys.S) || Gdx.input.isKeyPressed(Input.Keys.DOWN)) {
      ship.applyImpulse(-0.01f, false);
    }
    if (Gdx.input.isKeyPressed(Input.Keys.A) || Gdx.input.isKeyPressed(Input.Keys.LEFT)) {
      ship.rotate(0.7f, delta);
    }
    if (Gdx.input.isKeyPressed(Input.Keys.D) || Gdx.input.isKeyPressed(Input.Keys.RIGHT)) {
      ship.rotate(-0.7f, delta);
    }
    if (Gdx.input.isKeyPressed(Input.Keys.SPACE)) {
      camera.zoomOut();
    }
    if (Gdx.input.isKeyPressed(Input.Keys.Z)) {
      camera.zoomIn();
    }
    return false;
  }

  @Override
  public void drawInterface(Batch batch) {
    turretShooter.draw(batch);
    shipController.draw(batch);
    shotButton.draw(batch);
    shieldButton.draw(batch);
  }

  @Override
  public void resizeComponents(float w, float h) {
    shipController.handleScreenResize(w, h);
    turretShooter.handleScreenResize(w, h);
    shotButton.handleScreenResize(w, h);
    shieldButton.handleScreenResize(w, h);
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
