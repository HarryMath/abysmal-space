package com.mikilangelo.abysmal.shared.tools;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.TextureData;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Array;

public abstract class PlanetsGenerator {

  public static Array<Sprite> createPlanet(Texture texture, int size, int frames) {
    return createPlanet(texture, size, frames, -1.5f, -0.9f);
  }

  public static Array<Sprite> createPlanet(
          Texture texture, int size, int frames,
          float lightX, float lightY
  ) {
    Array<Sprite> planet = new Array<>(frames);
    Array<Circle> lightCircles = createCircles(lightX, lightY, size / 3);
    TextureData textureData = texture.getTextureData();
    textureData.prepare();
    Pixmap source = textureData.consumePixmap();
    final Color pixel = new Color();
    final int w = source.getWidth();
    final int h = source.getHeight();
    final float fragmentWidth = w * 0.5f;
    final float step = source.getWidth() / (float) frames;
    Circle c;
    float shadowPower;
    for (int i = 0; i < frames; i++) {
      Pixmap frame = new Pixmap(size, size, Pixmap.Format.RGBA8888);
      for (int y = 0; y < size; y++) {
        final float yy = 2f * y / size - 1;
        final float xRange = (float) Math.sqrt(1 - yy * yy);
        final int xStart = Math.round((1 - xRange) * 0.5f * size);
        final int xEnd = Math.round((1 + xRange) * 0.5f * size);
        final float targetY = Math.round(y / (float) size * h);
        for (int x = xStart; x < xEnd; x++) {
          final float xx = 2f * x / size - 1;
          final float fragmentX1 = (float) (Math.asin(xx) / Math.PI * 2 * xRange + 1) * 0.5f * fragmentWidth;
          final float fragmentX2 = (float) ((1 - Math.sqrt(1 - xx * xx)) * xx * xRange + 1) * 0.5f * fragmentWidth;
          final float targetX = (fragmentX1 * 0.7f + fragmentX2 * 0.3f);
          Color.rgba8888ToColor(pixel, source.getPixel(
                  Math.round((targetX + step * i)) % w,
                  Math.round(targetY)
          ));
          boolean found = false;
          for (byte j = 0; j < lightCircles.size; j++) {
            c = lightCircles.get(j);
            if (c.contains(xx, yy)) {
              if (j % 2 == 0) {
                shadowPower = j;
              } else if ( (x + y) % 2 == 0 && MathUtils.random() < 0.95f) {
                shadowPower = j + 0.7f;
              } else {
                shadowPower = j - 0.9f;
              }
              shadowPower = (float) Math.pow(shadowPower / (lightCircles.size + 0.01), 1.5);
              shadowPower = 0.4f + shadowPower * 0.6f;
              pixel.r = pixel.r * (1 - shadowPower);
              pixel.g = 0.025f * shadowPower + pixel.g * (1 - shadowPower);
              pixel.b = 0.07f * shadowPower + pixel.b * (1 - shadowPower);
              found = true;
              break;
            }
          }
          if (!found) {
            Logger.log("PlanetsGenerator", "createPlanet", "not in circle: [" + xx + ", " + yy + "]");
          }
          frame.setColor(Color.rgba8888(pixel));
          frame.fillRectangle(x, y, 1, 1);
        }
      }
      planet.add(new Sprite(new Texture(frame)));
      frame.dispose();
    }
    textureData.disposePixmap();
    source.dispose();
    Logger.log("PlanetsGenerator", "createPlanet", "done. total frames: " + planet.size);
    return planet;
  }

  private static Array<Circle> createCircles(float lightX, float lightY, int n) {
    if (n % 2 == 0) n++;
    final float[] sizes = new float[n];
    sizes[0] = 0.35f;
    for (float i = 0.0f; i < n - 2; i++) {
      sizes[(int) i] = 0.4f + i / (n - 2) * 0.6f;
    }
    sizes[n - 1] = 3f;
    final Array<Circle> circles = new Array<>(n);
    for (float r : sizes) {
      if (r <= 1) {
        circles.add(new Circle(
                lightX * (1 - 0.97f * r),
                lightY * (1 - 0.97f * r),
                r
        ));
      } else {
        circles.add(new Circle(0, 0, r));
      }
    }
    return circles;
  }

  private static class Circle {
    public final float x, y, r;

    private Circle(float x, float y, float r) {
      this.x = x;
      this.y = y;
      this.r = r;
    }

    public boolean contains(float x, float y) {
      final float dx = Math.abs(x - this.x);
      final float dy = Math.abs(y - this.y);
      return dx < this.r && dy < this.r &&
              dx * dx + dy * dy < this.r * this.r;
    }
  }
}
