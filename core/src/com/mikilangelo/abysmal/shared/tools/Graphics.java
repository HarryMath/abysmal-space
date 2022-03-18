package com.mikilangelo.abysmal.shared.tools;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.TextureData;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;

public abstract class Graphics {

  public static Sprite changeColor(Sprite texture, Vector3 color) {
    TextureData textureData = texture.getTexture().getTextureData();
    textureData.prepare();
    Pixmap pixmap = textureData.consumePixmap();
    final float colorBrightness = (color.x + color.y + color.z) / 3;
    final Color pixel = new Color();
    float initialBrightness;
    for (int y = 0; y < pixmap.getHeight(); y++) {
      for (int x = 0; x < pixmap.getWidth(); x++) {
        Color.rgba8888ToColor(pixel, pixmap.getPixel(x, y));
        if (pixel.a > 0) {
          initialBrightness = (pixel.r + pixel.g + pixel.b) / 3;
          if (initialBrightness < colorBrightness) {
            pixel.r = (color.x * initialBrightness / colorBrightness);
            pixel.g = (color.y * initialBrightness / colorBrightness);
            pixel.b = (color.z * initialBrightness / colorBrightness);
          } else {
            pixel.r = (color.x + (1 - color.x) / (1 - colorBrightness) * (initialBrightness - colorBrightness));
            pixel.g = (color.y + (1 - color.y) / (1 - colorBrightness) * (initialBrightness - colorBrightness));
            pixel.b = (color.z + (1 - color.z) / (1 - colorBrightness) * (initialBrightness - colorBrightness));
          }
          pixmap.setColor(Color.rgba8888(pixel));
          pixmap.fillRectangle(x, y, 1, 1);
        }
      }
    }
    Sprite result = new Sprite(new Texture(pixmap));
    textureData.disposePixmap();
    pixmap.dispose();
    return result;
  }

  public static Array<Sprite> changeColor(Array<Sprite> textures, Vector3 color) {
    Array<Sprite> result = new Array<>(textures.size);
    textures.forEach(t -> {
      Sprite s = changeColor(t, color);
      s.setScale(t.getScaleY());
      result.add(s);
    });
    return result;
  }
}
