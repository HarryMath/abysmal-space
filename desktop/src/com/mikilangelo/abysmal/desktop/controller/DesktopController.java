package com.mikilangelo.abysmal.desktop.controller;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.graphics.Cursor;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.input.GestureDetector;
import com.mikilangelo.abysmal.screens.game.actors.ship.PlayerShip;
import com.mikilangelo.abysmal.screens.game.components.Camera;
import com.mikilangelo.abysmal.screens.game.controllers.GameController;
import com.mikilangelo.abysmal.shared.tools.CalculateUtils;
import com.mikilangelo.abysmal.shared.repositories.TexturesRepository;

public class DesktopController implements GameController {
  private float rotationSensitivity = 0.2f;
  private boolean hasTurrets = false;
  private boolean shipRotated = false;
  private GestureDetector gestureDetector = null;
  private Cursor customCursor;
  private float cameraAngle = 0;
  private float w, h;
  private PlayerShip ship;

  public void setCustomCursor() {
    Texture texture = TexturesRepository.get("cursor.png");
    texture.getTextureData().prepare();
    Pixmap pixmap = texture.getTextureData().consumePixmap();
    int xHotspot = pixmap.getWidth() / 2;
    int yHotspot = pixmap.getHeight() / 2;
    this.customCursor = Gdx.graphics.newCursor(pixmap, xHotspot, yHotspot);
    Gdx.graphics.setCursor(customCursor);
    pixmap.dispose();
  }

  @Override
  public void init(PlayerShip ship, float w, float h) {
    this.ship = ship;
    this.hasTurrets = ship.turrets.size > 0;
    this.resizeComponents(w, h);
    this.gestureDetector = new DesktopGestureDetector();
    setCustomCursor();
    // TODO shotIndicator =
    // TODO shieldIndicator =
    // TODO speedUpIndicator =
  }

  @Override
  public boolean process(PlayerShip ship, Camera camera, float delta) {
    // long currentTime = System.currentTimeMillis();
    //TODO shotIndicator.update(ship.getShieldAbilityReloadTime(currentTime));
    //TODO shieldIndicator.update(ship.getShieldAbilityReloadTime(currentTime));
    //TODO speedUpIndicator.update(ship.getShieldAbilityReloadTime(currentTime));
    cameraAngle = camera.getRotation();
    if (Gdx.input.isTouched()) {
      shot(ship, Gdx.input.getX(), Gdx.input.getY());
    }
    ship.newAngle = ship.angle;
    shipRotated = false;
    if (Gdx.input.isKeyPressed(Input.Keys.W) || Gdx.input.isKeyPressed(Input.Keys.UP)) {
      ship.applyImpulse(1, true);
    }
    if (Gdx.input.isKeyPressed(Input.Keys.S) || Gdx.input.isKeyPressed(Input.Keys.DOWN)) {
      ship.applyImpulse(-0.01f, false);
    }
    if (Gdx.input.isKeyPressed(Input.Keys.A) || Gdx.input.isKeyPressed(Input.Keys.LEFT)) {
      ship.rotate(rotationSensitivity, delta);
      shipRotated = true;
    }
    if (Gdx.input.isKeyPressed(Input.Keys.D) || Gdx.input.isKeyPressed(Input.Keys.RIGHT)) {
      ship.rotate(-rotationSensitivity, delta);
      shipRotated = true;
    }
    if (Gdx.input.isKeyPressed(Input.Keys.SPACE)) {
      camera.zoomOut();
    }
    if (Gdx.input.isKeyPressed(Input.Keys.Z)) {
      camera.zoomIn();
    }
    if (Gdx.input.isKeyPressed(Input.Keys.E)) {
      ship.activateShield();
    }
    if (Gdx.input.isKeyPressed(Input.Keys.Q)) {
      ship.speedUp();
    }
    if (shipRotated) {
      rotationSensitivity = (rotationSensitivity * 0.89f + 0.071f);
    } else {
      rotationSensitivity = 0.2f;
    }
    return false;
  }

  private void shot(PlayerShip ship, float touchX, float touchY) {
    if (!hasTurrets) {
      ship.shotDirectly();
      return;
    }
    final float angle = CalculateUtils.normalizeAngle(
            CalculateUtils.defineAngle(touchX - w * 0.5f, h * 0.5f - touchY, 0)
            + cameraAngle
    );
    ship.turrets.forEach(turret -> {
      turret.control(angle);
      if (Math.abs((turret.angle + ship.angle) % 6.28f - angle) < 0.5f) {
        turret.shot(ship, 1, 0);
      }
    });
    if (Math.abs(angle - ship.angle) < 0.1f) {
      ship.shotDirectly();
    }
  }

  @Override
  public void drawInterface(Batch batch, BitmapFont font) {
    // TODO draw indicators
  }

  @Override
  public void resizeComponents(float w, float h) {
    this.w = w;
    this.h = h;
    // TODO resize indicators
  }

  @Override
  public InputProcessor getGestureListener() {
    return gestureDetector;
  }

  @Override
  public void dispose() {
    Gdx.graphics.setSystemCursor(Cursor.SystemCursor.Arrow);
    // TODO set indicators null
  }

  private class DesktopGestureDetector extends GestureDetector {

    public DesktopGestureDetector() {
      super(new GestureAdapter());
    }

    @Override
    public boolean mouseMoved(int x, int y) {
      if (!hasTurrets) {
        return false;
      }
      final float angle = CalculateUtils.defineAngle(x - w * 0.5f, h * 0.5f - y, 0) + cameraAngle;
      ship.turrets.forEach(turret -> {
        turret.control(CalculateUtils.normalizeAngle(angle));
      });
      return false;
    }
  }
}
