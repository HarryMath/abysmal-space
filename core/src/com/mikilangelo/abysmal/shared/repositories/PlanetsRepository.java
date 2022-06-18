package com.mikilangelo.abysmal.shared.repositories;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.utils.Array;
import com.mikilangelo.abysmal.shared.tools.PlanetsGenerator;

import java.util.HashMap;
import java.util.Map;

public abstract class PlanetsRepository {

  private static final Map<String, Array<Sprite>> planets = new HashMap<>();

  public static void init() {
    // load("venus", 45, 150);
    // load("test", 45, 150);
    // load("test1", 45, 200);
  }

  public static Array<Sprite> get(String name) {
    return planets.get(name);
  }

  private static void load(String name, int size, int frames) {
    Texture t = TexturesRepository.get("planets/" + name + ".png");
    planets.put(
            name,
            PlanetsGenerator.createPlanet(t, size, frames)
    );
  }
}
