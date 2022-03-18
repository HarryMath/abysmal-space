package com.mikilangelo.abysmal.screens.game.actors.basic;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.Sprite;

public class DecorObject implements StaticObject {
  Sprite texture;
  float x;
  float y;
  float scale;
  float layer; // form 0 (at screen) to 1 (infinite far)
  float opacity; // form 1 to 0 (invisible)

  // natural sizes if screenPart is 0
  public DecorObject(
          String texturePath,
          float x,
          float y,
          float layer,
          float screenPart,
          float screenSize,
          float opacity,
          boolean withFilters
  ) {
    this.texture = new Sprite(new Texture(texturePath));
    if (withFilters) {
      this.texture.getTexture().setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
    }
    this.x = x;
    this.y = y;
    this.layer = layer;
    this.scale = screenSize * screenPart / this.texture.getHeight();
    this.opacity = opacity;
  }

  @Deprecated
  public void draw(Batch batch, float cameraX, float cameraY, float zoom) {
    float zoomCoefficient = (float) Math.pow(zoom, layer)/zoom;
//    texture.setCenter(
//            cameraX + (x + (cameraX - x) * layer - cameraX) * (float) Math.pow(zoom, layer),
//            cameraY + (y + (cameraY - y) * layer - cameraY) * (float) Math.pow(zoom, layer));
    texture.setCenter(
            x + (cameraX - x) * layer * zoomCoefficient,
            y + (cameraY - y) * layer * zoomCoefficient);
    texture.setScale(scale * zoomCoefficient * zoom);
    texture.draw(batch);
  }
}
