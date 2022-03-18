package com.mikilangelo.abysmal.screens.game.enemies;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.mikilangelo.abysmal.shared.repositories.TexturesRepository;
import com.mikilangelo.abysmal.screens.game.actors.ship.Ship;

public abstract class Enemy {
  public Ship ship;
  private boolean underPlayerFocus = false;
  private float focusStep = 0;
  private static float leftX, rightX;
  private static float topY, bottomY;
  private float frameWidth;
  private final float shipSize;
  private static Sprite border = null;

  public Enemy(Ship ship) {
    if (border == null) {
      border = new Sprite(TexturesRepository.get("UI/enemy_frame.png"));
    }
    this.ship = ship;
    shipSize = ship.def.shieldRadius;
  }

  public void draw(Batch batch, float delta, float cameraX, float cameraY, float w, float h) {
    if (ship.distance > w) {
      return;
    }
    ship.draw(batch, delta);
    if (underPlayerFocus) {
      if (focusStep < 1) {
        focusStep = focusStep * 0.9f + 0.1f;
        if (focusStep > 0.999) focusStep = 1;
        if (ship.distance > 200) {
          underPlayerFocus = false;
          ship.bodyData.underPlayerFocus = false;
          ship.shieldData.underPlayerFocus = false;
          return;
        }
      }
      leftX = (1 - focusStep) * (cameraX - w * 0.5f) + focusStep * (ship.x - shipSize);
      rightX = (1 - focusStep) * (cameraX + w * 0.5f) + focusStep * (ship.x + shipSize);
      bottomY = (1 - focusStep) * (cameraY - h * 0.5f) + focusStep * (ship.y - shipSize);
      topY = (1 - focusStep) * (cameraY + h * 0.5f) + focusStep * (ship.y + shipSize);
      frameWidth = (1 - focusStep) * w * 0.0075f + focusStep * 0.09f;
      drawFrame(batch, frameWidth, (0.4f + focusStep * 0.6f));
    } else {
      underPlayerFocus = ship.bodyData.underPlayerFocus || ship.shieldData.underPlayerFocus;
    }
  }

  private static void drawFrame(Batch batch, float width, float opacity) {
    final float cornerLength = (width * 11 + (topY - bottomY) * 0.6f) * 0.25f;
    final float halfL = cornerLength * 0.5f;
    final float w = width * 0.5f;
    border.setAlpha(opacity);

    border.setScale(width, cornerLength);
    border.setCenter(leftX - w, topY - halfL);
    border.draw(batch);
    border.setScale(cornerLength, width);
    border.setCenter(leftX + halfL, topY - w);
    border.draw(batch);

    border.setScale(width, cornerLength);
    border.setCenter(rightX - w, topY - halfL);
    border.draw(batch);
    border.setScale(cornerLength, width);
    border.setCenter(rightX - halfL, topY - w);;
    border.draw(batch);

    border.setScale(width, cornerLength);
    border.setCenter(leftX + w, bottomY + halfL);
    border.draw(batch);
    border.setScale(cornerLength, width);
    border.setCenter(leftX + halfL, bottomY + w);
    border.draw(batch);

    border.setScale(width, cornerLength);
    border.setCenter(rightX - w, bottomY + halfL);
    border.draw(batch);
    border.setScale(cornerLength, width);
    border.setCenter(rightX - halfL, bottomY + w);
    border.draw(batch);



//    batch.draw(border, leftX, topY - cornerLength, width, cornerLength);
//    batch.draw(border, leftX, topY - width, cornerLength, width);
//
//    batch.draw(border, rightX - width, topY - cornerLength, width, cornerLength);
//    batch.draw(border, rightX - cornerLength, topY - width, cornerLength, width);
//
//    batch.draw(border, leftX, bottomY, width, cornerLength);
//    batch.draw(border, leftX, bottomY, cornerLength, width);
//
//    batch.draw(border, rightX - width, bottomY, width, cornerLength);
//    batch.draw(border, rightX - cornerLength, bottomY, cornerLength, width);
  }
}
