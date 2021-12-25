package com.mikilangelo.abysmal.components;

import static com.mikilangelo.abysmal.ui.screens.GameScreen.HEIGHT;
import static com.mikilangelo.abysmal.ui.screens.GameScreen.WIDTH;

import com.mikilangelo.abysmal.models.game.Ship;
import com.mikilangelo.abysmal.ui.gameElemets.Joystick;
import com.mikilangelo.abysmal.ui.gameElemets.Shooter;

public class TouchHandler {

  private static boolean hasCreatedJoystick = false;
  private static boolean hasCreatedShooter = false;

  public boolean createdJoystick = false;
  public boolean createdShooter = false;
  private final boolean turretsControl;
  private final Ship ship;

  public float touchX, touchY;

  public TouchHandler(Ship ship) {
    this.ship = ship;
    this.turretsControl = ship.turrets.size > 0;
    hasCreatedJoystick = hasCreatedShooter = false;
  }

  public boolean handleTouch(Joystick joystick, Shooter shooter, float delta) {
    if (touchY > HEIGHT / 3f && !createdShooter && !createdJoystick) {
      if (touchX < WIDTH / 3f) {
        if (!hasCreatedJoystick) {
          createdJoystick = hasCreatedJoystick = true;
          joystick.startTouch(touchX, HEIGHT - touchY);
        }
      } else if (touchX > 2 * WIDTH / 3f) {
        if (!hasCreatedShooter && turretsControl) {
          shooter.startTouch(touchX, HEIGHT - touchY);
          createdShooter = hasCreatedShooter = true;
        }
      }
    }
    if (createdShooter) {
      shooter.update(touchX, HEIGHT - touchY);
      for (short i = 0; i < ship.turrets.size; i++) {
        ship.turrets.get(i).control(shooter.getDirection());
      }
      ship.shot();
    } else if (!turretsControl && shooter.contains(touchX, HEIGHT - touchY)) {
      ship.shot();
    }
    if (createdJoystick) {
      joystick.update(touchX, HEIGHT - touchY);
      ship.control(joystick.getDirection(), joystick.getPower(), delta);
    }
    return createdJoystick || createdShooter;
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
