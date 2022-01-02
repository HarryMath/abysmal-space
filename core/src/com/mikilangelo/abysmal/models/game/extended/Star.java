package com.mikilangelo.abysmal.models.game.extended;

import com.mikilangelo.abysmal.components.Settings;
import com.mikilangelo.abysmal.components.repositories.TexturesRepository;
import com.mikilangelo.abysmal.models.game.basic.StaticObject;
import com.mikilangelo.abysmal.ui.screens.GameScreen;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.math.MathUtils;

public class Star implements StaticObject {
  private static Sprite texture;
  private static boolean isInitialised;
  float x, rangeX;
  float y, rangeY;
  float scale;
  float layer; // form 0 (at screen) to 1 (infinite far)
  float opacity; // form 1 to 0 (invisible)

  public Star(float layer, float opacity) {
    if (Settings.cameraRotation) {
      this.x = MathUtils.random(0, GameScreen.SCREEN_WIDTH * 1.41f * (1.9f - layer)) / (1.000001f - layer);
      this.y = MathUtils.random(0, GameScreen.SCREEN_WIDTH * 1.41f * (1.9f - layer)) / (1.000001f - layer);
      this.rangeX = this.rangeY = GameScreen.SCREEN_WIDTH * 1.41f * (1.9f - layer);
    } else {
      this.x = MathUtils.random(0, GameScreen.SCREEN_WIDTH * (1.9f - layer)) / (1.000001f - layer);
      this.y = MathUtils.random(0, GameScreen.SCREEN_HEIGHT * (1.9f - layer)) / (1.000001f - layer);
      this.rangeX = GameScreen.SCREEN_WIDTH * (1.9f - layer);
      this.rangeY = GameScreen.SCREEN_HEIGHT * (1.9f - layer);
    }
    this.layer = layer;
    this.opacity = opacity;
    this.scale = GameScreen.SCREEN_HEIGHT * 0.028f * (1.03f - (float) Math.pow(layer, 0.31f));
    if (layer <= 0.2f) {
      this.rangeX *= 2;
      this.rangeY *= 2;
      this.x *= 2;
      this.y *= 2;
    }
  }

  public static void initTexture() {
    texture = new Sprite(TexturesRepository.get("star.png"));
    isInitialised = true;
  }

  public static void dispose() {
    if (isInitialised) {
      texture.getTexture().dispose();
      texture = null;
      isInitialised = false;
    }
  }

  private static float mod(float pos, float max) {
    return pos > 0 ? (pos % max - max / 2) : (max / 2 + pos % max);
  }

  @Override
  public void draw(Batch batch, float cameraX, float cameraY, float zoom) {
    if (isInitialised) {
      texture.setCenter(
              cameraX + mod(x + (cameraX - x) * layer - cameraX, rangeX) * (float) Math.pow(zoom, layer),
              cameraY + mod(y + (cameraY - y) * layer - cameraY, rangeY) * (float) Math.pow(zoom, layer));
      texture.setScale(scale * (float) Math.pow(zoom, layer));
      texture.setAlpha(opacity);
      texture.draw(batch);
    }
  }
}
