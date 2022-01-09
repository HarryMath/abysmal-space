package com.mikilangelo.abysmal.tools;

import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.math.MathUtils;
import com.mikilangelo.abysmal.components.Settings;
import com.mikilangelo.abysmal.models.game.Ship;

public class Camera {

  public float zoom = 1;
  private final float maxZoom;
  private final float minZoom;

  private final OrthographicCamera camera;
  public float X = 0, Y = 0;
  private float shakeRotation = 0, shakePower = 0;
  private boolean isShaken = false, shakeDirect = false;
  public float screenCoefficient;
  public float initialZoomCoefficient;
  public float speedZoomCoefficient;
  private float cameraBiasX, cameraBiasY;

  public Camera(int h, int w, float minZoom, float maxZoom) {
    camera = new OrthographicCamera();
    resize(h, w);
    initialZoomCoefficient = (1 + minZoom + maxZoom) / 3f;
    speedZoomCoefficient = 1.5f;
    camera.zoom = initialZoomCoefficient * speedZoomCoefficient;
    this.minZoom = minZoom;
    this.maxZoom = maxZoom;
  }

  public void shake(float power) {
    isShaken = true;
    shakePower = power * MathUtils.degreesToRadians;
    shakeDirect = !shakeDirect;
  }

  public void update(Ship ship, Batch objectsBatch, Batch backgroundBatch, Batch shaderBatch) {
    if (isShaken) {
      if (Math.abs(shakeRotation) > shakePower) {
        shakeDirect = !shakeDirect;
        shakePower = shakePower * 0.9f - 0.003f;
        shakeRotation = shakeRotation > 0 ? shakePower : -shakePower;
      }
      if (shakePower <= 0f || (shakePower < 0.006f && shakeRotation <= shakePower)) {
        if (Settings.cameraRotation) {
          camera.up.x = MathUtils.cos(ship.angle);
          camera.up.y = MathUtils.sin(ship.angle);
        } else {
          camera.up.x = 0;
          camera.up.y = 1;
        }
        isShaken = false;
      } else {
        shakeRotation += shakeDirect ? (0.01f + shakePower * 0.333f) : -(0.01f + shakePower * 0.333f);
        if (Settings.cameraRotation) {
          camera.up.x = MathUtils.cos(ship.angle + shakeRotation);
          camera.up.y = MathUtils.sin(ship.angle +shakeRotation);
        } else {
          camera.up.x = MathUtils.sin(shakeRotation);
          camera.up.y = MathUtils.cos(shakeRotation);
        }
      }
    } else if (Settings.cameraRotation) {
      camera.up.x = MathUtils.cos(ship.angle);
      camera.up.y = MathUtils.sin(ship.angle);
    }
    speedZoomCoefficient = (ship.speed * 0.06f + speedZoomCoefficient * 39f) / 40f;
    if (Settings.fixedPosition) {
      cameraBiasX = cameraBiasY = 0;
    } else {
      cameraBiasX = (cameraBiasX * 87 + MathUtils.cos(ship.newAngle) * speedZoomCoefficient * 7) / 88f;
      cameraBiasY = (cameraBiasY * 87 + MathUtils.sin(ship.newAngle) * speedZoomCoefficient * 7) / 88f;
    }
    X = ship.x + cameraBiasX;
    Y = ship.y + cameraBiasY;
    camera.position.set(X, Y, 0);
    camera.zoom = zoom = (speedZoomCoefficient + 1) * (initialZoomCoefficient + 1) * 0.5f - 0.5f;
    camera.update();
    objectsBatch.setProjectionMatrix(camera.combined);
    backgroundBatch.setProjectionMatrix(camera.combined);
    if (Settings.drawBackground) {
      backgroundBatch.setProjectionMatrix(camera.combined);
      if (Settings.showBlackHoles) {
        shaderBatch.setProjectionMatrix(camera.combined);
      }
    }
  }

  public void zoomOut() {
    if (initialZoomCoefficient < maxZoom) {
      initialZoomCoefficient += 0.01;
    }
  }

  public void zoomIn() {
    if (initialZoomCoefficient > 0.4) {
      initialZoomCoefficient -= 0.01;
    }
  }

  public void resize(int height, int width) {
    screenCoefficient = height / 25f;
    camera.setToOrtho(false, 25f * width / height, 25f);
  }
}
