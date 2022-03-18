package com.mikilangelo.abysmal.screens.game.actors.decor;

import com.mikilangelo.abysmal.shared.repositories.TexturesRepository;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.utils.Array;

public class AnimatedPlanet extends Planet {

  final Array<Sprite> textures;
  private Sprite previousFrame;

  private float counter = 0;
  private short currentFrame = 0;

  public AnimatedPlanet(String name, float size, float x, float y, float layer, int frames) {
    super(name + "/1", size, x, y, layer);
    textures = new Array<>();
    for (int i = 1; i <= frames; i++) {
      textures.add(new Sprite(TexturesRepository.get("planets/" + name + "/" + i + ".png")));
    }
    this.texture = textures.get(0);
    this.previousFrame = textures.get(textures.size - 1);
  }

  protected void move() {
    counter += 0.05f;
    if (counter > 1) {
      counter = 0;
      currentFrame += 1;
      if (currentFrame >= textures.size) {
        currentFrame = 0;
      }
      previousFrame = texture;
      texture = textures.get(currentFrame);
    }
  }

  @Override
  public void draw(Batch batch, float cameraX, float cameraY, float zoom) {
    previousFrame.setAlpha(1);
    previousFrame.setCenter(
            cameraX + (x + (cameraX - x) * layer - cameraX) * (float) Math.pow(zoom, layer),
            cameraY + (y + (cameraY - y) * layer - cameraY) * (float) Math.pow(zoom, layer));
    previousFrame.setScale(scale * (float) Math.pow(zoom, layer));
    previousFrame.draw(batch);
    this.texture.setAlpha(counter);
    super.draw(batch, cameraX, cameraY, zoom);
    move();
  }
}
