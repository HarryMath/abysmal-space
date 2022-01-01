package com.mikilangelo.abysmal.tools;

import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.math.MathUtils;
import com.mikilangelo.abysmal.components.Settings;
import com.mikilangelo.abysmal.models.game.Ship;

public class Camera {

  public float zoom = 1;
  private final float maxZoom;

  public final OrthographicCamera camera;
  public float X = 0, Y = 0;
  private float shakeRotation = 0, cameraRotation = 0, shakePower = 0;
  private boolean isShaken = false, shakeDirect = false;
  public float screenCoefficient;
  public float initialZoomCoefficient;
  public float speedZoomCoefficient;
  private float cameraBiasX, cameraBiasY;

  public Camera(int h, int w, float maxZoom) {
    camera = new OrthographicCamera();
    resize(h, w);
    initialZoomCoefficient = 1f;
    speedZoomCoefficient = 1.5f;
    camera.zoom = initialZoomCoefficient * speedZoomCoefficient;
    this.maxZoom = maxZoom;
  }

  public void shake(float power) {
    isShaken = true;
    shakePower = power;
    shakeDirect = !shakeDirect;
  }

  public void update(Ship ship, Batch objectsBatch, Batch backgroundBatch, Batch shaderBatch) {
    if (isShaken) {
      if (Math.abs(shakeRotation) > shakePower) {
        shakeDirect = !shakeDirect;
        shakePower = shakePower * 0.9f - 0.05f;
        shakeRotation = shakeRotation > 0 ? shakePower : -shakePower;
        System.out.println(shakePower);
      }
      if (shakePower <= 0f || (shakePower < 0.1f && cameraRotation <= shakePower)) {
        System.out.println(cameraRotation);
//        camera.rotate(-Geometry.defineAngle(camera.up.x, camera.up.y, -cameraRotation));
        camera.rotate(-cameraRotation);
        isShaken = false;
      } else {
        shakeRotation += shakeDirect ? (0.13f + shakePower * 0.333f) : -(0.13f + shakePower * 0.333f);
        camera.rotate(shakeRotation - cameraRotation);
        cameraRotation = shakeRotation;
      }
    }
    speedZoomCoefficient = (ship.body.getLinearVelocity().len() * 0.06f + speedZoomCoefficient * 39f) / 40f;
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
