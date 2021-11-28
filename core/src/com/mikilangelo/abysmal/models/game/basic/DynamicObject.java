package com.mikilangelo.abysmal.models.game.basic;

import com.badlogic.gdx.graphics.g2d.Batch;

public interface DynamicObject {

  void move(float delta);

  void draw(Batch batch);
}
