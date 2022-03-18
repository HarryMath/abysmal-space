package com.mikilangelo.abysmal.screens.game.actors.basic;

import com.badlogic.gdx.graphics.g2d.Batch;

public interface DynamicObject {

  void move(float delta);

  void draw(Batch batch);
}
