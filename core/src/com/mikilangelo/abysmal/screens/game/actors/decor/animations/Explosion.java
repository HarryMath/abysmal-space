package com.mikilangelo.abysmal.screens.game.actors.decor.animations;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.utils.Array;

public class Explosion {
  protected final Array<Sprite> textures;
  float counter = 0;
  public boolean ended = false;
  final float frameDuration;
  short currentFrame = 0;
  float x;
  float y;

  public Explosion(Array<Sprite> textures, float x, float y, float frameDuration) {
    this.textures = textures;
    this.x = x; this.y = y;
    this.frameDuration = frameDuration;
  }

  protected void move(float delta) {
    counter += delta;
    if (counter > frameDuration) {
      counter = 0;
      currentFrame += 1;
      if (currentFrame > textures.size) {
        this.ended = true;
      }
    }
  }

  public void draw(Batch batch, float delta) {
    move(delta);
    if (!ended) {
      if (currentFrame < textures.size) {
        textures.get(currentFrame).setCenter(x, y);
        textures.get(currentFrame).setAlpha(1);
        textures.get(currentFrame).draw(batch);
      }
    }
  }
}
