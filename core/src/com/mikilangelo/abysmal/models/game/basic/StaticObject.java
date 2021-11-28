package com.mikilangelo.abysmal.models.game.basic;

import com.badlogic.gdx.graphics.g2d.Batch;

public interface StaticObject {

  void draw(Batch batch, float cameraX, float cameraY, float zoom);
}
