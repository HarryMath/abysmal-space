package com.mikilangelo.abysmal.components;

import com.badlogic.gdx.input.GestureDetector;
import com.badlogic.gdx.math.Vector2;
import com.mikilangelo.abysmal.ui.screens.GameScreen;

public class GameController implements GestureDetector.GestureListener {
  private static boolean shot = false;
  private final float maxZoom;

  public GameController(float maxZoom) {
    this.maxZoom = maxZoom;
  }

  @Override
  public boolean touchDown(float x, float y, int pointer, int button) {
    return false;
  }

  @Override
  public boolean tap(float x, float y, int count, int button) {
    return false;
  }

  @Override
  public boolean longPress(float x, float y) {
    return false;
  }

  @Override
  public boolean fling(float velocityX, float velocityY, int button) {
    return false;
  }

  @Override
  public boolean pan(float x, float y, float deltaX, float deltaY) {
    return false;
  }

  @Override
  public boolean panStop(float x, float y, int pointer, int button) {
    return false;
  }

  @Override
  public boolean zoom(float initialDistance, float distance) {
    if (!GameScreen.underControl) {
      if (initialDistance > distance && GameScreen.initialZoomCoefficient < maxZoom) {
        GameScreen.initialZoomCoefficient += 0.01;
      } else if (initialDistance < distance && GameScreen.initialZoomCoefficient > 0.4f) {
        GameScreen.initialZoomCoefficient -= 0.01;
      }
    }
    return false;
  }

  @Override
  public boolean pinch(Vector2 initialPointer1, Vector2 initialPointer2, Vector2 pointer1, Vector2 pointer2) {
    return false;
  }

  @Override
  public void pinchStop() {
  }
}
