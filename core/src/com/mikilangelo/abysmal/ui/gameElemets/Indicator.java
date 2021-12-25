package com.mikilangelo.abysmal.ui.gameElemets;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.math.Vector3;
import com.mikilangelo.abysmal.components.repositories.TexturesRepository;

public class Indicator {

  private static final byte FULL_HEIGHT = 25,
          INDICATOR_START_X = 3, INDICATOR_START_Y = 19, INDICATOR_MAX_WIDTH = 54,
          ICON_START_X = 6, ICON_START_Y = 6, ICON_HEIGHT = 7,
          BIG_FONT_HEIGHT = 9, BIG_FONT_START_Y = 5,
          SMALL_FONT_HEIGHT = 5, SMALL_START_Y = 6;

  private static Texture background, border;

  public static void init() {
    background = TexturesRepository.get("UI/indicators/back.png");
    border = TexturesRepository.get("UI/indicators/border.png");
  }

  private final Texture icon;
  private final Sprite bar;
  private final int maxValue;
  private final byte maxValueLength;
  private final float r, g, b;
  private final float initX;
  private float x, iconX, barX, fontOffset, smallFontSize;
  private static float ratio, y, barHeight, barY, barMaxWidth, bigFontSize, bigFontY,
          smallFontY, iconY, iconHeight, iconWidth, fullHeight, fullWidth, fontScale;
  private final GlyphLayout maxText, currentText;

  public Indicator(String iconName, String barColor, Vector3 color, BitmapFont font, int max, float x, int screenHeight) {
    icon = TexturesRepository.get("UI/indicators/" + iconName + ".png");
    bar = new Sprite(TexturesRepository.get("UI/indicators/" + barColor + ".png"));
    r = color.x; g = color.y; b = color.z;
    maxValueLength = (byte) String.valueOf(max).length();
    this.maxValue = max;
    this.initX = x;
    fontScale = font.getScaleY() / font.getCapHeight();
    currentText = new GlyphLayout(font, String.valueOf(max));
    maxText = new GlyphLayout(font, String.valueOf(max));
    resize(screenHeight);
  }

  public void draw(Batch batch, BitmapFont font, float currentValue) {
    if (currentValue < 0) currentValue = 0;
    batch.draw(background, x, y, fullWidth, fullHeight);
    bar.setAlpha(0.14f + 0.5f * currentValue / maxValue);
    bar.draw(batch);
    batch.draw(bar, barX, barY, barMaxWidth * currentValue / maxValue, barHeight);
    batch.draw(bar, barX + barMaxWidth * currentValue / maxValue - ratio * 0.005f,
            barY + ratio, ratio * 1.01f, barHeight / 2);
    batch.draw(border, x, y, fullWidth, fullHeight);
    batch.draw(icon, iconX, iconY, iconWidth, iconHeight);

    font.setColor(r, g, b, 0.6f);
    font.getData().setScale(smallFontSize * fontScale * (0.5f + 1.0f / maxValueLength),
            smallFontSize * fontScale);
    maxText.setText(font, "|" + maxValue);
    font.draw(batch, maxText, fontOffset - maxText.width, smallFontY);
    font.getData().setScale( bigFontSize * fontScale);
    currentText.setText(font, String.valueOf((int) currentValue));
    font.setColor(r, g, b, 1);
    font.draw(batch, currentText, fontOffset - maxText.width - 2 * ratio - currentText.width, bigFontY);
    font.setColor(r, g, b, 0.1f);
    currentText.setText(font, "0000");
    byte length = (byte) String.valueOf((int)currentValue).length();
    font.draw(batch, "0000".substring(0, 4 - length),
            fontOffset - maxText.width - currentText.width - 2 * ratio, bigFontY);
  }


  public void resize(int h) {
    ratio = (25 + 0.05f * h) / FULL_HEIGHT;
    fullHeight = FULL_HEIGHT * ratio;
    fullWidth = fullHeight * background.getWidth() / background.getHeight();
    x = initX * ratio;
    y = h - FULL_HEIGHT * ratio - 4 * ratio;
    barHeight = 4.2f * ratio;
    barX = x + INDICATOR_START_X * ratio;
    barY = y + fullHeight - INDICATOR_START_Y * ratio - barHeight;
    barMaxWidth = INDICATOR_MAX_WIDTH * ratio;
    bigFontSize = BIG_FONT_HEIGHT * ratio;
    bigFontY = y + fullHeight - BIG_FONT_START_Y * ratio;
    smallFontSize = SMALL_FONT_HEIGHT * ratio;
    smallFontY = y + fullHeight - SMALL_START_Y * ratio;
    fontOffset = x + fullWidth - 6 * ratio * (0.5f + 1.0f / maxValueLength);
    iconHeight = ICON_HEIGHT * ratio;
    iconX = x + ICON_START_X * ratio;
    iconY = y + fullHeight - ICON_START_Y * ratio - iconHeight;
    iconWidth = iconHeight * icon.getWidth() / icon.getHeight();
    bar.setCenter(barX + barMaxWidth / 2, barY + 2 * ratio);
    bar.setScale(barMaxWidth + ratio, 2.2f * ratio);
  }
}
