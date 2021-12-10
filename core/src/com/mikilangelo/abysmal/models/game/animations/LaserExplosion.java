package com.mikilangelo.abysmal.models.game.animations;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.Array;

public class LaserExplosion extends Explosion {

  public LaserExplosion(Array<Sprite> textures, float x, float y, float frameDuration) {
    super(textures, x, y, frameDuration);
  }

  @Override
  public void draw(Batch batch, float delta) {
    move(delta);
    if (!ended) {
      batch.setColor(0,1, 0, 1);
      textures.get(0).setCenter(x, y);
      textures.get(0).setAlpha(
              1f - Math.abs((textures.size - 1) / 2f - currentFrame + 1) /
                      textures.size * 2
      );
      textures.get(0).draw(batch);
      if (currentFrame > 1) {
        textures.get(currentFrame - 1).setCenter(x, y);
        textures.get(currentFrame - 1).setAlpha(0.6f);
        textures.get(currentFrame - 1).draw(batch);
      }
      if (currentFrame < textures.size) {
        textures.get(currentFrame).setCenter(x, y);
        textures.get(currentFrame).setAlpha(1);
        textures.get(currentFrame).draw(batch);
      }
      batch.setColor(1,1, 1, 1);
    }
  }
}
