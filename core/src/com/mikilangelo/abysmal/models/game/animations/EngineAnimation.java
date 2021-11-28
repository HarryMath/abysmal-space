package com.mikilangelo.abysmal.models.game.animations;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Array;

public class EngineAnimation {

  private final Array<Sprite> textures;
  private final float frequency;
  private float counter = 0;
  private byte current = 0;

  public EngineAnimation(Array<Sprite> textures, float frameFrequency) {
    this.frequency = frameFrequency;
    this.textures = textures;
  }

  public void draw(Batch batch, float delta, float x, float y, float angle) {
    counter += delta;
    if (counter >= frequency) {
      counter = 0;
      if (current < textures.size - 1) {
        current++;
      } else {
        current = 0;
      }
    }
    if (textures.size > 0) {
      int previous = current > 0 ? current - 1 : textures.size - 1;
      textures.get(previous).setRotation(angle * MathUtils.radiansToDegrees);
      textures.get(previous).setCenter(x, y);
      textures.get(previous).setAlpha(0.4f);
      textures.get(previous).draw(batch);
      textures.get(current).setRotation(angle * MathUtils.radiansToDegrees);
      textures.get(current).setCenter(x, y);
      textures.get(current).setAlpha(0.8f);
      textures.get(current).draw(batch);
    }
  }
}
