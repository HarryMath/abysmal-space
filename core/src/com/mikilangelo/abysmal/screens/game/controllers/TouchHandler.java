package com.mikilangelo.abysmal.screens.game.controllers;

import static com.mikilangelo.abysmal.screens.game.GameScreen.HEIGHT;
import static com.mikilangelo.abysmal.screens.game.GameScreen.WIDTH;

import com.mikilangelo.abysmal.screens.game.actors.ship.Ship;
import com.mikilangelo.abysmal.screens.game.uiElements.ButtonShot;
import com.mikilangelo.abysmal.screens.game.uiElements.JoystickController;
import com.mikilangelo.abysmal.screens.game.uiElements.JoystickShooter;

public class TouchHandler {

  private static boolean hasCreatedJoystick = false;
  private static boolean hasCreatedShooter = false;

  public boolean createdJoystick = false;
  public boolean createdShooter = false;
  public boolean buttonClicked = false;
  private final Ship ship;

  public float touchX, touchY;

  public TouchHandler(Ship ship) {
    this.ship = ship;
    hasCreatedJoystick = hasCreatedShooter = false;
  }

  public boolean handleTouch(
          JoystickController shipController,
          JoystickShooter turretShooter,
          ButtonShot shotButton,
          float delta
  ) {
    buttonClicked = false;
    if (!createdShooter && !createdJoystick) {
      if (shotButton.contains(touchX, HEIGHT - touchY)) {
        ship.shotDirectly();
        buttonClicked = true;
      }
      // else if () {} TODO check if other buttons are clicked
    }
    if (!buttonClicked && touchY > HEIGHT * 0.25f && !createdShooter && !createdJoystick) {
      if (touchX < WIDTH * 0.33f) {
        if (!hasCreatedJoystick) {
          createdJoystick = hasCreatedJoystick = true;
          shipController.startTouch(touchX, HEIGHT - touchY);
        }
      } else if (turretShooter.isActive && touchX > WIDTH * 0.66f) {
        if (!hasCreatedShooter && turretShooter.contains(touchX, HEIGHT - touchY)) {
          turretShooter.startTouch(touchX, HEIGHT - touchY);
          createdShooter = hasCreatedShooter = true;
        }
      }
    }
    if (createdShooter) {
      turretShooter.update(touchX, HEIGHT - touchY);
      for (short i = 0; i < ship.turrets.size; i++) {
        ship.turrets.get(i).control(turretShooter.getDirection());
      }
      ship.shotByTurrets();
    }
    if (createdJoystick) {
      shipController.update(touchX, HEIGHT - touchY);
      ship.control(shipController.getDirection(), shipController.getPower(), delta);
    }
    return createdJoystick || createdShooter || buttonClicked;
  }

  public void endTouch() {
    if (createdShooter) {
      hasCreatedShooter = createdShooter = false;
    }
    if (createdJoystick) {
      hasCreatedJoystick = createdJoystick = false;
    }
  }
}
