package com.mikilangelo.abysmal.screens.game.actors.decor;

import com.mikilangelo.abysmal.shared.repositories.TexturesRepository;
import com.mikilangelo.abysmal.screens.game.actors.basic.StaticObject;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.Sprite;

public class Planet implements StaticObject {

  protected Sprite texture;
  final float x, y;
  final float scale;
  final float layer;


  public Planet(String name, float size, float x, float y, float layer) {
    this.texture = new Sprite(TexturesRepository.get("planets/" + name + ".png"));
    this.scale = size / this.texture.getHeight();
    this.layer = layer;
    this.x = x;
    this.y = y;
  }

  @Override
  public void draw(Batch batch, float cameraX, float cameraY, float zoom) {
    texture.setCenter(
            cameraX + (x + (cameraX - x) * layer - cameraX) * (float) Math.pow(zoom, layer),
            cameraY + (y + (cameraY - y) * layer - cameraY) * (float) Math.pow(zoom, layer));
    texture.setScale(scale * (float) Math.pow(zoom, layer));
    texture.draw(batch);
  }
}
