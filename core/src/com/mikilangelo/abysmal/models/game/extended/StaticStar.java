package com.mikilangelo.abysmal.models.game.extended;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.math.MathUtils;
import com.mikilangelo.abysmal.components.Settings;
import com.mikilangelo.abysmal.ui.screens.GameScreen;

public class StaticStar extends Star {
  private float rangeX, rangeY;

  public StaticStar(float layer, float opacity) {
    super(layer, opacity,
            Settings.cameraRotation ?
                    MathUtils.random(0, GameScreen.SCREEN_WIDTH * 1.41f * (1.9f - layer)) / (1.000001f - layer) :
                    MathUtils.random(0, GameScreen.SCREEN_WIDTH * (1.9f - layer)) / (1.000001f - layer),
            Settings.cameraRotation ?
                    MathUtils.random(0, GameScreen.SCREEN_WIDTH * 1.41f * (1.9f - layer)) / (1.000001f - layer) :
                    MathUtils.random(0, GameScreen.SCREEN_HEIGHT * (1.9f - layer)) / (1.000001f - layer)
    );
    if (Settings.cameraRotation) {
      this.rangeX = this.rangeY = GameScreen.SCREEN_WIDTH * 1.41f * (1.9f - layer);
    } else {
      this.rangeX = GameScreen.SCREEN_WIDTH * (1.9f - layer);
      this.rangeY = GameScreen.SCREEN_HEIGHT * (1.9f - layer);
    }
    if (layer <= 0.2f) {
      this.rangeX *= 2;
      this.rangeY *= 2;
      this.x *= 2;
      this.y *= 2;
    }
  }

  private static float mod(float pos, float max) {
    return pos > 0 ? (pos % max - max / 2) : (max / 2 + pos % max);
  }

  @Override
  public void draw(Batch batch, float cameraX, float cameraY, float zoom) {
    texture.setCenter(
            cameraX + mod(x + (cameraX - x) * layer - cameraX, rangeX) * (float) Math.pow(zoom, layer),
            cameraY + mod(y + (cameraY - y) * layer - cameraY, rangeY) * (float) Math.pow(zoom, layer));
    texture.setScale(scale * (float) Math.pow(zoom, layer));
    texture.setAlpha(opacity);
    texture.draw(batch);
  }
}
