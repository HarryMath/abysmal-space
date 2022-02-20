package com.mikilangelo.abysmal.models.game.extended;

import com.mikilangelo.abysmal.components.repositories.TexturesRepository;
import com.mikilangelo.abysmal.models.game.basic.StaticObject;
import com.mikilangelo.abysmal.ui.screens.GameScreen;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.Sprite;

public class Star implements StaticObject {
  protected static Sprite texture;
  protected static boolean isInitialised;
  protected float x, y;
  float scale;
  public float layer; // form 0 (at screen) to 1 (infinite far)
  float opacity; // form 1 to 0 (invisible)

  public Star(float layer, float opacity, float x, float y) {
    this.x = x;
    this.y = y;
    this.layer = layer;
    this.opacity = opacity;
    this.scale = GameScreen.SCREEN_HEIGHT * 0.028f * (1.03f - (float) Math.pow(layer, 0.31f));
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
    texture.setCenter(
            cameraX + (x + (cameraX - x) * layer - cameraX) * (float) Math.pow(zoom, layer),
            cameraY + (y + (cameraY - y) * layer - cameraY) * (float) Math.pow(zoom, layer));
    texture.setScale(scale * (float) Math.pow(zoom, layer));
    texture.setAlpha(opacity);
    texture.draw(batch);
  }
}
