package com.mikilangelo.abysmal.components.repositories;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.utils.Array;

import java.util.HashMap;
import java.util.Map;

public abstract class TexturesRepository {

  private static final Array<Texture> textures = new Array<>();
  private static final Map<String, Integer> paths = new HashMap<>();

  private static void loadTextures(String prefix, Array<String> pathsList, String postfix) {
    for (String path: pathsList) {
      loadTexture(prefix + path + postfix);
    }
  }

  private static void loadTextures(String prefix, String[] pathsList, String postfix) {
    loadTextures(prefix, new Array<>(pathsList), postfix);
  }

  private static void loadTexture(String path) {
    paths.put(path, textures.size);
    textures.add(new Texture(path));
  }

  public static void init() {
    loadTextures("UI/", new String[]{
            "circle", "controller", "radar", "logo", "radar_asteroid", "radar_center", "radar_enem",
            "radar_scanner", "shot", "arrow", "health/leftBar", "health/rightBar", "health/full",
            "health/blizzard", "health/center", "health/gray", "health/white"},
            ".png");
    loadTextures("", new String[]{"back", "backMenu", "starMenu"}, ".png");
    loadTextures("planets/", new String[]{
            "Baren", "Eros", "Gelios", "Ice", "mud", "Tatuin", "moon", "Terra", "Vulcano"}, ".png");
    loadTextures("planets/nebula/", range(1, 61), ".png");
    loadTextures("things/spawn/", range(24), ".png");
    loadTextures("things/", new String[] {"shield", "shieldTouch"}, ".png");
    loadTextures("ships/defender/", new String[] {
            "body", "kak0", "kak1", "laser", "engine0", "engine1", "engine2", "engine3"}, ".png");
    loadTextures("ships/defender/explosions/", range(11), ".png");
    loadTextures("ships/hyperion/", new String[] {
            "body", "laser", "engine0", "engine1", "engine2", "turret", "turretSmall"}, ".png");
    loadTextures("ships/invader/", new String[] {"body", "kak", "laser"}, ".png");
    loadTextures("ships/hyperion/explosions/", range(11), ".png");
    loadTextures("explosions/ship/", range(13), ".png");
    loadTextures("explosions/green/FX_6_", range(7), ".png");
    loadTextures("explosions/stone/", range(4), ".png");
    loadTextures("explosions/asteroid/", range(1, 7), ".png");
    loadTextures("asteroids/small/", range(5), "/texture.png");
    loadTextures("asteroids/medium/", range(1), "/texture.png");
    loadTextures("asteroids/big/", range(11), "/texture.png");
  }

  private static String[] range(int start, int end) {
    String[] r = new String[end - start];
    for (int i = start; i < end; i++) {
      r[i - start] = String.valueOf(i);
    }
    return r;
  }

  private static String[] range(int end) {
    return range(0, end);
  }

  public static Texture get(String path) {
    return textures.get(paths.get(path));
  }

  public static void disposeAll() {
    for (Texture t: textures) {
      t.dispose();
    }
    textures.clear();
    paths.clear();
  }

}
