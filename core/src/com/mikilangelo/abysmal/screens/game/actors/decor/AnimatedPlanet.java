package com.mikilangelo.abysmal.screens.game.actors.decor;

import com.mikilangelo.abysmal.shared.repositories.TexturesRepository;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.utils.Array;

public class AnimatedPlanet extends Planet {

  final Array<Sprite> textures;

  private float counter = 0;
  private short currentFrame = 0;
  private final float speed;

  public AnimatedPlanet(Array<Sprite> frames, float size, float x, float y, float layer) {
    this(frames, size, x, y, layer, 0.17f);
  }

  public AnimatedPlanet(Array<Sprite> frames, float size, float x, float y, float layer, float speed) {
    super(frames.get(0), size, x, y, layer);
    textures = frames;
    this.texture = textures.get(0);
    this.speed = speed;
  }

  protected void move() {
    counter += speed;
    if (counter > 1) {
      counter = 0;
      currentFrame += 1;
      if (currentFrame >= textures.size) {
        currentFrame = 0;
      }
      texture = textures.get(currentFrame);
    }
  }

  @Override
  public void draw(Batch batch, float cameraX, float cameraY, float zoom) {
    super.draw(batch, cameraX, cameraY, zoom);
    move();
  }
}
