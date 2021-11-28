package com.mikilangelo.abysmal.models.game.animations;

import com.mikilangelo.abysmal.components.repositories.TexturesRepository;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.utils.Array;

public class Portal {
  private static final float INITIAL_SIZE = 0.4f;
  private static final float INITIAL_LAYER = 0.5f;
  Array<Sprite> textures = new Array<>();
  float counter = 0;
  byte prePreCurrent = 1;
  byte preCurrent = 1;
  byte current = 1;
  final float x;
  final float y;
  float scale;

  public Portal(float x, float y, float screenHeight) {
    for (int i = 0; i < 24; i++) {
      textures.add(new Sprite(TexturesRepository.get("things/spawn/" + i + ".png")));
    }
    this.x = x;
    this.y = y;
    this.scale = screenHeight * INITIAL_SIZE / this.textures.get(0).getHeight();
  }

  public void draw(Batch batch, float delta, float cameraX, float cameraY, float zoom) {
    counter += delta;
    if (counter >= 0.07f) {
      nextFrame();
    }
    prepareTextures(cameraX, cameraY, zoom);

    textures.get(prePreCurrent).draw(batch);
    textures.get(preCurrent).draw(batch);
    textures.get(current).draw(batch);
    textures.get(0).draw(batch);
  }

  private void nextFrame() {
    counter = 0;
    prePreCurrent = preCurrent;
    preCurrent = current;
    current %= (this.textures.size - 1);
    preCurrent %= (this.textures.size - 1);
    prePreCurrent %= (this.textures.size - 1);
    current++;
    if (prePreCurrent < 1) {
      prePreCurrent = 1;
    }
    if (preCurrent < 1) {
      preCurrent = 1;
    }
  }

  private void prepareTextures(float cameraX, float cameraY, float zoom) {
    textures.get(0).setScale(scale * (float) Math.pow(zoom, INITIAL_LAYER));
    textures.get(current).setScale(scale * (float) Math.pow(zoom, INITIAL_LAYER));
    textures.get(preCurrent).setScale(scale * (float) Math.pow(zoom, INITIAL_LAYER));
    textures.get(prePreCurrent).setScale(scale * (float) Math.pow(zoom, INITIAL_LAYER));
    float centerX = cameraX + (x + (cameraX - x) * INITIAL_LAYER - cameraX) * (float) Math.pow(zoom, INITIAL_LAYER);
    float centerY = cameraY + (y + (cameraY - y) * INITIAL_LAYER - cameraY) * (float) Math.pow(zoom, INITIAL_LAYER);
    textures.get(0).setCenter(centerX, centerY);
    textures.get(current).setCenter(centerX, centerY);
    textures.get(preCurrent).setCenter(centerX, centerY);
    textures.get(prePreCurrent).setCenter(centerX, centerY);
    textures.get(preCurrent).setAlpha(0.6f);
    textures.get(prePreCurrent).setAlpha(0.3f);
    textures.get(current).setAlpha(1);
  }
}
