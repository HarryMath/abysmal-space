package com.mikilangelo.abysmal.shared.repositories;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.math.MathUtils;
import com.mikilangelo.abysmal.screens.game.actors.decor.Star;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Random;


public abstract class StarsRepository {
  public static final List<Star> stars = new ArrayList<>();
  private static final Random random = new Random();

  public static void generateGalaxy(float x, float y) {
    final int raysAmount = 3; // MathUtils.random(2, 4);
    final float layer = 0.96f; // normalRandom(0.88f, 0.95f);
    final float size = (float) Math.sqrt(raysAmount) * 17; // normalRandom(15, 20);
    final float step = 0.008f * (float) Math.sqrt(raysAmount);
    final float tiltAngle = normalRandom(0, MathUtils.PI2);
    final float scaleY = 0.6f; // normalRandom(0.5f, 0.9f);
    float radius, startAngle, spread;
    for (int ray = 0; ray < raysAmount; ray++) {
      startAngle = ray * MathUtils.PI2 / raysAmount;
      for (float angle = 0.07f; angle <= 5.5f; angle += step) {
        radius = 0.01f * (
                (float) Math.pow(angle, 4) * size + size * normalRandom(-0.1f, 0.1f)
        );
        spread = (float) Math.pow((5.6f - angle) / 6.3f, 1.1f) * size * 0.3f;
        stars.add(new Star(
                layer + normalRandom(-0.01f, 0.002f) / (1.1f + (float) Math.sqrt(angle) * 1.1f),
                0.1f + (normalRandom(0.2f, 0.8f)) / (1.1f + angle * 1.5f),
                x + radius * MathUtils.cos(startAngle + angle + tiltAngle)
                        + spread * normalRandom(-1f, 1f),
                y + radius * scaleY * MathUtils.sin(startAngle + angle + tiltAngle)
                        + spread * normalRandom(-1f, 1f)
        ));
      }
    }
    stars.sort(new Comparator<Star>() {
      @Override
      public int compare(Star s1, Star s2) {
        return s1.layer > s2.layer ? 1 :
                s1.layer > s2.layer ? -1 : 0;
      }
    });
  }

  public static void draw(final Batch batch, final float cameraX, final float cameraY, float zoom) {
    stars.forEach(s -> {
      s.draw(batch, cameraX, cameraY, zoom);
    });
  }

  private static float normalRandom(float start, float end) {
    return ((float) random.nextGaussian() + 1f) * 0.5f * (end - start) + start;
  }
}
