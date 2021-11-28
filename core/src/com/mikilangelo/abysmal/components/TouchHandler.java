package com.mikilangelo.abysmal.components;

import static com.mikilangelo.abysmal.ui.screens.GameScreen.HEIGHT;
import static com.mikilangelo.abysmal.ui.screens.GameScreen.WIDTH;

import com.mikilangelo.abysmal.models.game.Ship;
import com.mikilangelo.abysmal.ui.Joystick;
import com.mikilangelo.abysmal.ui.Shooter;
import com.badlogic.gdx.graphics.g2d.Batch;

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

  public boolean handleTouch(Batch batch, Joystick joystick, Shooter shooter, float delta) {
    if (touchY > HEIGHT / 3f && !createdShooter && !createdJoystick) {
      if (touchX < WIDTH / 3f) {
        if (!hasCreatedJoystick) {
          createdJoystick = hasCreatedJoystick = true;
          joystick.x = touchX;
          joystick.y = HEIGHT - touchY;
        }
      } else if (touchX > 2 * WIDTH / 3f) {
        if (!hasCreatedShooter && turretsControl) {
          shooter.area.x = touchX;
          shooter.area.y = HEIGHT - touchY;
          createdShooter = hasCreatedShooter = true;
        }
      }
    }
    if (createdShooter) {
      shooter.draw(batch, touchX, HEIGHT - touchY);
      for (short i = 0; i < ship.turrets.size; i++) {
        ship.turrets.get(i).control(touchX, HEIGHT - touchY, shooter.area.x, shooter.area.y);
      }
      ship.shot();
    } else if (!turretsControl && shooter.area.contains(touchX, HEIGHT - touchY)) {
      ship.shot();
    }
    if (createdJoystick) {
      joystick.draw(batch, touchX, HEIGHT - touchY);
      ship.control(touchX, HEIGHT - touchY, joystick.x, joystick.y, delta);
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
