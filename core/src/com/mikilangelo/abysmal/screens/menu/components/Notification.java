package com.mikilangelo.abysmal.screens.menu.components;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.utils.Array;
import com.mikilangelo.abysmal.shared.repositories.TexturesRepository;


public class Notification {

  private static final short maxRowLength = 40;
  private final Texture window = TexturesRepository.get("UI/notification/window.png");
  private final Texture border = TexturesRepository.get("UI/notification/border.png");
  private final Texture shadow = TexturesRepository.get("UI/notification/shadow.png");
  private final Texture button = TexturesRepository.get("UI/notification/btn.png");
  private final Texture buttonBorder = TexturesRepository.get("UI/notification/btnBorder.png");
  private final String message;
  private final short rowsAmount, rowLength;
  private float fontSize, pixelSize;
  private float w, h, centerX, centerY;

  public Notification(String message, int screenW, int screenH) {
    String[] words = message.split(" ");
    String row = "";
    Array<String> text = new Array<>();
    for (String word: words) {
      if (row.length() + word.length() <= maxRowLength) {
        row += word + " ";
      } else {
        text.add(row.trim());
        row = word + " ";
      }
    }
    row = row.trim();
    if (row.length() > 0) {
      text.add(row);
    }
    this.rowsAmount = (short) text.size;
    this.message = String.join("\n", text);
    this.rowLength = (short) Math.min(message.length(), maxRowLength);
    this.resize(screenW, screenH);
  }

  public void draw(Batch batch, BitmapFont font) {
    batch.draw(shadow, centerX - w * 0.7f, centerY - h * 0.7f, w * 1.4f, h * 1.4f);
    batch.draw(window, centerX - w * 0.5f, centerY - h * 0.5f, w, h);
    drawBorder(batch);
    font.setColor(1,0.99f, 0.95f, 0.6f);
    font.getData().setScale(fontSize / font.getCapHeight() * font.getScaleY());
    GlyphLayout textBox = new GlyphLayout(font, message);
    font.draw(batch, textBox,
            centerX - textBox.width * 0.5f,
            centerY + h * 0.5f - fontSize - textBox.height * 0.5f);
    batch.draw(button,
            centerX - fontSize * 3,
            centerY - h * 0.5f + fontSize,
            fontSize * 6,
            fontSize * 2);
    drawBorder(batch, buttonBorder,
            centerX - fontSize * 3,
            centerY - h * 0.5f + fontSize,
            fontSize * 6,
            fontSize * 2);
    batch.draw(buttonBorder, centerX - fontSize * 3,
            centerY - h * 0.5f + fontSize - pixelSize * 2,
            fontSize * 6, pixelSize * 2);
    font.setColor(1,1, 1, 1);
    font.getData().setScale(fontSize * 1.2f / font.getCapHeight() * font.getScaleY());
    textBox = new GlyphLayout(font, "OK");
    font.draw(batch, textBox,
            centerX - textBox.width * 0.5f,
            centerY - h * 0.5f + fontSize * 2 + textBox.height * 0.5f);
  }

  public boolean isInButton(int x, int y) {
    return x >= centerX - fontSize * 3 && x <= centerX + fontSize * 3 &&
            y <= centerY - h * 0.5f + fontSize * 3 && y >= centerY - h * 0.5f + fontSize;
  }

  private void drawBorder(Batch batch, Texture t, float x, float y, float width, float height) {
    batch.draw(t, x, y, width, pixelSize);
    batch.draw(t, x, y + height - pixelSize, width, pixelSize);
    batch.draw(t, x + width - pixelSize, y, pixelSize, height);
    batch.draw(t, x, y, pixelSize, height);
  }

  private void drawBorder(Batch batch) {
    batch.draw(border, centerX - w * 0.5f, centerY - h * 0.5f, w, pixelSize);
    batch.draw(border, centerX - w * 0.5f, centerY + h * 0.5f - pixelSize, w, pixelSize);
    batch.draw(border, centerX + w * 0.5f - pixelSize, centerY - h * 0.5f, pixelSize, h);
    batch.draw(border, centerX - w * 0.5f, centerY - h * 0.5f, pixelSize, h);
  }

  public void resize(int w, int h) {
    this.pixelSize = 0.5f + h / 500f * 0.7f + w / 800f * 0.3f;
    this.fontSize = 16 * pixelSize * 0.45f;
    this.w = fontSize * 4 + (maxRowLength * 0.4f + rowLength * 0.6f) *
            (fontSize * 0.5f * 0.7f + w * 0.4f / maxRowLength * 0.3f);
    this.h = (rowsAmount +
            2f + // top offset
            4f // bottom offset
    ) * fontSize * 1.4f;
    centerX = w * 0.5f;
    centerY = h * 0.5f;
  }
}
