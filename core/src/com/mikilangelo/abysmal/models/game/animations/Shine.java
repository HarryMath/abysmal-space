package com.mikilangelo.abysmal.models.game.animations;

import com.mikilangelo.abysmal.ui.screens.GameScreen;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.utils.Array;

public class Shine {
  private static final float INITIAL_SIZE = 0.11f;
  private static final float INITIAL_LAYER = 0.991f;
  Array<Sprite> textures = new Array<>();
  float counter = 0;
  boolean direct = true;
  int current = 0;
  float x;
  float y;
  float scale;

  public Shine(float x, float y) {
    for (int i = -8; i < 11; i++) {
      textures.add(new Sprite(new Texture("things/shine/" + i + ".png")));
    }
    this.x = x;
    this.y = y;
    this.scale = GameScreen.SCREEN_HEIGHT * INITIAL_SIZE / this.textures.get(0).getHeight();
  }

  public void draw(Batch batch, float delta, float cameraX, float cameraY, float zoom) {
    counter += delta;
    if (counter >= 0.08f) {
      nextFrame();
    }
    prepareTextures(cameraX, cameraY, zoom);
    textures.get(current).draw(batch);
  }

  private void nextFrame() {
    counter = 0;
    if (direct) {
      if (current < textures.size - 1) {
        current++;
      } else {
        current--;
        direct = false;
      }
    } else {
      if (current > 1) {
        current--;
      } else {
        current++;
        direct = true;
      }
    }
  }

  private void prepareTextures(float cameraX, float cameraY, float zoom) {
    textures.get(current).setScale(scale * (float) Math.pow(zoom, INITIAL_LAYER));
    float centerX = cameraX + (x + (cameraX - x) * INITIAL_LAYER - cameraX) * (float) Math.pow(zoom, INITIAL_LAYER);
    float centerY = cameraY + (y + (cameraY - y) * INITIAL_LAYER - cameraY) * (float) Math.pow(zoom, INITIAL_LAYER);
    textures.get(current).setCenter(centerX, centerY);
  }
}
